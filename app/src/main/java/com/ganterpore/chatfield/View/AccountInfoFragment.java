package com.ganterpore.chatfield.View;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganterpore.chatfield.Controller.AccountController;
import com.ganterpore.chatfield.Model.Account;
import com.ganterpore.chatfield.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.app.Activity.RESULT_OK;

public class AccountInfoFragment extends Fragment {
    private static final String IS_CONTACT = "isContact";
    private static final String CONTACT_ID = "contactId";
    private static final String CONVERSATION_ID = "conversationId";

    private boolean isContact;
    private String contactId;
    private String conversationId;

    public static final int DETAILS_RESULT = 1;
    public static final int CAMERA_RESULT = 2;

    AccountController accountC;
    private boolean isHelper = false;

    private TextView username;
    private TextView bio;
    private TextView number;
    private TextView email;
    private TextView lastname;
    private TextView firstname;
    private TextView helperView;
    private ImageView profilePhoto;

    private OnFragmentInteractionListener mListener;

    public AccountInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AccountInfoFragment.
     */
    public static AccountInfoFragment viewContactInfo(String contactId, String conversationId) {
        AccountInfoFragment fragment = new AccountInfoFragment();
        Bundle args = new Bundle();
        args.putString(CONTACT_ID, contactId);
        args.putString(CONVERSATION_ID, conversationId);
        args.putBoolean(IS_CONTACT, true);
        fragment.setArguments(args);
        return fragment;
    }

    public static AccountInfoFragment viewSelfInfo() {
        AccountInfoFragment fragment = new AccountInfoFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_CONTACT, false);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isContact = getArguments().getBoolean(IS_CONTACT);
            if(isContact) {
                contactId = getArguments().getString(CONTACT_ID);
                conversationId = getArguments().getString(CONVERSATION_ID);
            }
        }

        accountC = AccountController.getInstance();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        firstname = view.findViewById(R.id.firstname_field);
        lastname = view.findViewById(R.id.lastname_field);
        email = view.findViewById(R.id.email_field);
        number = view.findViewById(R.id.number_field);
        bio = view.findViewById(R.id.bio_field);
        username = view.findViewById(R.id.username);

        Button logoutButton = view.findViewById(R.id.logout);
        Button updateButton = view.findViewById(R.id.update_details);
        Button messageButton = view.findViewById(R.id.send_message);

        profilePhoto = view.findViewById(R.id.profile);

        //setting up the view differently depending on if it is a contact or not
        if(!isContact) {
            profilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePhoto();
                }
            });
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logout();
                }
            });
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateDetails();
                }
            });
        } else {
            logoutButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.VISIBLE);

            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openChat();
                }
            });
        }

        //getting the user details in the background
        getUserDetails(this);
        //getting the profile photo in the background
        if(isContact) {
            updateProfilePhoto(profilePhoto, accountC, contactId);
        } else {
            updateProfilePhoto(profilePhoto, accountC);
        }

        return view;
    }

    private static void getUserDetails(final AccountInfoFragment activity) {
        new  AsyncTask<Void, Void, Account>() {
            @Override
            protected Account doInBackground(Void... voids) {
                if(activity.isContact) {
                    try {
                        return activity.accountC.getContactAccount(activity.contactId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return activity.accountC.getUserAccount();
            }

            @Override
            protected void onPostExecute(Account account) {
                activity.displayUserDetails(account);
            }
        }.execute();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if(isContact) {
//            if (id == R.id.message) {
//                openChat(false);
//                return true;
//            } else if (id == R.id.delete) {
//                accountC.deleteContact(contactID);
//                return true;
//            } else {
//                return false;
//            }
//        }
//        return false;
//    }

//    private void openChat(boolean needHelp) {
//        //open chat when clicked
//        Intent intent = new Intent(this, ChatActivity.class);
//        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
//        intent.putExtra(ChatActivity.CONTACT_ID, contactId);
//        intent.putExtra("needHelp", needHelp);
//        intent.putExtra("name", username.getText().toString());
//        startActivity(intent);
//    }

    private void updatePhoto() {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (photoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(photoIntent, CAMERA_RESULT);
//        }
    }

    private void displayUserDetails(Account account) {
        firstname.setText(account.getFirstname());
        lastname.setText(account.getLastname());
        email.setText(account.getEmail());
        bio.setText(account.getBio());
        String usernameString = account.getFirstname() + " " + account.getLastname();
        username.setText(usernameString);
    }

    public void updateDetails() {
        Intent intent = new Intent(this.getContext(), ConfigureUserActivity.class);
        intent.putExtra("firstname", firstname.getText());
        intent.putExtra("lastname", lastname.getText());
        intent.putExtra("number", number.getText());
        intent.putExtra("bio", bio.getText());
        intent.putExtra("helper", isHelper);
        startActivityForResult(intent, DETAILS_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case DETAILS_RESULT:
                //after user details are updated, update them in the db
                accountC.updateUser(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        data.getStringExtra("firstname"), data.getStringExtra("lastname"),
                        accountC.getUserAccount().getEmail(), data.getStringExtra("bio"));
                displayUserDetails(accountC.getUserAccount());
                break;
            case CAMERA_RESULT:
                //after a photo is taken, update the profile photo
                Bundle extras = data.getExtras();
                //don't need the full size photo, just a thumbnail
                Bitmap photoThumbnail = (Bitmap) extras.get("data");
                profilePhoto.setImageBitmap(photoThumbnail);
                accountC.setProfile(photoThumbnail);
                break;
        }
    }

    public void logout() {
        mListener.logout();
    }

    /**
     * updates profile in the background
     * static to avoid leaks
     * @param profilePhoto, the ImageView to update to photo
     * @param accountC, the AccountController being used
     */
    private static void updateProfilePhoto(ImageView profilePhoto, AccountController accountC) {
        updateProfilePhoto(profilePhoto, accountC, null);
    }

    private static void updateProfilePhoto(final ImageView profilePhoto, final AccountController accountC,
                                           final String contactID) {
        new  AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    if(contactID != null) {
                        return accountC.getContactProfile(contactID);
                    }
                    return accountC.getProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap photo) {
                if(photo==null) {
                    photo = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_notifications_black_24dp);
                }
                profilePhoto.setImageBitmap(photo);
            }
        }.execute();
    }

    public void openChat() {
        mListener.onChatSelected(contactId, conversationId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onChatSelected(String contactID, String conversationID);
        void logout();
    }
}
