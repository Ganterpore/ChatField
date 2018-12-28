package com.ganterpore.chatfield.L4_User_Interface;

import android.view.View;

import com.ganterpore.chatfield.Models.Contact;
import com.ganterpore.chatfield.L3_View_Fragments.ContactListFragment;
import com.ganterpore.chatfield.R;

import java.util.ArrayList;

public class ContactSelector extends ContactListFragment {
    private ArrayList<String> selectedContacts = new ArrayList<>();

    @Override
    public ContactListViewHolder createContactListViewHolder(View view) {
        return new ContactListSelectorHolder(view);
    }

    public ArrayList<String> getSelectedContacts() {
        return selectedContacts;
    }

    public class ContactListSelectorHolder extends ContactListFragment.ContactListViewHolder {
        private String contactID;

        /**
         * Public Constructor
         *
         * @param itemView The individual person record inside the recycler view
         */
        ContactListSelectorHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initialiseDisplay(Contact contact) {
            this.contactID = contact.getUserID();
            super.initialiseDisplay(contact);
        }

        @Override
        public void onClick(View v) {
            if(selectedContacts.contains(contactID)) {
                selectedContacts.remove(contactID);
                v.setBackgroundColor(getResources().getColor(R.color.colorPureWhite));
            } else {
                selectedContacts.add(contactID);
                v.setBackgroundColor(getResources().getColor(R.color.colorSelected));
            }
        }
    }
}
