package org.ramonaza.unofficialazaapp.people.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.ui.activities.AddCustomContactActivity;
import org.ramonaza.unofficialazaapp.people.ui.activities.ContactDataActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.Calendar;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Ilan Scheinkman on 1/12/15.
 */
public class ContactListFragment extends InfoWrapperTextListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String PAGE_NAME = "Contact List";
    public int fraglayer;

    public ContactListFragment() {
    }

    public static ContactListFragment newInstance(int sectionNumber) {
        ContactListFragment fragment = new ContactListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.fraglayer = sectionNumber;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_custom_contact:
                Intent intent = new Intent(getActivity(), AddCustomContactActivity.class);
                intent.putExtra(AddCustomContactActivity.EXTRA_PARENT_ACTIVITY, getActivity().getClass());
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
    public Observable<ContactInfoWrapper> generateInfo() {
        ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        return handler.getContacts(null).filter(new Func1<ContactInfoWrapper, Boolean>() {
            @Override
            public Boolean call(ContactInfoWrapper contactInfoWrapper) {
                try {
                    return Integer.valueOf(contactInfoWrapper.getGradYear()) >= Calendar.getInstance().get(Calendar.YEAR);
                } catch (NumberFormatException e){
                    return true;
                }
            }
        });
    }


}


