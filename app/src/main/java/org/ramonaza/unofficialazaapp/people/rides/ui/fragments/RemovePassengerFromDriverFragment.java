package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;

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
