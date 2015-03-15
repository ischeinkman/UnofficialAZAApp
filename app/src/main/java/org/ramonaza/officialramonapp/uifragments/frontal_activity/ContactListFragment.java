package org.ramonaza.officialramonapp.uifragments.frontal_activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.ramonaza.officialramonapp.R;
import org.ramonaza.officialramonapp.activities.ContactDataActivity;
import org.ramonaza.officialramonapp.activities.FrontalActivity;
import org.ramonaza.officialramonapp.datafiles.condrive_database.ConDriveDatabaseContract;
import org.ramonaza.officialramonapp.datafiles.condrive_database.ConDriveDatabaseHelper;
import org.ramonaza.officialramonapp.datafiles.condrive_database.ContactInfoWrapper;
import org.ramonaza.officialramonapp.datafiles.condrive_database.ContactInfoWrapperGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilan Scheinkman on 1/12/15.
 */
public class ContactListFragment  extends Fragment{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String EXTRA_CONTRUCTION_INFO="org.ramonaza.officialramonapp.ALEPH_ID";
    private static final String EXTRA_LAYER="org.ramonaza.officialramonapp.LAYER_NAME";
    private static final String PAGE_NAME="Contact List";
    public int fraglayer;

    public static ContactListFragment newInstance(int sectionNumber) {
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.fraglayer=sectionNumber;
        return fragment;
    }

    public ContactListFragment() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle("Contact List");
        View rootView = inflater.inflate(R.layout.fragment_contact_list, container, false);
        LinearLayout cLayout=(LinearLayout) rootView.findViewById(R.id.cListLinearList);
        ProgressBar pBar=(ProgressBar) rootView.findViewById(R.id.ContactListProgress);
        new getContactsTask(pBar,rootView,cLayout).execute();
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((FrontalActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }



    public class ButtonClickListener implements View.OnClickListener {
        ContactInfoWrapper buttonContactInfoWrapper;

        public ButtonClickListener setContact(ContactInfoWrapper inContactInfoWrapper) {
            this.buttonContactInfoWrapper = inContactInfoWrapper;
            return this;
        }

        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ContactDataActivity.class);
            intent.putExtra(EXTRA_LAYER, PAGE_NAME);
            intent.putExtra(EXTRA_CONTRUCTION_INFO, this.buttonContactInfoWrapper.getId());
            Log.d("ContactListFrag",""+this.buttonContactInfoWrapper.getId());
            startActivity(intent);
        }
    }
    public class getContactsTask extends AsyncTask<Void,Integer,List<ContactInfoWrapper>>{

        private  ProgressBar bar;
        private  View rootView;
        private LinearLayout cLayout;

        @Override
        protected List<ContactInfoWrapper> doInBackground(Void... params) {
            ConDriveDatabaseHelper dbHelpter=new ConDriveDatabaseHelper(getActivity().getApplicationContext());
            SQLiteDatabase db=dbHelpter.getReadableDatabase();
            Cursor cursor=db.query(ConDriveDatabaseContract.ContactListTable.TABLE_NAME,null,null,null,null,null,ConDriveDatabaseContract.ContactListTable.COLUMN_NAME+" ASC");
            return ContactInfoWrapperGenerator.fromDataBase(cursor);
        }

        @Override
        protected void onPostExecute(List<ContactInfoWrapper> alephs) {
            super.onPostExecute(alephs);
            List<Button> contactButtons=new ArrayList<Button>();
            for(ContactInfoWrapper aleph: alephs){
                Button temp=new Button(getActivity());
                temp.setBackground(getResources().getDrawable(R.drawable.songbuttonlayout));
                temp.setText(aleph.getName());
                ButtonClickListener buttonClickListener=new ButtonClickListener();
                buttonClickListener.setContact(aleph);
                temp.setOnClickListener(buttonClickListener);
                contactButtons.add(temp);
            }
            for(Button cButton:contactButtons){
                cLayout.addView(cButton);
            }
            bar.setVisibility(View.GONE);

        }
        public getContactsTask(ProgressBar progressBar, View inView, LinearLayout linearLayout){
            bar=progressBar;
            rootView=inView;
            cLayout=linearLayout;
        }
    }

}

