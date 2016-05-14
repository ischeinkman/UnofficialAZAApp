package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.Arrays;

import rx.Observable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PresentContactFromListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentContactFromListFragment extends InfoWrapperCheckBoxesFragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PresentContactFromListFragment.
     */
    public static PresentContactFromListFragment newInstance() {
        PresentContactFromListFragment fragment = new PresentContactFromListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Observable<?> onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked) {
        ContactInfoWrapper[] presentContacts = Arrays.copyOf(checked, checked.length, ContactInfoWrapper[].class);
        ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        return handler.updateContactField(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, "1", presentContacts);
    }

    @Override
    public Observable<ContactInfoWrapper> generateInfo() {
        ContactDatabaseHandler handler = new ContactDatabaseHandler(getActivity());
        return handler.getContacts(new String[]{AppDatabaseContract.ContactListTable.COLUMN_PRESENT + "=0"},
                AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");
    }

}
