package com.ganterpore.chatfield.Controller;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChatController {
    public static final String MESSAGE_BRANCH = "message";
    public static final String CONVERSATION_BRANCH = "conversations";
    public static final String IMAGE_BRANCH = "images";

    private String senderID;
    private String receiverID;
    private String conversationID;

    private int userNumber;
    private FirebaseFirestore db;

    private AccountController accountC;

    public static FirebaseFirestore getDatabaseInstance() {
        return FirebaseFirestore.getInstance();
    }

    /**
     * used to create an instance of the ChatController with an already started conversationID.
     * @param conversationID, the id of the conversation to open
     * @return the ChatController instance
     * @throws ExecutionException when something goes wrong in execution
     * @throws InterruptedException when the process is interrupted
     */
    public static ChatController openChat(String conversationID)
            throws ExecutionException, InterruptedException {
        ChatController chat = new ChatController();

        //initialising variables
        chat.conversationID = conversationID;
        chat.senderID = AccountController.getInstance().getUid();
        Task<DocumentSnapshot> getConversation = chat.db.collection(CONVERSATION_BRANCH)
                .document(conversationID).get();
        Tasks.await(getConversation);
        DocumentSnapshot conversation = getConversation.getResult();
        //getting recieverID
        if(chat.senderID.equals(conversation.get("user1ID"))) {
            chat.userNumber = 1;
            Object user2 = conversation.get("user2ID");
            if(user2 != null) {
                chat.receiverID = user2.toString();
            }
        } else {
            chat.userNumber = 2;
            Object user1 = conversation.get("user1ID");
            if(user1 != null) {
                chat.receiverID = user1.toString();
            }
        }
        //when chat opened, mark it as read
        chat.markAsRead();
        return chat;
    }

    private ChatController() {
        this.db = getDatabaseInstance();
        this.accountC = AccountController.getInstance();
    }

    /**
     * Called when a conversationID is between two users, will either find an existing chat, or
     * make a new one between the users
     * @param user1ID the first user
     * @param user2ID the second user
     * @return a conversationID of a new or already existing conversation
     * @throws ExecutionException when something goes wrong in execution
     * @throws InterruptedException when the process is interrupted
     */
    public static String findOrBuildConversation(String user1ID, String user2ID)
            throws ExecutionException, InterruptedException {
        final FirebaseFirestore db = getDatabaseInstance();

        //getting a pre-existing conversation between the two users
        Task<QuerySnapshot> conversationQuery = db.collection(CONVERSATION_BRANCH)
                .whereEqualTo("user1ID", user1ID).whereEqualTo("user2ID", user2ID).get();
        Tasks.await(conversationQuery);
        //get all the conversations
        QuerySnapshot conversations = conversationQuery.getResult();
        //if there are conversations, return the id from the first one
        if (!conversations.isEmpty()) {
            return conversations.getDocuments().get(0).getId();
        }
        //if there aren't any, try a conversation with the other user order
        else {
            Task<QuerySnapshot> conversationQuery2 = db.collection(CONVERSATION_BRANCH)
                    .whereEqualTo("user1ID", user2ID).whereEqualTo("user2ID", user1ID).get();
            Tasks.await(conversationQuery2);
            //get all the conversations
            QuerySnapshot conversations2 = conversationQuery2.getResult();
            //if there are conversations, return the id from the first one
            if (!conversations2.isEmpty()) {
                return conversations2.getDocuments().get(0).getId();
            }
        }

        //if we reach this point, no conversation is currently set up
        // therefore we must make a new convo
        Chat chat = new Chat(user1ID, user2ID);
        chat.setUser1Seen(true);
        chat.setUser2Seen(true);
        Task<DocumentReference> newChat = db.collection(CONVERSATION_BRANCH).add(chat);
        Tasks.await(newChat);
        return newChat.getResult().getId();
    }

    /**
     * used to mark the conversation as read
     */
    public void markAsRead() {
        Map<String, Boolean> data = new HashMap<>();
        //if its a conversation with yourself, mark both as read
        if(receiverID.equals(senderID)) {
            data.put("user1Seen", true);
            data.put("user2Seen", true);
        }
        //otherwise only mark the current user as read
        else if(userNumber == 1) {
            data.put("user1Seen", true);
        } else {
            data.put("user2Seen", true);
        }
        updateConversation(data);
    }

    private void updateConversation(Map<String, Boolean> data) {
        db.collection(CONVERSATION_BRANCH).document(conversationID).set(data, SetOptions.merge());
    }

    /**
     * sends a message to the conversation/other user
     * @param message, the message to send
     */
    public void sendMessage(String message) {
        //if the message is empty, don't send it
        if(message.length()==0) {
            return;
        }
        String sender = accountC.getUserAccount().getFirstname() + " " + accountC.getUserAccount().getLastname();
        Message data = new Message(Message.TEXT_TYPE, message, senderID, sender, System.currentTimeMillis());
        db.collection(CONVERSATION_BRANCH).document(conversationID).collection(MESSAGE_BRANCH).add(data);
    }

    /**
     * sends an image to the conversation/other user
     * @param image, the image to send
     */
    public void sendImage(final File image) {
        //if the image doesn't exist, don't send it
        if(image==null) {
            return;
        }
        String sender = accountC.getUserAccount().getFirstname() + " " + accountC.getUserAccount().getLastname();
        final Message data = new Message(Message.IMAGE_TYPE, "Image", senderID, sender, System.currentTimeMillis());

        //add a record for the image
        db.collection(CONVERSATION_BRANCH).document(conversationID).collection(IMAGE_BRANCH).add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()) {
                            //get the id for the image, and upload it to the cloud storage
                            String imageID = task.getResult().getId();
                            data.setMessage(imageID);
                            uploadImage(image, imageID).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    //send the message to the chat
                                    db.collection(CONVERSATION_BRANCH).document(conversationID)
                                            .collection(MESSAGE_BRANCH).add(data);
                                }
                            });
                        }
                    }
                });
    }

    /**
     * used to upload an image to the current conversation
     * @param image, the image to upload
     * @param filename, the filename to store it under
     * @return a task uploading the image
     */
    private UploadTask uploadImage(File image, String filename) {
        StorageReference picDest = FirebaseStorage.getInstance().getReference()
                .child("ConversationPictures/"+conversationID+"/"+filename+".jpg");

        Uri fileUri = Uri.fromFile(image);
        return picDest.putFile(fileUri);
    }

    /**
     * used to download an image from the current conversation
     * @param filename, the name of the image to be downloaded
     * @param downloadLoc, the file to download the image to
     * @return the task downloading the image
     */
    public FileDownloadTask downloadImage(String filename, File downloadLoc) {
        StorageReference picDest = FirebaseStorage.getInstance().getReference()
                .child("ConversationPictures/"+conversationID+"/"+filename+".jpg");
        return picDest.getFile(downloadLoc);
    }

    /**
     * Gets a Query representing the list of all messages sent into the conversation
     * @return a query for all the messages
     */
    public Query getAllMessages() {
        return db.collection(CONVERSATION_BRANCH).document(conversationID)
                .collection(MESSAGE_BRANCH).orderBy("sentAt");
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getConversationID() {
        return conversationID;
    }
}
