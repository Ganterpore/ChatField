package com.ganterpore.chatfield.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.R;

public class MainActivity extends AppCompatActivity implements ContactListFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private int currentView;

    private AccountController accountController;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_chats:

                    currentView = R.id.navigation_chats;
                    return true;
                case R.id.navigation_contacts:

                    openContacts();
                    currentView = R.id.navigation_contacts;
                    return true;
                case R.id.navigation_account:

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
        }
    }

    private void openContacts() {
//        Intent intent = new Intent(this, ContactList.class);
//        startActivity(intent);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ContactListFragment contactList = ContactListFragment.newInstance();
        fragmentTransaction.add(R.id.screen_content, contactList, "contacts_content");
        fragmentTransaction.commit();

        //TODO add animation
        FloatingActionButton addContactButton = findViewById(R.id.add_contact);
        addContactButton.show();
    }

    public void addContact(View view) {
        //creating a pop up window confirming the call
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View addContactLayout = layoutInflater.inflate(R.layout.add_contact_dialogue_box, null);
        final EditText emailEt = addContactLayout.findViewById(R.id.enter_email);

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

        addContactAlert.create().show();

//        final PopupWindow popupWindow = new PopupWindow(addContactLayout,
//                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
//                true);
//        popupWindow.setOutsideTouchable(true);
////        popupWindow.setAnimationStyle(R.style.MyPopupWindow_anim_style);
//
//        // PopupWindow pop up position
//        final View activityContent = findViewById(android.R.id.content);
//        popupWindow.showAtLocation(activityContent,
//                Gravity.CENTER, 0, 0);
//        activityContent.setAlpha(0.5f);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                //when the popup window is closed, remove the transparency
//                activityContent.setAlpha(1f);
//            }
//        });
    }

    @Override
    public void onFragmentInteraction(View view) {

    }
}
