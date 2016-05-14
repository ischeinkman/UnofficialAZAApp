package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesContactManipActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RidesContactManipFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RidesContactManipFragment extends Fragment {

    public static final String EXTRA_CONTACTID = RidesContactManipActivity.EXTRA_CONTACTID;
    private int contactID;
    private ContactInfoWrapper mContact;
    private TextView dataview;
    private Subscription popTask;
    private RidesDatabaseHandler rhandler;
    private ContactDatabaseHandler chandler;

    public RidesContactManipFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param inID the id of the contact
     * @return A new instance of fragment RidesContactManipFragment.
     */
    public static RidesContactManipFragment newInstance(int inID) {
        RidesContactManipFragment fragment = new RidesContactManipFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_CONTACTID, inID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.contactID = args.getInt(EXTRA_CONTACTID, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_rides_contact_manip, container, false);
        this.dataview = (TextView) rootView.findViewById(R.id.ContactInfoView);
        refreshData();
        return rootView;
    }

    private void refreshData() {
        if (popTask != null && !popTask.isUnsubscribed()) popTask.unsubscribe();
        if (chandler == null) chandler = ChapterPackHandlerSupport.getContactHandler(getActivity());
        if (rhandler == null) rhandler = new RidesDatabaseHandler(chandler);
        popTask = chandler.getContacts(contactID)
                .map(new Func1<ContactInfoWrapper, ContactInfoWrapper>() {
                    @Override
                    public ContactInfoWrapper call(ContactInfoWrapper contactInfoWrapper) {
                        mContact = contactInfoWrapper;
                        return contactInfoWrapper;
                    }
                })
                .map(new Func1<ContactInfoWrapper, String[]>() {
                    @Override
                    public String[] call(ContactInfoWrapper contactInfoWrapper) {
                        return new String[]{
                                String.format("%s in (SELECT %s FROM %s WHERE %s = %s)", AppDatabaseContract.DriverListTable._ID,
                                        AppDatabaseContract.RidesListTable.COLUMN_CAR, AppDatabaseContract.RidesListTable.TABLE_NAME,
                                        AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, mContact.getId())
                        };
                    }
                })
                .flatMap(new Func1<String[], Observable<DriverInfoWrapper>>() {
                    @Override
                    public Observable<DriverInfoWrapper> call(String[] strings) {
                        return rhandler.getDrivers(strings, null);
                    }
                })
                .defaultIfEmpty(new DriverInfoWrapper()).collect(new Func0<StringBuilder>() {
                    @Override
                    public StringBuilder call() {
                        return new StringBuilder();
                    }
                }, new Action2<StringBuilder, DriverInfoWrapper>() {
                    @Override
                    public void call(StringBuilder stringBuilder, DriverInfoWrapper driver) {
                        if (driver.getName() == null) stringBuilder.append("Not currently in car.");
                        else stringBuilder.append("Currently in car: " + driver.getName() + "\n");
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StringBuilder>() {
                    @Override
                    public void call(StringBuilder stringBuilder) {
                        String viewData = "Name: " + mContact.getName() + "\n\n" +
                                "Address: " + mContact.getAddress() + "\n\n" +
                                "School: " + mContact.getSchool() + "\n\n";
                        viewData += stringBuilder.toString();
                        dataview.setTextSize(20);
                        dataview.setText(viewData);
                        getActivity().getActionBar().setTitle(mContact.getName());
                        popTask.unsubscribe();
                    }
                });
    }


}
