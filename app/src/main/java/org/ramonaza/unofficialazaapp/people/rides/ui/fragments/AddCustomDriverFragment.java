package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCustomDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCustomDriverFragment extends Fragment {

    private static final String PRESET_CONTACT_ID = "org.ramonaza.unofficialazaapp.CONTACT";

    private int presetID;
    private ContactInfoWrapper presContact;
    private Subscription initTask;
    private Subscription submitTask;

    public AddCustomDriverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddCustomDriverFragment.
     */
    public static AddCustomDriverFragment newInstance(int presetID) {
        AddCustomDriverFragment fragment = new AddCustomDriverFragment();
        Bundle args = new Bundle();
        args.putInt(PRESET_CONTACT_ID, presetID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        presetID = getArguments().getInt(PRESET_CONTACT_ID);
        final View rootView = inflater.inflate(R.layout.fragment_add_custom_driver, container, false);
        initTask = getPresetContact().subscribe(new Action1<ContactInfoWrapper>() {
            @Override
            public void call(ContactInfoWrapper contactInfoWrapper) {
                if (contactInfoWrapper != null) {
                    EditText nameField = (EditText) rootView.findViewById(R.id.AddDriverName);
                    nameField.setText(presContact.getName());

                    EditText addressField = (EditText) rootView.findViewById(R.id.AddDriverAddress);
                    addressField.setText(presContact.getAddress());
                }
                Button submitButton = (Button) rootView.findViewById(R.id.SubmitButton);
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitTask = saveNewDriver(rootView).subscribe(new Action1<DriverInfoWrapper>() {
                            @Override
                            public void call(DriverInfoWrapper driverInfoWrapper) {
                                if (driverInfoWrapper == null)
                                    Toast.makeText(getActivity(), R.string.error_blank_responce, Toast.LENGTH_LONG).show();
                                else getActivity().finish();
                            }
                        });
                    }
                });
                rootView.findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.cProgressBar).setVisibility(View.INVISIBLE);
            }
        });
        return rootView;
    }

    private Observable<ContactInfoWrapper> getPresetContact() {
        if (presetID < 0) return Observable.just(null);
        return ChapterPackHandlerSupport.getContactHandler(getActivity())
                .getContacts(presetID)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<DriverInfoWrapper> saveNewDriver(View myView) {
        EditText nameField = (EditText) myView.findViewById(R.id.AddDriverName);
        EditText spotsField = (EditText) myView.findViewById(R.id.AddDriverSpots);
        EditText addressField = (EditText) myView.findViewById(R.id.AddDriverAddress);
        String tryName = nameField.getText().toString();
        String trySpots = spotsField.getText().toString();
        final String tryAddress = addressField.getText().toString();
        if (tryName.equals("") || trySpots.equals("") || tryAddress.equals("")) {
            return Observable.just(null);
        }
        final String driverName = tryName;
        final String driverAddress = tryAddress;
        final String driverSpots = trySpots;

        return Observable.create(new Observable.OnSubscribe<DriverInfoWrapper>() {
            @Override
            public void call(Subscriber<? super DriverInfoWrapper> subscriber) {
                DriverInfoWrapper mDriver = new DriverInfoWrapper();
                mDriver.setName(driverName);
                int inpSpots = Integer.parseInt(driverSpots);

                mDriver.setAddress(driverAddress);
                if (mDriver.getAddress().equals(presContact.getAddress())) {
                    mDriver.setLongitude("" + presContact.getY());
                    mDriver.setLatitude("" + presContact.getX());
                }

                if (mDriver.getName().equals(presContact.getName())) mDriver.setSpots(inpSpots + 1);
                else mDriver.setSpots(inpSpots);

                if (presContact != null) mDriver.setContactInfo(presContact);
                subscriber.onNext(mDriver);
                subscriber.onCompleted();

            }
        }).flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
            @Override
            public Observable<DriverInfoWrapper> call(final DriverInfoWrapper driverInfoWrapper) {
                return new RidesDatabaseHandler(ChapterPackHandlerSupport.getContactHandler(getActivity()))
                        .addDriver(driverInfoWrapper)
                        .map(new Func1<Integer, DriverInfoWrapper>() {
                            @Override
                            public DriverInfoWrapper call(Integer integer) {
                                return driverInfoWrapper;
                            }
                        });
            }
        }).flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
            @Override
            public Observable<DriverInfoWrapper> call(final DriverInfoWrapper driverInfoWrapper) {
                if (driverInfoWrapper.getContactInfo() == null)
                    return Observable.just(driverInfoWrapper);
                driverInfoWrapper.getContactInfo().setPresent(true);
                return ChapterPackHandlerSupport.getContactHandler(getActivity())
                        .updateContacts(driverInfoWrapper.getContactInfo())
                        .map(new Func1<ContactInfoWrapper, DriverInfoWrapper>() {
                            @Override
                            public DriverInfoWrapper call(ContactInfoWrapper contactInfoWrapper) {
                                return driverInfoWrapper;
                            }
                        });
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

}
