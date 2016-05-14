package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.os.Bundle;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.Arrays;

import rx.Observable;

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
    public Observable<?> onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked) {
        ContactInfoWrapper[] newPassengers = Arrays.copyOf(checked, checked.length, ContactInfoWrapper[].class);
        RidesDatabaseHandler rideshandler = new RidesDatabaseHandler(getActivity());
        return rideshandler.addPassengersToCar(driverId, newPassengers);
    }

    @Override
    public Observable<ContactInfoWrapper> generateInfo() {
        ContactDatabaseHandler dbhandler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        return dbhandler.getContacts(new String[]{
                AppDatabaseContract.ContactListTable.COLUMN_PRESENT + "=1",
                AppDatabaseContract.ContactListTable._ID + " NOT IN (" + "SELECT " + AppDatabaseContract.RidesListTable.COLUMN_PASSENGER + " FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME + ")"
        }, AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");
    }
}
