package com.ganterpore.chatfield.L2_Controllers;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ganterpore.chatfield.Models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChatController {
    public static final String MESSAGE_BRANCH = "message";
    public static final String CONVERSATION_BRANCH = "conversations";
    public static final String IMAGE_BRANCH = "images";

    private String senderID;
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
        ArrayList<String> users = (ArrayList<String>) conversation.get("users");
        chat.userNumber = users.indexOf(chat.senderID);//Arrays.asList(users).indexOf(chat.senderID);//.binarySearch(users, chat.senderID);

        //when chat opened, mark it as read
        chat.markAsRead();
        return chat;
    }

    private ChatController() {
        this.db = getDatabaseInstance();
        this.accountC = AccountController.getInstance();
    }

    /**
     * used to mark the conversation as read
     */
    public void markAsRead() {
        //TODO solve concurrent read receipts
        //get the current read table from the database
        db.collection(CONVERSATION_BRANCH).document(conversationID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    //update current user to read
                    DocumentSnapshot result = task.getResult();
                    ArrayList<Boolean> seen = (ArrayList<Boolean>) result.get("seen");
                    seen.set(userNumber, true);
                    Map<String, Object> data = new HashMap<>();
                    data.put("seen", seen);
                    db.collection(CONVERSATION_BRANCH).document(conversationID)
                            .set(data, SetOptions.merge());

                }
            }
        });

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

    public String getConversationID() {
        return conversationID;
    }
}
