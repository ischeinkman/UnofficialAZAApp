package org.ramonaza.unofficialazaapp.people.ui.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonazaapi.contacts.ContactInfoWrapper;

/**
 * Created by Ilan Scheinkman on 1/13/15.
 */
public class GeneralContactFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ContactInfoWrapper mContact;
    private SharedPreferences sp;

    public static GeneralContactFragment newInstance(int sectionNumber, ContactInfoWrapper contact) {
        GeneralContactFragment fragment = new GeneralContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setContact(contact);
        fragment.setArguments(args);
        return fragment;
    }

    public void setContact(ContactInfoWrapper contact) {
        this.mContact = contact;
    }

    protected void refreshInfoView(TextView inView) {
        String infoDump = String.format("N" +
                "ame:   %s\nGrade:   %s\nSchool:  %s\nAddress:   %s\nEmail:  %s\nPhone:   %s\n", mContact.getName(), mContact.getYear(), mContact.getSchool(), mContact.getAddress(), mContact.getEmail(), mContact.getPhoneNumber());

        if (sp.getBoolean("admin", false)) {
            infoDump += "ID: " + mContact.getId() + "\n";
            infoDump += "Lat: " + mContact.getLatitude() + "\n";
            infoDump += "Long: " + mContact.getLongitude() + "\n";
            if (mContact.isPresent()) {
                infoDump += "RIDES: PRESENT";
            } else {
                infoDump += "RIDES: ABSENT";
            }
        }
        inView.setText(infoDump);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(this.mContact.getName());
        View rootView = inflater.inflate(R.layout.fragment_contact_data, container, false);
        LinearLayout rootLayout = (LinearLayout) rootView.findViewById(R.id.cPageLayout);
        final TextView information = (TextView) rootView.findViewById(R.id.ContactInfoView);
        information.setTextSize(22);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        refreshInfoView(information);


        Button callButton = (Button) rootView.findViewById(R.id.ContactCallButton);
        callButton.setOnClickListener(new CallButtonListener(this.mContact));

        Button textButton = (Button) rootView.findViewById(R.id.ContactTextButton);
        textButton.setOnClickListener(new TextButtonListener(this.mContact));

        Button emailButton = (Button) rootView.findViewById(R.id.ContactEmailButton);
        emailButton.setOnClickListener(new EmailButtonListener(this.mContact));

        Button addContactButton = (Button) rootView.findViewById(R.id.ContactAddButton);
        addContactButton.setOnClickListener(new AddContactButtonListener(this.mContact));

        Button navButton = (Button) rootView.findViewById(R.id.ContactDirButton);
        navButton.setOnClickListener(new NavigatorButtonListener(this.mContact));

        if (sp.getBoolean("admin", false)) {
            Button presentSwitch = new Button(getActivity());
            presentSwitch.setText("Set Present");
            presentSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppDatabaseHelper dbH = new AppDatabaseHelper(getActivity());
                    SQLiteDatabase db = dbH.getWritableDatabase();
                    ContentValues cValues = new ContentValues();
                    if (mContact.isPresent()) {
                        mContact.setPresent(false);
                        cValues.put(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, 0);
                    } else {
                        mContact.setPresent(true);
                        cValues.put(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, 1);
                    }
                    refreshInfoView(information);
                    db.update(AppDatabaseContract.ContactListTable.TABLE_NAME, cValues, AppDatabaseContract.ContactListTable._ID + "=?", new String[]{"" + mContact.getId()});
                }
            });
            rootLayout.addView(presentSwitch);

        }

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public class CallButtonListener implements View.OnClickListener {
        ContactInfoWrapper mContact;

        public CallButtonListener(ContactInfoWrapper contact) {
            this.mContact = contact;
        }

        public void onClick(View v) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mContact.getPhoneNumber()));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("Calling Phone Number: " + mContact.getPhoneNumber(), "Call failed", activityException);
            }
        }
    }

    public class EmailButtonListener implements View.OnClickListener {
        ContactInfoWrapper mContact;

        public EmailButtonListener(ContactInfoWrapper contact) {
            this.mContact = contact;
        }

        public void onClick(View v) {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{this.mContact.getEmail()});
                startActivity(Intent.createChooser(emailIntent, "Email using:"));
            } catch (ActivityNotFoundException activityException) {
                Log.d("Emailing:: " + mContact.getEmail(), "Email failed", activityException);
            }
        }
    }

    public class AddContactButtonListener implements View.OnClickListener {
        ContactInfoWrapper mContact;

        public AddContactButtonListener(ContactInfoWrapper contact) {
            this.mContact = contact;
        }

        public void onClick(View v) {
            try {
                Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, mContact.getName());
                contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mContact.getPhoneNumber());
                contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, mContact.getEmail());
                contactIntent.putExtra(ContactsContract.Intents.Insert.POSTAL, mContact.getAddress());
                startActivity(contactIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("Adding Contact: " + mContact.getName(), "Failed", activityException);
            }
        }
    }

    public class NavigatorButtonListener implements View.OnClickListener {
        ContactInfoWrapper mContact;

        public NavigatorButtonListener(ContactInfoWrapper contact) {
            this.mContact = contact;
        }

        public void onClick(View v) {
            try {
                if (GeneralContactFragment.this.mContact.getAddress().equals("Not Submitted")) {
                    Toast.makeText(getActivity(), "Address Not Submitted", Toast.LENGTH_SHORT).show();
                } else {
                    String uri = String.format("google.navigation:q=%s", mContact.getAddress().replace(" ", "+"));
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(navIntent);
                }
            } catch (ActivityNotFoundException activityException) {
                Log.d("Directions to:" + mContact.getAddress(), "Failed", activityException);
            }
        }
    }

    public class TextButtonListener implements View.OnClickListener {
        ContactInfoWrapper mContact;

        public TextButtonListener(ContactInfoWrapper contact) {
            this.mContact = contact;
        }

        public void onClick(View v) {
            try {
                Intent textIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", this.mContact.getPhoneNumber(), null));
                startActivity(textIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("Directions to:" + mContact.getAddress(), "Failed", activityException);
            }
        }
    }

}
