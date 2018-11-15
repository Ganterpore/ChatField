package com.ganterpore.chatfield.View;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.R;

import java.security.AccessControlContext;

public class AccountInfoActivity extends AppCompatActivity implements AccountInfoFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Intent intent = getIntent();

        AccountInfoFragment accountInfo = AccountInfoFragment.viewContactInfo(
                intent.getStringExtra("contact"), intent.getStringExtra("conversationID"));
        fragmentTransaction.replace(R.id.info_fragment, accountInfo);
        fragmentTransaction.commit();
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
        AccountController.getInstance().logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


}
