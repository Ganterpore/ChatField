package com.ganterpore.chatfield.Model;

public class Chat {
    String user1ID;
    String user2ID;
    Boolean user1Seen;
    Boolean user2Seen;

    public Chat(String user1ID, String user2ID) {
        this.user1ID = user1ID;
        this.user2ID = user2ID;
    }

    public Chat() {
    }

    public String getUser1ID() {
        return user1ID;
    }

    public void setUser1ID(String user1ID) {
        this.user1ID = user1ID;
    }

    public String getUser2ID() {
        return user2ID;
    }

    public void setUser2ID(String user2ID) {
        this.user2ID = user2ID;
    }

    public Boolean getUser1Seen() {
        return user1Seen;
    }

    public void setUser1Seen(Boolean user1Seen) {
        this.user1Seen = user1Seen;
    }

    public Boolean getUser2Seen() {
        return user2Seen;
    }

    public void setUser2Seen(Boolean user2Seen) {
        this.user2Seen = user2Seen;
    }
}
