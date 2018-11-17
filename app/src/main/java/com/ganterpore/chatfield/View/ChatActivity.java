package com.ganterpore.chatfield.View;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.ganterpore.chatfield.Controller.Camera;
import com.ganterpore.chatfield.R;

import java.io.File;
import java.io.IOException;

public class ChatActivity extends AppCompatActivity implements ChatFragment.OnFragmentInteractionListener{
    public static final String CONVERSATION_ID = "conversationID";
    public static final String CONTACT_ID = "contactID";
    private static final String TAG = ChatActivity.class.getName();
    public static final String NAME = "name";
    public static final int CAMERA_RESULT = 1;

    private ChatFragment chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra(CONVERSATION_ID);

        ActionBar bar = getSupportActionBar();
        //TODO get conversation details and add title to toolbar

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        chat = ChatFragment.newInstance(conversationId);
        fragmentTransaction.replace(R.id.chat_container, chat).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CAMERA_RESULT:
                //when a photo is taken in this activity, send it as a message
                File image = Camera.getLastPhoto();
                chat.sendImage(image);
                break;
        }
    }

    @Override
    public void onMessageReceived() {
        //do nothing
    }

    @Override
    public void pictureRequested() {
        try {
            Camera.takePicture(this, CAMERA_RESULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Failed to get location to store photos",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
