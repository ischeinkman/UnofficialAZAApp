package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.os.Bundle;

import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperCheckBoxesFragment;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import rx.Observable;
import rx.functions.Func1;

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
    public Observable<?> onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked) {
        final RidesDatabaseHandler handler = new RidesDatabaseHandler(getActivity());
        return Observable.from(checked).flatMap(new Func1<InfoWrapper, Observable<?>>() {
            @Override
            public Observable<?> call(InfoWrapper infoWrapper) {
                return handler.removePassengersFromCar(infoWrapper.getId());
            }
        });
    }

    @Override
    public Observable<ContactInfoWrapper> generateInfo() {
        RidesDatabaseHandler handler = new RidesDatabaseHandler(getActivity());
        return handler.getPassengersInCar(driverId);
    }
}
