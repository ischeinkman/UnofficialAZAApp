package org.ramonaza.unofficialazaapp.people.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHelper;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.ui.activities.AddCustomAlephActivity;
import org.ramonaza.unofficialazaapp.people.ui.activities.ContactDataActivity;

/**
 * Created by Ilan Scheinkman on 1/12/15.
 */
public class ContactListFragment  extends InfoWrapperTextListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_list,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_custom_aleph:
                Intent intent=new Intent(getActivity(), AddCustomAlephActivity.class);
                intent.putExtra(AddCustomAlephActivity.EXTRA_PARENT_ACTIVITY,getActivity().getClass());
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((FrontalActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        Intent intent = new Intent(getActivity(), ContactDataActivity.class);
        intent.putExtra(ContactDataActivity.EXTRA_LAYER, PAGE_NAME);
        intent.putExtra(ContactDataActivity.EXTRA_CONTRUCTION_INFO, mWrapper.getId());
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        if(!ChapterPackHandlerSupport.chapterPackIsLoaded() && ChapterPackHandlerSupport.getOptions().length > 0){
            ChapterPackHandlerSupport.getChapterPackHandler(getActivity(), ChapterPackHandlerSupport.getOptions()[0]);
        }
        ContactDatabaseHandler handler= ChapterPackHandlerSupport.getContactHandler(getActivity());
        ContactInfoWrapper[] currentContacts= handler.getContacts(null, ContactDatabaseContract.ContactListTable.COLUMN_NAME+" ASC");
        if(currentContacts.length <= 1){
            ContactDatabaseHelper dbh=new ContactDatabaseHelper(getActivity());
            SQLiteDatabase db= dbh.getWritableDatabase();
            dbh.onDelete(db);
            dbh.onCreate(db);
            currentContacts= handler.getContacts(null, ContactDatabaseContract.ContactListTable.COLUMN_NAME+" ASC");
        }
        return currentContacts;
    }



}


