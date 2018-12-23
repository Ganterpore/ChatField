package com.ganterpore.chatfield.L4_User_Interface;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ganterpore.chatfield.R;

public class ConfigureUserActivity extends AppCompatActivity {
    private EditText firstnameView;
    private EditText lastnameView;
    private EditText bioView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        firstnameView = findViewById(R.id.enter_firstname);
        lastnameView = findViewById(R.id.enter_lastname);
        bioView = findViewById(R.id.enter_bio);

        Intent intent = getIntent();
        updateFields(intent);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnInfo();
            }
        });
    }

    private void updateFields(Intent intent) {
        if(intent.hasExtra("firstname")){
            firstnameView.setText(intent.getStringExtra("firstname"));
        }
        if(intent.hasExtra("lastname")){
            lastnameView.setText(intent.getStringExtra("lastname"));
        }
        if(intent.hasExtra("bio")){
            bioView.setText(intent.getStringExtra("bio"));
        }
    }

    private void returnInfo() {
        Intent result = new Intent();
        String firstname = firstnameView.getText().toString();
        String lastname = lastnameView.getText().toString();
        if((firstname.length()) > 1 && (lastname.length() > 1)) {
            result.putExtra("firstname", firstname);
            result.putExtra("lastname", lastname);
            result.putExtra("bio", bioView.getText().toString());
            setResult(Activity.RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this, "Names are too short", Toast.LENGTH_SHORT).show();
        }
    }
}
