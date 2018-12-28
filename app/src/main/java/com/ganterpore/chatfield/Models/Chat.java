package com.ganterpore.chatfield.Models;

import com.google.firebase.firestore.DocumentSnapshot;

public class Chat {
    private  String name;
    private long lastMessageSentAt;
    private String conversationID;
    private boolean seen;
    private String type;

    public Chat(String name, long lastMessageSentAt, String conversationID, boolean seen, String type) {
        this.name = name;
        this.lastMessageSentAt = lastMessageSentAt;
        this.conversationID = conversationID;
        this.seen = seen;
        this.type = type;
    }

    public Chat() {
    }

    public Chat(DocumentSnapshot doc) {
        if(doc.exists()) {
            if(doc.contains("name")) {
                Object name = doc.get("name");
                this.name = (String) name;
            }
            if(doc.contains("lastMessageSentAt")) {
                Object lastMessageSentAt = doc.get("lastMessageSentAt");
                this.lastMessageSentAt = (long) lastMessageSentAt;
            }
            if(doc.contains("conversationID")) {
                Object conversationID = doc.get("conversationID");
                this.conversationID = (String) conversationID;
            }
            if(doc.contains("seen")) {
                Object seen = doc.get("seen");
                this.seen = (boolean) seen;
            }
            if(doc.contains("type")) {
                Object type = doc.get("type");
                this.type = (String) type;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastMessageSentAt() {
        return lastMessageSentAt;
    }

    public void setLastMessageSentAt(long lastMessageSentAt) {
        this.lastMessageSentAt = lastMessageSentAt;
    }

//    public void setLastMessageSentAt(String lastMessageSentAt) {
//        this.lastMessageSentAt = Long.parseLong(lastMessageSentAt);
//    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Chat)) {
            return false;
        } else {
            Chat other = (Chat) obj;
            return     this.name.equals(other.getName())
                    && this.lastMessageSentAt == other.getLastMessageSentAt()
                    && this.conversationID.equals(other.getConversationID())
                    && this.seen == other.isSeen()
                    && this.type.equals(other.getType());
        }
    }
}
