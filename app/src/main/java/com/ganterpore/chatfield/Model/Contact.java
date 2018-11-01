package com.ganterpore.chatfield.Model;

import com.google.firebase.firestore.DocumentSnapshot;

public class Contact extends Account {
    private String conversationID; //unique id representing the relationship between the two users

    public Contact(String userID, String firstname, String lastname, String email, String number,
                   String bio, boolean helper, String conversationID) {
        super(userID, firstname, lastname, email, number, bio, helper);
        this.conversationID = conversationID;
    }

    public Contact(DocumentSnapshot doc) {
        super(doc);
        if(doc.exists()) {
            if(doc.contains("conversationID")) {
                Object conversationID = doc.get("conversationID");
                if(conversationID != null) {
                    this.conversationID = conversationID.toString();
                }
            }
        }
    }

    public Contact() {
        super();
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }
}
