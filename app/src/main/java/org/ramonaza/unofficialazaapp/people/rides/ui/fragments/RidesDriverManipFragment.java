package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.other.InfoWrapperTextWithButtonAdapter;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.AddPassengerToDriverActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class RidesDriverManipFragment extends Fragment {

    public static final String EXTRA_DRIVERID = RidesDriverManipActivity.EXTRA_DRIVERID;

    private DriverInfoWrapper mDriver;
    private int driverId;
    private View rootView;
    private ListView passengersView;
    private InfoWrapperTextWithButtonAdapter mAdapter;
    private Subscription popTask;
    private Subscription deleteTask;
    private RidesDatabaseHandler dbHandler;

    public RidesDriverManipFragment() {
    }

    public static RidesDriverManipFragment newInstance(int inDriver) {
        RidesDriverManipFragment rFrag = new RidesDriverManipFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_DRIVERID, inDriver);
        rFrag.setArguments(args);
        return rFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        driverId = args.getInt(EXTRA_DRIVERID);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rides_driver_manip, container, false);
        this.rootView = rootView;
        passengersView = (ListView) rootView.findViewById(R.id.Passengers);
        this.mAdapter = new InfoWrapperTextWithButtonAdapter(getActivity()) {
            @Override
            public String getButtonText() {
                return "Delete";
            }

            @Override
            public void onButton(InfoWrapper info) {
                if (deleteTask != null && !deleteTask.isUnsubscribed()) deleteTask.unsubscribe();
                if (dbHandler == null) dbHandler = new RidesDatabaseHandler(getActivity());
                deleteTask = dbHandler.deleteDrivers(info.getId())
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                refreshData();
                                deleteTask.unsubscribe();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT);
                            }
                        });
            }

            @Override
            public void onText(InfoWrapper info) {
                ContactInfoWrapper currentPassenger = (ContactInfoWrapper) info;
                Toast toast = Toast.makeText(getActivity(), currentPassenger.getAddress(), Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        passengersView.setAdapter(mAdapter);
        (rootView.findViewById(R.id.AddPassengerToDriverButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAdd = new Intent(getActivity(), AddPassengerToDriverActivity.class);
                intentAdd.putExtra(AddPassengerToDriverActivity.EXTRA_DRIVERID, mDriver.getId());
                startActivity(intentAdd);
            }
        });
        refreshData();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        if (popTask != null && !popTask.isUnsubscribed()) popTask.unsubscribe();
        if (dbHandler == null) dbHandler = new RidesDatabaseHandler(getActivity());
        popTask = dbHandler
                .getDrivers(driverId)
                .flatMap(new Func1<DriverInfoWrapper, Observable<ContactInfoWrapper>>() {
                    @Override
                    public Observable<ContactInfoWrapper> call(DriverInfoWrapper driverInfoWrapper) {
                        mDriver = driverInfoWrapper;
                        return dbHandler.getPassengersInCar(driverInfoWrapper.getId());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ContactInfoWrapper>>() {
                    @Override
                    public void call(List<ContactInfoWrapper> contactInfoWrappers) {
                        mAdapter.clear();
                        ActionBar actionBar = getActivity().getActionBar();
                        actionBar.setTitle(mDriver.getName());
                        ((TextView) rootView.findViewById(R.id.DriverName)).setText(mDriver.getName());
                        ((TextView) rootView.findViewById(R.id.FreeSpots)).setText("" + mDriver.getFreeSpots());
                        mAdapter.addAll(contactInfoWrappers);
                        popTask.unsubscribe();
                    }
                });
    }
}
