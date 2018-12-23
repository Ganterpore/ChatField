package com.ganterpore.chatfield.L3_View_Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.chatfield.L2_Controllers.AccountController;
import com.ganterpore.chatfield.Models.Chat;
import com.ganterpore.chatfield.R;
import com.ganterpore.chatfield.L4_User_Interface.ChatActivity;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ganterpore.chatfield.L4_User_Interface.ChatActivity.CONVERSATION_ID;

public class ChatListFragment extends Fragment {
    //TODO add ability to refresh
    private AccountController accountController;

    private FirestoreRecyclerAdapter<Chat, ChatListViewHolder> chatAdapter;

    private View view;

    private ChatListFragment.OnFragmentInteractionListener mListener;

    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance(){
        return new ChatListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountController = AccountController.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        displayChat();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatListFragment.OnFragmentInteractionListener) {
            mListener = (ChatListFragment.OnFragmentInteractionListener) context;
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


    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }

    private void displayChat() {
        //Tell the recycler view to display results as a list
        RecyclerView allConversations = view.findViewById(R.id.chat_list_all);

        //Query the database for conversations and project the results onto the RecyclerView
        Query conversations = accountController.getConversations();

        FirestoreRecyclerOptions<Chat> chatOptions = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(conversations, Chat.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<Chat, ChatListFragment.ChatListViewHolder>(chatOptions) {
            @NonNull
            @Override
            public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conversation_list_item, parent, false);

                return new ChatListViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatListViewHolder holder,
                                            int position, @NonNull Chat model) {
                holder.initialiseDisplay(model);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        chatAdapter.notifyDataSetChanged();
        allConversations.setAdapter(chatAdapter);
    }

    public interface OnFragmentInteractionListener {

    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        Context context;
        Chat conversation;

        /**
         * Public Constructor
         * @param itemView The individual person record inside the recycler view
         */
        ChatListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        /**
         * Fills out the information for a given contact
         */
        public void initialiseDisplay(final Chat conversation) {
            this.conversation = conversation;

            TextView conversationName = itemView.findViewById(R.id.conversation_name);
            TextView lastSentAtTime = itemView.findViewById(R.id.last_sent_at_time);

            conversationName.setText(conversation.getName());
            Date sentAt = new Date(conversation.getLastMessageSentAt());
            SimpleDateFormat formatter = new SimpleDateFormat("d/MM/yyyy hh:mm a", Locale.getDefault());
            lastSentAtTime.setText(formatter.format(sentAt));

            if(!conversation.isSeen()) {
                conversationName.setTypeface(null, Typeface.BOLD);
                lastSentAtTime.setTypeface(null, Typeface.BOLD);
            }
        }

        @Override
        public void onClick(View v) {
            openChat(v);
        }

        /**
         * Opens a chat when clicked
         * @param v View for context
         */
        private void openChat(View v) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(CONVERSATION_ID, conversation.getConversationID());
            context.startActivity(intent);
        }
    }
}
