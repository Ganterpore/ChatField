package com.ganterpore.chatfield.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.chatfield.Controller.ChatController;
import com.ganterpore.chatfield.Model.Chat;
import com.ganterpore.chatfield.Model.Message;
import com.ganterpore.chatfield.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    private static final String CONVERSATION_ID = "param1";

    private String conversationID;
    private ChatController chatController;

    private OnFragmentInteractionListener mListener;
    private View view;
    private FirestoreRecyclerAdapter<Message, MessageListViewHolder> adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param conversationID Parameter 1.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String conversationID) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(CONVERSATION_ID, conversationID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            conversationID = getArguments().getString(CONVERSATION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        FloatingActionButton send = view.findViewById(R.id.sendMessage);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = view.findViewById(R.id.typeMessage);
                String message = input.getText().toString();
                chatController.sendMessage(message);
                //reset text field
                input.setText("");
            }
        });

        FloatingActionButton camera = view.findViewById(R.id.sendPhoto);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.pictureRequested();
            }
        });
        showProgress(true);
        (new ChatBuilder(this)).execute();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void sendImage(File image) {
        chatController.sendImage(image);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    /**
     * initialises the RecyclerView of the chat
     */
    private void displayChat(){
        final RecyclerView allMessages = view.findViewById(R.id.allMessages);
        Query getMessages = chatController.getAllMessages();

        chatController.getAllMessages().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mListener.onMessageReceived();
            }
        });

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(getMessages, Message.class).build();

        adapter = new FirestoreRecyclerAdapter<Message, MessageListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageListViewHolder holder, int position, @NonNull Message model) {
                chatController.markAsRead();
                holder.initialiseDisplay(model);
            }

            @NonNull
            @Override
            public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //if the message is an image, then prepare for an image
                if(viewType==Message.IMAGE_TYPE) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.image_message, parent, false);
                    return new MessageListViewHolder(view, viewType);
                }
                //otherwise handle normally
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message, parent, false);
                return new MessageListViewHolder(view, viewType);
            }

            @Override
            public int getItemViewType(int position) {
                Message message = getItem(position);
                return message.getMessageType();
            }
        };
        adapter.notifyDataSetChanged();
        allMessages.setAdapter(adapter);
        adapter.startListening();
    }

    public class MessageListViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        Context context;
        int messageType;

        MessageListViewHolder(View itemView, int messageType) {
            super(itemView);
            this.itemView = itemView;
            this.context = itemView.getContext();
            this.messageType = messageType;
        }

        public void initialiseDisplay(Message message) {
            TextView messageUser = itemView.findViewById(R.id.message_user);
            messageUser.setText(message.getUserName());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            switch (messageType) {
                case Message.TEXT_TYPE:
                    TextView messageText = itemView.findViewById(R.id.message_text);
                    messageText.setText(message.getMessage());
                    break;
                case Message.IMAGE_TYPE:
                    StorageReference imgURL = storageRef.child((
                            "ConversationPictures/" +chatController.getConversationID()
                                    + "/" + message.getMessage()+".jpg"));
                    updateImageView(message.getMessage());
                    break;
            }
        }

        private void updateImageView(final String imageName) {
            final ImageView messageImage = itemView.findViewById(R.id.message_image);
            final TextView loadingSign = itemView.findViewById(R.id.message_loading);
            //create a file to save the image to
            File imageFile = null;
            try {
                imageFile = File.createTempFile(imageName, "jpg");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context,
                        "failed to download image",
                        Toast.LENGTH_SHORT).show();
            }
            if(imageFile != null) {
                final String fileLoc = imageFile.getAbsolutePath();
                //download the image to the file
                chatController.downloadImage(imageName, imageFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            //set the ImageView to the image
                            BitmapFactory.Options bmOptions;
                            bmOptions = new BitmapFactory.Options();
                            bmOptions.inSampleSize=2;
                            messageImage.setImageBitmap(
                                    BitmapFactory.decodeFile(fileLoc,
                                            bmOptions
                                    )
                            );
                            loadingSign.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(context,
                                    "failed to download image",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * This method is called when a message is received by the chatControllerBuilder.
         * Then the parent activity can handle this however they want
         */
        void onMessageReceived();

        /**
         * Called when a user requests a photo to be taken to be sent in the chat. The method should
         * implement a call to a service to supply a photo.
         * Once the parent activity has finished taking and editing the picture,
         * this call should be followed with a chatFragment.sendImage(File image) call
         */
        void pictureRequested();
    }

    private static class ChatBuilder extends AsyncTask<Void, Void, Boolean> {
        private String conversationID;
        private ChatFragment parent;

        public ChatBuilder(ChatFragment parent) {
            this.conversationID = parent.conversationID;
            this.parent = parent;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(conversationID != null) {
                try {
                    parent.chatController = ChatController.openChat(conversationID);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            parent.showProgress(false);
            if (success) {
                parent.displayChat();
            }
        }
    }

    /**
     * Shows the progress UI and hides the Chat Information
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        final View allMessagesView = view.findViewById(R.id.allMessages);
        final View typeMessageView = view.findViewById(R.id.typeMessage);
        final View sendMessageView = view.findViewById(R.id.sendMessage);
        final View messageContainerview = view.findViewById(R.id.typeMessageContainer);
        final View progressView    = view.findViewById(R.id.chat_c_progress);
        final View sendImageView = view.findViewById(R.id.sendPhoto);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            allMessagesView.setVisibility(show ? View.GONE : View.VISIBLE);
            allMessagesView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    allMessagesView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            typeMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
            typeMessageView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    typeMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            sendMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
            sendMessageView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sendMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            sendImageView.setVisibility(show ? View.GONE : View.VISIBLE);
            sendImageView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sendImageView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            messageContainerview.setVisibility(show ? View.GONE : View.VISIBLE);
            messageContainerview.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    messageContainerview.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            allMessagesView.setVisibility(show ? View.GONE : View.VISIBLE);
            sendMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
            typeMessageView.setVisibility(show ? View.GONE : View.VISIBLE);
            messageContainerview.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
