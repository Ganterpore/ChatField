package com.ganterpore.chatfield.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.Model.AppMessagingService;
import com.ganterpore.chatfield.Model.Contact;
import com.ganterpore.chatfield.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements ContactListFragment.OnFragmentInteractionListener,
        AccountInfoFragment.OnFragmentInteractionListener,
        ChatListFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private int currentView=-1;

    private AccountController accountController;

    private ArrayList<View> onScreenViews = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            if(item.getItemId() == currentView) {
//                return false;
//            }
            switch (item.getItemId()) {
                case R.id.navigation_chats:

                    openChats();
                    currentView = R.id.navigation_chats;
                    return true;
                case R.id.navigation_contacts:
//
                    openContacts();
                    currentView = R.id.navigation_contacts;
                    return true;
                case R.id.navigation_account:

                    openAccount();
                    currentView = R.id.navigation_account;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!AccountController.isLoggedOn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            this.accountController = AccountController.getInstance();
            if(currentView == -1) {
                openChats();
                currentView = R.id.navigation_chats;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (currentView) {
            case R.id.navigation_chats:
                openChats();
                break;
            case R.id.navigation_contacts:
                openContacts();
                break;
            case R.id.navigation_account:
                openAccount();
                break;
        }
    }

    public void clearScreen() {
        for(View view : onScreenViews) {
            if(view instanceof FloatingActionButton) {
                ((FloatingActionButton) view).hide();
            } else {
                view.setVisibility(View.GONE);
            }
        }
        onScreenViews.clear();
    }

    private void openAccount() {
        clearScreen();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AccountInfoFragment accountInfo = AccountInfoFragment.viewSelfInfo();
        fragmentTransaction.replace(R.id.screen_content, accountInfo);
        fragmentTransaction.commit();
    }

    private void openContacts() {
        clearScreen();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ContactListFragment contactList = ContactListFragment.newInstance(true);
        fragmentTransaction.replace(R.id.screen_content, contactList);
        fragmentTransaction.commit();

        FloatingActionButton addContactButton = findViewById(R.id.add_contact);
        addContactButton.show();
        onScreenViews.add(addContactButton);
    }

    private void openChats() {
        clearScreen();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ChatListFragment chatListFragment = ChatListFragment.newInstance();

        fragmentTransaction.replace(R.id.screen_content, chatListFragment);
        fragmentTransaction.commit();


        FloatingActionButton createGroupChatButton = findViewById(R.id.create_chat);
        createGroupChatButton.show();
        onScreenViews.add(createGroupChatButton);
    }

    public void addContact(View view) {
        //inflating the contact request view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View addContactLayout = layoutInflater.inflate(R.layout.add_contact_dialogue_box, null);
        final EditText emailEt = addContactLayout.findViewById(R.id.enter_email);
        //building the alert dialogue
        AlertDialog.Builder addContactAlert = new AlertDialog.Builder(this);
        addContactAlert.setTitle("Add contact");
        addContactAlert.setView(addContactLayout);
        addContactAlert.setNegativeButton("Cancel", null);
        addContactAlert.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                accountController.requestContact(emailEt.getText().toString());
            }
        });
        //displaying the dialogue to the UI
        addContactAlert.create().show();
    }

    public void createGroup(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //TODO fix crash that occurs when openned a second time
        final View contactListLayout = layoutInflater.inflate(R.layout.create_group_dialog_box, null);
        final ContactSelector contactSelector = (ContactSelector) getSupportFragmentManager().findFragmentById(R.id.selector_fragment);
//        final ContactSelector contactSelector = new ContactSelector();
//        FragmentTransaction fragmentTransaction = contactSelector.getChildFragmentManager().beginTransaction();
//        FragmentTransaction fragmentTransaction = contactSelector.getFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.selector_fragment, contactSelector);
//        fragmentTransaction.commit();


        AlertDialog.Builder createGroupAlert = new AlertDialog.Builder(this);
        createGroupAlert.setTitle("Create Group Chat");
        createGroupAlert.setView(contactListLayout);
        createGroupAlert.setNegativeButton("Cancel", null);
        createGroupAlert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<String> selectedContacts = contactSelector.getSelectedContacts();
                if(!selectedContacts.contains(accountController.getUid())) {
                    selectedContacts.add(accountController.getUid());
                }
                EditText groupName = contactListLayout.findViewById(R.id.enter_chat_name);
                AppMessagingService.createGroupChat(groupName.getText().toString(), selectedContacts);
            }
        });
        createGroupAlert.create().show();

    }

    @Override
    public void onContactSelected(Contact contact) {
            Intent intent = new Intent(this, AccountInfoActivity.class);
            intent.putExtra("contact", contact.getUserID());
            intent.putExtra("conversationID", contact.getConversationID());
            startActivity(intent);
    }

    @Override
    public void onChatSelected(String contactID, String conversationID) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
        intent.putExtra(ChatActivity.CONTACT_ID, contactID);
//        intent.putExtra("name", contact.getFirstname() + " " + contact.getLastname());
        startActivity(intent);
    }

    @Override
    public void logout() {
        accountController.logout();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onBackPressed() {
        if(currentView != R.id.navigation_chats) {
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.navigation_chats);
            currentView = R.id.navigation_chats;
            openChats();
        } else {
            super.onBackPressed();
        }
    }
}
