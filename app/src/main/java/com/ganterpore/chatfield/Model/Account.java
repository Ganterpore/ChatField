package com.ganterpore.chatfield.Model;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

public class Account {
    private String userID;
    private String firstname;
    private String lastname;
    private String email;
    private String number;
    private String bio;
    private boolean helper;

    public Account(String userID, String firstname, String lastname, String email,
                   String number, String bio, boolean helper) {
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.number = number;
        this.bio = bio;
        this.helper = helper;
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
            if(document.contains("number")) {
                Object number = document.get("number");
                if(number != null){
                    this.number = number.toString();
                }
            }
            if(document.contains("bio")) {
                Object bio = document.get("bio");
                if(bio != null){
                    this.bio = bio.toString();
                }
            }
            if(document.contains("helper")) {
                try {
                    this.helper = document.getBoolean("helper");
                } catch (NullPointerException e) {
                    e.printStackTrace();
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isHelper() {
        return helper;
    }

    public void setHelper(boolean helper) {
        this.helper = helper;
    }
}
