package org.ramonaza.androidzadikapplication.people.rides.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.androidzadikapplication.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.androidzadikapplication.people.rides.ui.activities.RidesDriverManipActivity;
import org.ramonazaapi.interfaces.InfoWrapper;

public class RemovePassengerFromDriverFragment extends InfoWrapperCheckBoxesFragment {

    public static final String EXTRA_DRIVERID = RidesDriverManipActivity.EXTRA_DRIVERID;
    private int driverId;

    public RemovePassengerFromDriverFragment() {
    }

    public static RemovePassengerFromDriverFragment newInstance(int driverId) {
        RemovePassengerFromDriverFragment fragment = new RemovePassengerFromDriverFragment();
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
        RidesDatabaseHandler handler = new RidesDatabaseHandler(getActivity());
        return handler.getPassengersInCar(driverId);
    }

    private class SubmitFromList extends AsyncTask<InfoWrapper, Void, Void> {

        @Override
        protected Void doInBackground(InfoWrapper... params) {
            RidesDatabaseHandler handler = new RidesDatabaseHandler(getActivity());
            for (InfoWrapper passenger : params) {
                handler.removePassengerFromCar(passenger.getId());
            }
            return null;
        }


    }
}
