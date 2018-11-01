package com.ganterpore.chatfield.View;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.Model.Contact;
import com.ganterpore.chatfield.R;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class ContactList extends AppCompatActivity {

    private AccountController account;
    private FirestoreRecyclerAdapter<Contact, ContactListViewHolder> adapter;
    LinearLayoutManager linearLayoutManager;

    /**
     * Class constructor
     * @param savedInstanceState Untouched by our software
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account = AccountController.getInstance();

        //The XML fragments used by the Contacts list
        setContentView(R.layout.activity_contact_list);

        //Driver for recycler view
        displayContacts();
    }

//    /**
//     * Start the AddContactActivity
//     */
//    private void addContact() {
//        startActivity(new Intent(this, AddContactActivity.class));
//    }

    /**
     * Driver function to display each contact of a user
     */
    private void displayContacts() {
        //Tell the recycler view to display results as a list
        RecyclerView allContacts = findViewById(R.id.contact_list_all);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        allContacts.setLayoutManager(linearLayoutManager);

        //Query the database for contacts and project the results onto the RecyclerView
        Query contacts = account.getContacts();
        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(contacts, Contact.class).build();

        adapter = new FirestoreRecyclerAdapter<Contact, ContactListViewHolder>(options) {
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
                Log.d("A", "onBindViewHolder: UID is " + model.getUserID());
                Log.d("A", "onBindViewHolder: name is " + model.getFirstname());
                holder.initialiseDisplay(model);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        adapter.notifyDataSetChanged();
        allContacts.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    /**
     * Used for defining the behaviour of each users record
     */
    public class ContactListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        Context context;
        String contactID;
        String conversationID;
        String contactName;

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

            contactID = contact.getUserID();
            conversationID = contact.getConversationID();
            contactName = contact.getFirstname() + " " + contact.getLastname();

            //XML fragments
            TextView contactNameView = itemView.findViewById(R.id.list_contact_name);
            contactNameView.setText(contactName);
            Button messageButton = itemView.findViewById(R.id.contact_message_button);
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                //Begin a chat with a user after clicking the messageButton
                public void onClick(View view) {
                    openChat(view);
                }
            });

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

        /**
         * If we need help derive help from the main activity
         * @param v View for context
         */
        private void selectContact(View v) {
            //if not coming from help, open up account info
//            Intent intent = new Intent(context, AccountInfoActivity.class);
//            intent.putExtra("contact", contactID);
//            intent.putExtra("conversationID", conversationID);
//            startActivity(intent);
        }
    }
}
