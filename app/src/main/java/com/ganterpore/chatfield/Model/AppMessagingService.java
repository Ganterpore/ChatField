package com.ganterpore.chatfield.Model;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppMessagingService extends FirebaseMessagingService {

    public static final String DEVICE_TOKEN_BRANCH = "tokens";
    public static final String TOKEN_FIELD = "token";

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NotificationManager mManager = (NotificationManager) this.getSystemService(Activity.NOTIFICATION_SERVICE);
        assert (remoteMessage.getNotification() != null && mManager != null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(mManager);
        }

        Notification notification = new NotificationCompat.Builder(this, "G")
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        mManager.notify(123, notification);
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        NotificationChannel mChannel = new NotificationChannel(
                "G", "general", NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription("General notifications for the app");
        notificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }


    /**
     * used to send a notification to a specific userID.
     * @param JSONNotification, a JSON object for the notification.
     *                          example:
    JSONObject notification = new JSONObject();
    JSONObject notif = new JSONObject();
    JSONObject data = new JSONObject();
    try {
    notif.put("title", "Notification title");
    notif.put("body", "Notification message");

    data.put("senderID", userID);

    notification.put("notification", notif);
    notification.put("data", data);
    } catch (JSONException e) {
    e.printStackTrace();
    }
     * @param userID, the ID of the user you wish to send a notification to
     */
    public static void sendNotificationToUser(JSONObject JSONNotification, String userID) {
        Map<String, Object> data = new HashMap<>();
        data.put("notification", JSONNotification);
        data.put("userID", userID);
        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendNotificationToUser").call(data);
    }

    /**
     * used to send a message to the server requesting a contact to be added.
     * Contact will be informed with a notification
     * @param firstname of the user sending the request
     * @param lastname  of the user sending the request
     * @param email of the user sending the request
     * @param receiverEmail of the contact being added
     */
    public static void addContactMessage(String firstname, String lastname, String email, String receiverEmail) {
        //TODO dont send request if already friends
        Map<String, String> data = new HashMap<>();
        data.put("firstname", firstname);
        data.put("lastname", lastname);
        data.put("email", email);
        data.put("receiverEmail", receiverEmail);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("addContact").call(data);

    }

    /**
     * used to send a message to the server responding to a contact request
     * @param contactID of the person you are accepting
     */
    public static void acceptContactMessage(String contactID) {
        Map<String, String> data = new HashMap<>();
        data.put("contactID", contactID);

        FirebaseFunctions.getInstance().getHttpsCallable("acceptContact").call(data);
    }

    @Override
    public void onNewToken(String token) {
        addToken(token);
    }

    /**
     *used to add an application token to the database. this is used to send notifications to an
     * application instance
     * @param token of the app instance to add to the db
     */
    public static void addToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = AccountController.getInstance().getUid();
        DocumentReference tokens = db.collection(DEVICE_TOKEN_BRANCH).document(uid);
        Map<String, Object> data = new HashMap<>();
        data.put(TOKEN_FIELD, token);
        tokens.set(data);
    }
}
