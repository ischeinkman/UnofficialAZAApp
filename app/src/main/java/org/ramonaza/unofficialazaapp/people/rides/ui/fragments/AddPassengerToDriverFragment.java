package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;

import java.util.Arrays;

public class AddPassengerToDriverFragment extends InfoWrapperCheckBoxesFragment {


    public static final String EXTRA_DRIVERID = RidesDriverManipActivity.EXTRA_DRIVERID;
    private int driverId;

    public AddPassengerToDriverFragment() {
    }

    public static AddPassengerToDriverFragment newInstance(int driverId) {
        AddPassengerToDriverFragment fragment = new AddPassengerToDriverFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_DRIVERID, driverId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.driverId = getArguments().getInt(EXTRA_DRIVERID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked) {
        new SubmitFromList().execute(checked);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        ContactDatabaseHandler dbhandler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        return dbhandler.getContacts(new String[]{
                ContactDatabaseContract.ContactListTable.COLUMN_PRESENT + "=1",
                ContactDatabaseContract.ContactListTable._ID + " NOT IN (" + "SELECT " + ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER + " FROM " + ContactDatabaseContract.RidesListTable.TABLE_NAME + ")"
        }, ContactDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");
    }

    private class SubmitFromList extends AsyncTask<InfoWrapper, Void, Void> {

        @Override
        protected Void doInBackground(InfoWrapper... params) {
            RidesDatabaseHandler rideshandler = new RidesDatabaseHandler(getActivity());
            ContactInfoWrapper[] newPassengers = Arrays.copyOf(params, params.length, ContactInfoWrapper[].class);
            rideshandler.addPassengersToCar(driverId, newPassengers);
            return null;
        }


    }
}
