package com.ganterpore.chatfield.L2_Controllers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ganterpore.chatfield.Models.Account;
import com.ganterpore.chatfield.L1_Database_Interface.AppMessagingService;
import com.ganterpore.chatfield.Models.Chat;
import com.ganterpore.chatfield.Models.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

public class AccountController {
    private static AccountController controller;
    private Account userAccount;
    private FirebaseFirestore db;
    public static final String USER_BRANCH = "user";

    private String uid;

    public static FirebaseFirestore getDatabaseInstance() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseAuth getAuthoriserInstance() {
        return FirebaseAuth.getInstance();
    }

    /////////////// Sign in/out methods //////////////////////////
    /**
     * Used for the initial signing up of a user. The firstname, lastname, email and password for
     * them is given. Will return a task which is running the authorisation.
     * @return a task that is authorising the user.
     */
    public static Task<AuthResult> signup(final String firstname, final String lastname,
                                          final String bio, final String email, String password) {
        FirebaseAuth authoriser = getAuthoriserInstance();
        //signing up user
        Task<AuthResult> accountCreation = authoriser.createUserWithEmailAndPassword(email,password);

        //adding user details to the database if the account is successfully authenticated
        controller = new AccountController();
        controller.handleNewUser(accountCreation, firstname, lastname, email, bio);

        return accountCreation;
    }



    /**
     * used for logging in a user who has already created an account
     * @param email,    the email used when setting up the account
     * @param password, the users password
     * @return an associated task that is signing in a user
     */
    public static Task<AuthResult> login(String email, String password) {
        FirebaseAuth authoriser = getAuthoriserInstance();
        return authoriser.signInWithEmailAndPassword(email, password);
    }

    /**
     * gets a current instance of the account controller.
     * Will also add assosciated userAccount with the instance
     * Will return null if no user is logged on. Check with isLoggedOn().
     * @return the current AccountController.
     */
    public static AccountController getInstance() {
        FirebaseUser currUser = getAuthoriserInstance().getCurrentUser();
        if (currUser == null) {
            Log.d(TAG, "getInstance: Warning, no logged in user. instance is null.");
            //if there is no logged in user, the controller will be null
            controller = null;
        } else if(controller == null){
            //if there is currently no controller but a user is signed in, create one and assign the user
            controller = new AccountController();
            controller.updateToken(currUser);
        } else if(controller.userAccount == null) {
            controller.updateToken(currUser);
            //if there is a controller, but the user account is not yet assigned, start assigning it
            controller.assignUser(currUser.getUid());
        }
        return controller;
    }

    /**
     * used to logout a user who is currently logged on.
     */
    public void logout() {
        //if it is already logged out, do nothing
        if(!isLoggedOn()) {
            return;
        }
        FirebaseAuth authoriser = getAuthoriserInstance();
        authoriser.signOut();
        userAccount = null;

    }

    /**
     * constructer for the account controller. simply sets up the db and assigns a user.
     */
    private AccountController() {
        db = getDatabaseInstance();
        FirebaseUser user = getAuthoriserInstance().getCurrentUser();

        //if logged in, assign user, otherwise add warning to log
        if(isLoggedOn() && user!=null) {
            uid = user.getUid();
            assignUser(uid);
        } else {
            Log.d(TAG, "AccountController: Warning, AccountController created without logged in user," +
                    "this could lead to future Exceptions");
        }
    }

    /////////////////helper functions /////////////////////////
    /**
     * checks whether there is a user currently logged onto the device.
     * If there is no user they must either sign in or sign up before accessing the AccountController
     * @return boolean whether logged in or not
     */
    public static boolean isLoggedOn() {
        return getAuthoriserInstance().getCurrentUser() != null;
    }

