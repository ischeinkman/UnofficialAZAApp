package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.AddCustomDriverActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import rx.Observable;

/**
 * Created by ilanscheinkman on 8/25/15.
 */
public class AddDriverFromContactFragment extends InfoWrapperTextListFragment {

    public static AddDriverFromContactFragment newInstance() {
        AddDriverFromContactFragment fragment = new AddDriverFromContactFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        ContactInfoWrapper contactBase = (ContactInfoWrapper) mWrapper;
        Intent intent = new Intent(getActivity(), AddCustomDriverActivity.class);
        intent.putExtra(AddCustomDriverActivity.PRESET_CONTACT_ID, contactBase.getId());
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public Observable<? extends InfoWrapper> generateInfo() {
        ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        return handler.getContacts(null, AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");
    }
}
