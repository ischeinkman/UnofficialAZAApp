package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PresentAlephFromListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentAlephFromListFragment extends InfoWrapperCheckBoxesFragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PresentAlephFromListFragment.
     */
    public static PresentAlephFromListFragment newInstance() {
        PresentAlephFromListFragment fragment = new PresentAlephFromListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked) {
        new SubmitFromList().execute(checked);
    }

    @Override
    public ContactInfoWrapper[] generateInfo() {
        ContactDatabaseHandler handler=new ContactDatabaseHandler(getActivity());
        return handler.getContacts(new String[]{ContactDatabaseContract.ContactListTable.COLUMN_PRESENT+"=0"},
                ContactDatabaseContract.ContactListTable.COLUMN_NAME+" ASC");
    }


    private class SubmitFromList extends AsyncTask<InfoWrapper,Void,Void> {

        @Override
        protected Void doInBackground(InfoWrapper ... params) {
            ContactDatabaseHandler handler=new ContactDatabaseHandler(getActivity());
            ContactInfoWrapper[] alephs= Arrays.copyOf(params,params.length,ContactInfoWrapper[].class);
            handler.updateField(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT,"1",alephs);
            return null;
        }


    }

}
