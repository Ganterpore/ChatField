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

    private static final String SHOW_REQUESTS = "showRequests";
    private boolean showRequests;

    private FirestoreRecyclerAdapter<Contact, ContactListViewHolder> contactsAdapter;

    private View view;

    private OnFragmentInteractionListener mListener;
    private FirestoreRecyclerAdapter<Account, RequestListViewHolder> requestsAdapter;

    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance(boolean showRequests){
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        args.putBoolean(SHOW_REQUESTS, showRequests);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountController = AccountController.getInstance();

        if(getArguments() != null) {
            showRequests = getArguments().getBoolean(SHOW_REQUESTS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        buildContactsList();
        if(showRequests) {
            buildRequestsList();
        }
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
        if(showRequests) {
            requestsAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        contactsAdapter.stopListening();
        if(showRequests) {
            requestsAdapter.stopListening();
        }
    }

    private void buildRequestsList() {
        RecyclerView allRequests = view.findViewById(R.id.request_list_all);

        Query requests = accountController.getRequests();

        FirestoreRecyclerOptions<Account> requestOptions = new FirestoreRecyclerOptions.Builder<Account>()
                .setQuery(requests, Account.class).build();

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

                return createRequestListViewHolder(view);
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
    }

    private void buildContactsList() {
        //Tell the recycler view to display results as a list
        RecyclerView allContacts = view.findViewById(R.id.contact_list_all);

        //Query the database for contacts and project the results onto the RecyclerView
        Query contacts = accountController.getContacts();

        FirestoreRecyclerOptions<Contact> contactOptions = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(contacts, Contact.class).build();

        contactsAdapter = new FirestoreRecyclerAdapter<Contact, ContactListViewHolder>(contactOptions) {
            @NonNull
            @Override
            public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contact_list_item, parent, false);

                return createContactListViewHolder(view);
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

        contactsAdapter.notifyDataSetChanged();
        allContacts.setAdapter(contactsAdapter);
    }

    private void showRequests(boolean show) {
        TextView requestTitle = view.findViewById(R.id.requests_title);
        RecyclerView requests = view.findViewById(R.id.request_list_all);
        if(show && showRequests) {
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

    public ContactListViewHolder createContactListViewHolder(View view) {
        return new ContactListViewHolder(view);
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

        }

        @Override
        public void onClick(View v) {
            selectContact(v);
        }

        private void selectContact(View v) {
            mListener.onContactSelected(contact);
        }
    }

    public RequestListViewHolder createRequestListViewHolder(View view) {
        return new RequestListViewHolder(view);
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
