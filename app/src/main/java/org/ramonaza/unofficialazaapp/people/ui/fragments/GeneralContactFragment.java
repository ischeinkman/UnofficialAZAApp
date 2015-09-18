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
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHelper;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;

/**
 * Created by Ilan Scheinkman on 1/13/15.
 */
public class GeneralContactFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ContactInfoWrapper aleph;
    private SharedPreferences sp;

    public static GeneralContactFragment newInstance(int sectionNumber, ContactInfoWrapper aleph) {
        GeneralContactFragment fragment = new GeneralContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setAlpeh(aleph);
        fragment.setArguments(args);
        return fragment;
    }
    public void setAlpeh(ContactInfoWrapper aleph){
        this.aleph=aleph;
    }
    protected void refreshInfoView(TextView inView){
        String infoDump=String.format("N" +
                "ame:   %s\nGrade:   %s\nSchool:  %s\nAddress:   %s\nEmail:  %s\nPhone:   %s\n",aleph.getName(), aleph.getYear(), aleph.getSchool(),aleph.getAddress(),aleph.getEmail(),aleph.getPhoneNumber());

        if(sp.getBoolean("admin",false)){
            infoDump+="ID: "+aleph.getId()+"\n";
            infoDump+="Lat: "+aleph.getLatitude()+"\n";
            infoDump+="Long: "+aleph.getLongitude()+"\n";
            if(aleph.isPresent()){
                infoDump+="RIDES: PRESENT";
            }
            else{
                infoDump+="RIDES: ABSENT";
            }
        }
        inView.setText(infoDump);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(this.aleph.getName());
        View rootView=inflater.inflate(R.layout.fragment_contact_data,container,false);
        LinearLayout rootLayout=(LinearLayout) rootView.findViewById(R.id.cPageLayout);
        final TextView information=(TextView) rootView.findViewById(R.id.ContactInfoView);
        information.setTextSize(22);
        sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
        refreshInfoView(information);


        Button callButton=(Button) rootView.findViewById(R.id.ContactCallButton);
        callButton.setOnClickListener(new CallButtonListener(this.aleph));

        Button textButton=(Button) rootView.findViewById(R.id.ContactTextButton);
        textButton.setOnClickListener(new TextButtonListener(this.aleph));

        Button emailButton=(Button) rootView.findViewById(R.id.ContactEmailButton);
        emailButton.setOnClickListener(new EmailButtonListener(this.aleph));

        Button addContactButton=(Button) rootView.findViewById(R.id.ContactAddButton);
        addContactButton.setOnClickListener(new AddContactButtonListener(this.aleph));

        Button navButton=(Button) rootView.findViewById(R.id.ContactDirButton);
        navButton.setOnClickListener(new NavigatorButtonListener(this.aleph));

        if(sp.getBoolean("admin",false)){
            Button presentSwitch=new Button(getActivity());
            presentSwitch.setText("Set Present");
            presentSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDatabaseHelper dbH=new ContactDatabaseHelper(getActivity());
                    SQLiteDatabase db=dbH.getWritableDatabase();
                    ContentValues cValues=new ContentValues();
                    if (aleph.isPresent()) {
                        aleph.setPresent(false);
                        cValues.put(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT, 0);
                    }
                    else{
                        aleph.setPresent(true);
                        cValues.put(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT, 1);
                    }
                    refreshInfoView(information);
                    db.update(ContactDatabaseContract.ContactListTable.TABLE_NAME,cValues, ContactDatabaseContract.ContactListTable._ID+"=?",new String[]{""+aleph.getId()});
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

    public class CallButtonListener implements View.OnClickListener{
        ContactInfoWrapper contactInfoWrapperAleph;
        public CallButtonListener(ContactInfoWrapper inAleph){
            this.contactInfoWrapperAleph =inAleph;
        }
        public void onClick(View v){
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactInfoWrapperAleph.getPhoneNumber()));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.d("Calling Phone Number: "+ contactInfoWrapperAleph.getPhoneNumber(), "Call failed", activityException);
            }
        }
    }

    public class EmailButtonListener implements View.OnClickListener{
        ContactInfoWrapper contactInfoWrapperAleph;
        public EmailButtonListener(ContactInfoWrapper inAleph){
            this.contactInfoWrapperAleph =inAleph;
        }
        public void onClick(View v){
            try {
                Intent emailIntent=new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{this.contactInfoWrapperAleph.getEmail()});
                startActivity(Intent.createChooser(emailIntent,"Email using:"));
            }catch (ActivityNotFoundException activityException) {
                Log.d("Emailing:: "+ contactInfoWrapperAleph.getEmail(), "Email failed", activityException);
            }
        }
    }

    public class AddContactButtonListener implements View.OnClickListener{
        ContactInfoWrapper contactInfoWrapperAleph;
        public AddContactButtonListener(ContactInfoWrapper inAleph){
            this.contactInfoWrapperAleph =inAleph;
        }
        public void onClick(View v){
            try {
                Intent contactIntent=new Intent(ContactsContract.Intents.Insert.ACTION);
                contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, contactInfoWrapperAleph.getName());
                contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, contactInfoWrapperAleph.getPhoneNumber());
                contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, contactInfoWrapperAleph.getEmail());
                contactIntent.putExtra(ContactsContract.Intents.Insert.POSTAL, contactInfoWrapperAleph.getAddress());
                startActivity(contactIntent);
            }catch (ActivityNotFoundException activityException) {
                Log.d("Adding Contact: "+ contactInfoWrapperAleph.getName(), "Failed", activityException);
            }
        }
    }

    public class NavigatorButtonListener implements View.OnClickListener{
        ContactInfoWrapper contactInfoWrapperAleph;
        public NavigatorButtonListener(ContactInfoWrapper inAleph){
            this.contactInfoWrapperAleph =inAleph;
        }
        public void onClick(View v){
            try {
                if(aleph.getAddress().equals("Not Submitted")){
                    Toast.makeText(getActivity(), "Address Not Submitted", Toast.LENGTH_SHORT).show();
                }
                else {
                    String uri = String.format("google.navigation:q=%s", contactInfoWrapperAleph.getAddress().replace(" ", "+"));
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(navIntent);
                }
            }catch (ActivityNotFoundException activityException){
                Log.d("Directions to:"+ contactInfoWrapperAleph.getAddress(), "Failed", activityException);
            }
        }
    }

    public class TextButtonListener implements View.OnClickListener{
        ContactInfoWrapper contactInfoWrapperAleph;
        public TextButtonListener(ContactInfoWrapper inAleph){
            this.contactInfoWrapperAleph =inAleph;
        }
        public void onClick(View v){
            try {
                Intent textIntent=new Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",this.contactInfoWrapperAleph.getPhoneNumber(),null));
                startActivity(textIntent);
            }catch (ActivityNotFoundException activityException){
                Log.d("Directions to:"+ contactInfoWrapperAleph.getAddress(), "Failed", activityException);
            }
        }
    }

}
