package com.ganterpore.chatfield.Model;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

public class Account {
    private String userID;
    private String firstname;
    private String lastname;
    private String email;
    private String bio;

    public Account(String userID, String firstname, String lastname, String email, String bio) {
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.bio = bio;
    }

    public Account(DocumentSnapshot document) {
        if (document.exists()) {
            if(document.contains("userID")) {
                Object userID = document.get("userID");
                if(userID != null) {
                    this.userID = userID.toString();
                }
            }
            if(document.contains("firstname")) {
                Object firstname = document.get("firstname");
                if(firstname != null){
                    this.firstname = firstname.toString();
                }
            }
            if(document.contains("lastname")) {
                Object lastname = document.get("lastname");
                if(lastname != null){
                    this.lastname = lastname.toString();
                }
            }
            if(document.contains("email")) {
                Object email = document.get("email");
                if(email != null){
                    this.email = email.toString();
                }
            }
            if(document.contains("bio")) {
                Object bio = document.get("bio");
                if(bio != null){
                    this.bio = bio.toString();
                }
            }
        }
    }

    public Account() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

}
