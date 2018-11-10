package com.ganterpore.chatfield.Model;

import com.google.firebase.firestore.DocumentSnapshot;

public class Contact  {
    private String conversationID; //unique id representing the relationship between the two users
    private String userID;
    private String firstname;
    private String lastname;
    private String email;

    public Contact(String conversationID, String userID, String firstname, String lastname, String email) {
        this.conversationID = conversationID;
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public Contact() {
    }

    public Contact(DocumentSnapshot doc) {
        if(doc.exists()) {
            if(doc.contains("userID")) {
                Object conversationID = doc.get("userID");
                if(conversationID != null) {
                    this.conversationID = conversationID.toString();
                }
            }
            if(doc.contains("conversationID")) {
                Object conversationID = doc.get("conversationID");
                if(conversationID != null) {
                    this.conversationID = conversationID.toString();
                }
            }
            if(doc.contains("firstname")) {
                Object conversationID = doc.get("firstname");
                if(conversationID != null) {
                    this.conversationID = conversationID.toString();
                }
            }
            if(doc.contains("lastname")) {
                Object conversationID = doc.get("lastname");
                if(conversationID != null) {
                    this.conversationID = conversationID.toString();
                }
            }
        }
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
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
}
