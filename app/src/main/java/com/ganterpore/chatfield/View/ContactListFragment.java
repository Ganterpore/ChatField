package com.ganterpore.chatfield.View;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.Model.Account;
import com.ganterpore.chatfield.Model.Contact;
import com.ganterpore.chatfield.R;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class ContactListFragment extends Fragment {
    private AccountController accountController;

    private FirestoreRecyclerAdapter<Contact, ContactListViewHolder> contactsAdapter;

    private View view;

    private OnFragmentInteractionListener mListener;
    private FirestoreRecyclerAdapter<Account, RequestListViewHolder> requestsAdapter;

    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance(){
        return new ContactListFragment();
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
        view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        displayContacts();
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


    @Override
    public void onStart() {
        super.onStart();
        contactsAdapter.startListening();
        requestsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        contactsAdapter.stopListening();
        requestsAdapter.stopListening();
    }

    private void displayContacts() {
        //Tell the recycler view to display results as a list
        RecyclerView allContacts = view.findViewById(R.id.contact_list_all);
        RecyclerView allRequests = view.findViewById(R.id.request_list_all);

        //Query the database for contacts and project the results onto the RecyclerView
        Query contacts = accountController.getContacts();
        Query requests = accountController.getRequests();
        FirestoreRecyclerOptions<Contact> contactOptions = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(contacts, Contact.class).build();
        FirestoreRecyclerOptions<Account> requestOptions = new FirestoreRecyclerOptions.Builder<Account>()
                .setQuery(requests, Account.class).build();

        contactsAdapter = new FirestoreRecyclerAdapter<Contact, ContactListViewHolder>(contactOptions) {
            @NonNull
            @Override
            public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contact_list_item, parent, false);

                return new ContactListViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void onBindViewHolder(@NonNull ContactListViewHolder holder, int position, @NonNull Contact model) {
                holder.initialiseDisplay(model);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        requestsAdapter = new FirestoreRecyclerAdapter<Account, RequestListViewHolder>(requestOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RequestListViewHolder holder, int position, @NonNull Account model) {
                holder.initialiseDisplay(model);
            }

            @NonNull
            @Override
            public RequestListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.request_list_item, parent, false);

                return new RequestListViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if(getItemCount() == 0) {
                    showRequests(false);
                } else {
                    showRequests(true);
                }
            }
        };
        requestsAdapter.notifyDataSetChanged();
        allRequests.setAdapter(requestsAdapter);

        contactsAdapter.notifyDataSetChanged();
        allContacts.setAdapter(contactsAdapter);
    }

    private void showRequests(boolean show) {
        TextView requestTitle = view.findViewById(R.id.requests_title);
        RecyclerView requests = view.findViewById(R.id.request_list_all);
        if(show) {
            requestTitle.setVisibility(View.VISIBLE);
            requests.setVisibility(View.VISIBLE);
        } else {
            requestTitle.setVisibility(View.GONE);
            requests.setVisibility(View.GONE);
        }
    }

    public interface OnFragmentInteractionListener {
        void onContactSelected(Contact contact);
    }

    public class ContactListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        Context context;
        Contact contact;

        /**
         * Public Constructor
         * @param itemView The individual person record inside the recycler view
         */
        ContactListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        /**
         * Fills out the information for a given contact
         * @param contact The contact to be used
         */
        public void initialiseDisplay(final Contact contact) {
            this.contact = contact;

            //XML fragments
            TextView contactNameView = itemView.findViewById(R.id.list_contact_name);
            contactNameView.setText(contact.getFirstname() + " " + contact.getLastname());
//            Button messageButton = itemView.findViewById(R.id.contact_message_button);
//            messageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                //Begin a chat with a user after clicking the messageButton
//                public void onClick(View view) {
//                    openChat(view);
//                }
//            });

        }

        @Override
        public void onClick(View v) {
            selectContact(v);
        }

        /**
         * Opens a chat when clicked
         * @param v View for context
         */
        private void openChat(View v) {
//            Intent intent = new Intent(context, ChatActivity.class);
//            intent.putExtra(CONVERSATION_ID, conversationID);
//            intent.putExtra(ChatActivity.CONTACT_ID, contactID);
//            intent.putExtra("needHelp", needHelp);
//            intent.putExtra("name", contactName);
//            context.startActivity(intent);
        }

        private void selectContact(View v) {
//            Intent intent = new Intent(context, AccountInfoActivity.class);
//            intent.putExtra("contact", contactID);
//            intent.putExtra("conversationID", conversationID);
//            startActivity(intent);
            mListener.onContactSelected(contact);
        }
    }

    private class RequestListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View itemView;
        Context context;
        Account requester;

        RequestListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        public void initialiseDisplay(Account request) {
            requester = request;

            TextView contactName = itemView.findViewById(R.id.list_request_name);
            String nameString = request.getFirstname() + " " + request.getLastname();
            contactName.setText(nameString);
            Button acceptButton = itemView.findViewById(R.id.request_accept_button);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accountController.acceptContact(requester.getUserID());
                }
            });
            Button rejectButton = itemView.findViewById(R.id.request_reject_button);
            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accountController.declineContact(requester.getUserID());
                }
            });
        }

        @Override
        public void onClick(View view) {
//            Context context = view.getContext();
//            Intent intent  = new Intent(context, AcceptContactActivity.class);
//            intent.putExtra("userID", requester.getUserID());
//            intent.putExtra("firstname", requester.getFirstname());
//            intent.putExtra("lastname", requester.getLastname());
//            intent.putExtra("email", requester.getEmail());
//            startActivity(intent);
        }
    }
}