    /**
     * Adds a new user to the database once an account creation task is finished
     * @param accountCreation, the task authorising an account signup
     * @param firstname, the account owners first name
     * @param lastname, the account owners last name
     * @param email, the account owners email
     */
    private void handleNewUser(Task<AuthResult> accountCreation, final String firstname,
                               final String lastname, final String email, final String bio) {
        accountCreation.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete (@NonNull Task <AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    controller.updateUser(uid, firstname, lastname, email, bio);
                }
            }
        });
    }

    /**
     * adds/updates a user in the firestore database
     * will also assign the user to the userAccount field
     */
    public void updateUser(String uid, String firstname, String lastname, String email, String bio) {
        //creating account
        Account user = new Account(uid, firstname, lastname, email, bio);

        //adding account to database, and setting the userAccount variable
        DocumentReference userRow = db.collection(USER_BRANCH).document(uid);
        userRow.set(user);

        this.userAccount = user;
    }

    /**
     * assign a user to the userAccount variable. May not become immediately available due to a
     * database call.
     * @param uid, the userID of the account to assign.
     * @return the task that is getting the user
     */
    private Task<DocumentSnapshot> assignUser(String uid) {
        Log.w("A", "userId is: " + uid);
        //Task gets the user from the database
        Task<DocumentSnapshot> getUser = db.collection(USER_BRANCH).document(uid).get();
        //when user is taken from the database, assign to the user
        getUser.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //creating account and assigning
                    userAccount = new Account(task.getResult());
                } else {
                    Log.w("A", "Error finding user", task.getException());
                }
            }
        });
        return getUser;
    }

    /**
     * if there is no application token for the current user, or it is different to the current
     * application token, set one up so that the user can be notified
     * @param user, the user to check for
     */
    private void updateToken(final FirebaseUser user) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                //get the current application token from the device
                final String token = task.getResult().getToken();
                //request the current token on the databse
                db.collection(AppMessagingService.DEVICE_TOKEN_BRANCH).document(user.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    //if the document doesn't exist, there is no token contained, or it is
                                    // different, then update it
                                    if(!doc.exists()
                                            || doc.get("token")==null
                                            || !token.equals(doc.get("token"))) {
                                        AppMessagingService.addToken(token);
                                    }
                                }
                            }
                        });
            }
        });

    }

    ///////////////////////// Getters //////////////////////////
    /**
     * Get the assosciated userAccount with the AccountController
     * If the user account is not available, it will block the task until the data is returned from
     * the database. Therefore it is only recommended to run this method asynchrously from the
     * main (UI) thread.
     * @return userAccount or null if the data is not accessed (no internet etc).
     */
    public Account getUserAccount() {
        if(userAccount == null && isLoggedOn()) {
            //if user account not currently set up, get from database
            try {
                Task<DocumentSnapshot> userCall = assignUser(uid);
                //blocking the system until user found
                Tasks.await(userCall);
                return new Account(userCall.getResult());
            } catch(Exception e) {
                //if the user is not found, return null
                return null;
            }
        }
        Log.d(TAG, "getUserAccount: returning account for "+userAccount.getFirstname());
        return userAccount;
    }

    //For if we need a specific user created...
    public Account getContactAccount(String my_user) {
        if(isLoggedOn() && my_user.equals(uid)) {
            return getUserAccount();
        } else {
            try {
                Task<DocumentSnapshot> userCall = db.collection(USER_BRANCH).document(my_user).get();
                Tasks.await(userCall);
                return new Account(userCall.getResult());
            } catch(Exception e) {
                //if the user is not found, return null
                return null;
            }
        }
    }

    public UploadTask setProfile(Bitmap photo) {
        StorageReference picDest = FirebaseStorage.getInstance().getReference()
                .child("UserProfiles/"+uid+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        return picDest.putBytes(data);
    }

    public Bitmap getProfile() throws ExecutionException, InterruptedException {
        return getContactProfile(uid);
    }

    public Bitmap getContactProfile(String contactID) throws ExecutionException, InterruptedException {
        StorageReference picDest = FirebaseStorage.getInstance().getReference()
                .child("UserProfiles/"+contactID+".jpg");

        int one_megabyte = 1024*1024; // enough memory to handle the profile pictures (thumbnails)
        Task<byte[]> picBytesTask = picDest.getBytes(one_megabyte);
        Tasks.await(picBytesTask);
        byte[] picBytes = picBytesTask.getResult();

        return BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
    }

    ///////////////////////contact functions ////////////////////////////////////

    public static final String CONTACT_BRANCH = "contacts";
    public static final String REQUEST_BRANCH = "requests";
    public static final String CONVERSATIONS_BRANCH = "conversations";

    public Query getConversations() {
        Query conversations = db
                .collection(USER_BRANCH)
                .document(uid)
                .collection(CONVERSATIONS_BRANCH)
                .orderBy("lastMessageSentAt", Query.Direction.DESCENDING);
        //TODO update conversation last sent at and other things when method is called.
        updateConversations(conversations);
        return conversations;
    }

    private void updateConversations(Query conversations) {
        conversations.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for(DocumentSnapshot localConvDoc : documents) {
                    final Chat localConv = new Chat(localConvDoc);

                    //getting seen and name from dataabase
                    db.collection(ChatController.CONVERSATION_BRANCH)
                        .document(localConv.getConversationID()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot onlineConvDoc = task.getResult();
                                ArrayList<String> users = (ArrayList<String>) onlineConvDoc.get("users");
                                ArrayList<Boolean> allSeen  = (ArrayList<Boolean>) onlineConvDoc.get("seen");

                                int index = users.indexOf(uid);
                                boolean seen = allSeen.get(index);
                                if(localConv.isSeen() != seen) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("seen", seen);
                                    db.collection(USER_BRANCH).document(uid).collection(CONVERSATIONS_BRANCH)
                                            .document(localConv.getConversationID()).set(data, SetOptions.merge());

                                }

                                //if its a group, get the name from the conversation
                                //if its a contact, get it from contacts
                                if(localConv.getType().equals("group")) {
                                    String name = (String) onlineConvDoc.get("name");
                                    if(!localConv.getName().equals(name)) {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("name", name);
                                        db.collection(USER_BRANCH).document(uid).collection(CONVERSATIONS_BRANCH)
                                                .document(localConv.getConversationID()).set(data, SetOptions.merge());
                                    }
                                } else if (localConv.getType().equals("contact")) {
                                    db.collection(USER_BRANCH).document(uid)
                                        .collection(CONTACT_BRANCH)
                                        .whereEqualTo("conversationID",
                                                localConv.getConversationID()).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                DocumentSnapshot result = task.getResult().getDocuments().get(0);
                                                String name = result.get("firstname") + " " + result.get("lastname");
                                                if(!localConv.getName().equals(name)) {
                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("name", name);
                                                    db.collection(USER_BRANCH).document(uid).collection(CONVERSATIONS_BRANCH)
                                                            .document(localConv.getConversationID()).set(data, SetOptions.merge());
                                                }
                                            }
                                        });
                                }
                            }
                       });

                    //getting most recent message
                    db.collection(ChatController.CONVERSATION_BRANCH)
                            .document(localConv.getConversationID())
                            .collection(ChatController.MESSAGE_BRANCH)
                            .orderBy("sentAt", Query.Direction.DESCENDING).limit(1).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.getResult().getDocuments().size() > 0) {
                                        long mostRecent = (long) task.getResult().getDocuments().get(0).get("sentAt");
                                        if (localConv.getLastMessageSentAt() != mostRecent) {
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("lastMessageSentAt", mostRecent);
                                            db.collection(USER_BRANCH).document(uid).collection(CONVERSATIONS_BRANCH)
                                                    .document(localConv.getConversationID()).set(data, SetOptions.merge());
                                        }
                                    }
                                }
                            });
                    }
                }
        });
    }

    /**
     * returns a query to the list of contacts associated with the current user.
     * A Query can be used to fill a recycler view.
     * For usage example, See https://github.com/ennur/FirestoreRecyclerAdapterSample
     * @return a query for the contacts
     */
    public Query getContacts() {
        Query contacts = db
                .collection(USER_BRANCH)
                .document(uid)
                .collection(CONTACT_BRANCH)
                .orderBy("lastname");
//        cleanContacts(contacts);
        return contacts;
    }

    /**
     * makes sure the current contact details matches the details of the user account on the database.
     * @param contacts a query containing all the contacts to clean.
     */
    private void cleanContacts(Query contacts) {
        contacts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    for(DocumentSnapshot doc : documents) {
                        String contactID = (String) doc.get("userID");
                        Account contactDetails = getContactAccount(contactID);
                        if(!contactDetails.getFirstname().equals(doc.get("firstname"))
                                || !contactDetails.getLastname().equals(doc.get("lastname"))) {
                            updateContactDetails(contactID, contactDetails.getFirstname(),
                                    contactDetails.getLastname());
                        }
                    }
                }
            }
        });
    }

    /**
     * get a query representing the contact requests sent to the current logged on user
     * @return a Query representing requests
     */
    public Query getRequests() {
        return db.collection(USER_BRANCH).document(uid)
                .collection(REQUEST_BRANCH).orderBy("lastname");
    }

    /**
     * sends a contact request to a user with the given email
     * @param email, email of the contact
     */
    public void requestContact(String email) {
        Account account = getUserAccount();
        AppMessagingService.addContactMessage(account.getFirstname(), account.getLastname(),
                account.getEmail(), email);
    }

    /**
     * accepts a contact request from another user
     * @param contactID, the ID of the user to accept
     */
    public void acceptContact(String contactID) {
        AppMessagingService.acceptContactMessage(contactID);
    }

    /**
     * declines a contact request from another user
     * @param contactID, the id of the user to reject
     */
    public void declineContact(String contactID) {
        db.collection(USER_BRANCH).document(uid).collection(REQUEST_BRANCH).document(contactID).delete();
    }

    /**
     * used to remove a contact from a users contact list
     * @param contactId, the userID of the contact to delete
     */
    public void deleteContact(String contactId) {
        //finding contact with given user ID
        db.collection(USER_BRANCH).document(uid).collection(CONTACT_BRANCH)
                .whereEqualTo("userID", contactId).get()
                //when user found, delete them from collection
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //deleting contact
                                String docID = document.getId();
                                db.collection(USER_BRANCH).document(uid).collection(CONTACT_BRANCH)
                                        .document(docID).delete();
                            }
                        } else {
                            Log.w(TAG, "Error finding contact with given UID.", task.getException());
                        }
                    }
                });
    }

    private void updateContactDetails(String contactID, String firstname, String lastname) {
        Map<String, Object> data = new HashMap<>();
        data.put("firstname", firstname);
        data.put("lastname", lastname);
        db.collection(USER_BRANCH).document(uid).collection(CONTACT_BRANCH).document(contactID)
                .set(data, SetOptions.merge());
    }

    public Contact getContact(String contactID) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> documentTask = db
                .collection(USER_BRANCH)
                .document(uid)
                .collection(CONTACT_BRANCH)
                .document(contactID)
                .get();

        Tasks.await(documentTask);
        return new Contact(
                documentTask.getResult()
        );
    }

    /**
     * gets the number of unread messages the account has assosciated with is
     * @return the number of unread chats
     * @throws ExecutionException when something goes wrong in execution
     * @throws InterruptedException when the process is interrupted
     */
    public int getUnreadMessages() throws ExecutionException, InterruptedException {
        int unreadMessages = 0;

        //getting the list of contacts
        Task<QuerySnapshot> allContactsTask = getContacts().get();
        Tasks.await(allContactsTask);
        if(!allContactsTask.isSuccessful()) {
            return 0;
        }
        //for each contact, check if their conversation has been read or not
        List<DocumentSnapshot> documents = allContactsTask.getResult().getDocuments();
        for(DocumentSnapshot doc : documents) {
            //getting the conversation
            String conversationID = doc.getString("conversationID");
            if(conversationID==null) {
                //if there is no conversationID, there cant be an unread message
                continue;
            }
            Task<DocumentSnapshot> getConversationTask = db
                    .collection(ChatController.CONVERSATION_BRANCH).document(conversationID).get();
            Tasks.await(getConversationTask);

            //checking if the current user is up to date on the conversation
            if(getConversationTask.isSuccessful()) {
                DocumentSnapshot conversation = getConversationTask.getResult();
                if(uid.equals(conversation.get("user1ID"))) {
                    Boolean user1Seen = conversation.getBoolean("user1Seen");
                    if(user1Seen==null || !user1Seen) {
                        unreadMessages++;
                    }
                } else {
                    Boolean user2Seen = conversation.getBoolean("user2Seen");
                    if(user2Seen==null || !user2Seen) {
                        unreadMessages++;
                    }
                }
            }
        }
        return unreadMessages;
    }

    public String getUid() {
        return uid;
    }
}
