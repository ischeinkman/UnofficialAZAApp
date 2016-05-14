package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;
import org.ramonazaapi.rides.algorithms.ClusterMatch;
import org.ramonazaapi.rides.algorithms.FlamboyantElephant;
import org.ramonazaapi.rides.algorithms.NaiveHungarian;
import org.ramonazaapi.rides.algorithms.SerialArson;
import org.ramonazaapi.rides.clusters.ExpansionistCluster;
import org.ramonazaapi.rides.clusters.HungryCluster;
import org.ramonazaapi.rides.clusters.LazyCluster;
import org.ramonazaapi.rides.clusters.RidesCluster;
import org.ramonazaapi.rides.clusters.SnakeCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayRidesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayRidesFragment extends Fragment {

    private static final String EXTRA_ALGORITHM = "org.ramonaza.unofficialazaapp.algorithm";
    private static final String EXTRA_RETAIN_RIDES = "org.ramonaza.unofficialazaapp.retainrides";
    private static final String EXTRA_CLUSTER_TYPE = "org.ramonaza.unofficialazaapp.clusterType";
    private static final String EXTRA_OPTIMIZE = "org.ramonaza.unofficialazaapp.optomizeRides";

    private TextView ridesDisplay;
    private ProgressBar mBar;

    private DriverInfoWrapper[] rides;
    private ContactInfoWrapper[] driverless;
    private boolean ridesLoaded;

    private int algorithmIndex;
    private int clusterIndex;
    private boolean retainRides;
    private boolean optimize;
    private Subscription ridesSub;

    public DisplayRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DisplayRidesFragment.
     */
    public static DisplayRidesFragment newInstance(boolean optimize, int algorithm, int clusterIndex, boolean retain) {
        DisplayRidesFragment fragment = new DisplayRidesFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_ALGORITHM, algorithm);
        args.putBoolean(EXTRA_RETAIN_RIDES, retain);
        args.putInt(EXTRA_CLUSTER_TYPE, clusterIndex);
        args.putBoolean(EXTRA_OPTIMIZE, optimize);
        fragment.setArguments(args);
        return fragment;
    }

    private static String createRidesList(Collection<? extends DriverInfoWrapper> drivers, Collection<? extends ContactInfoWrapper> contacts) {
        return createRidesList(drivers.toArray(new DriverInfoWrapper[0]), contacts.toArray(new ContactInfoWrapper[0]));
    }

    private static String createRidesList(DriverInfoWrapper[] drivers, ContactInfoWrapper[] driverless) {
        String ridesList = "";
        int totalFree = 0;
        int overStuff = 0;
        for (DriverInfoWrapper driver : drivers) {
            ridesList += String.format("<h1><b><u>%s</u></b></h1>", driver.getName(), driver.getFreeSpots());
            for (ContactInfoWrapper passenger : driver.getPassengersInCar()) {
                ridesList += String.format("-%s<br/>", passenger.getName());
            }
            ridesList += "<b>Free Spots: " + driver.getFreeSpots();
            ridesList += "</b><br/><br/>";
            if (driver.getFreeSpots() > 0) totalFree += driver.getFreeSpots();
            else if (driver.getFreeSpots() < 0) overStuff -= driver.getFreeSpots();
        }
        if (driverless.length > 0) {
            ridesList += "<h1><b><u>Driverless</u></b></h1>";
            for (ContactInfoWrapper driverlessPassenger : driverless)
                ridesList += String.format("-%s<br/>", driverlessPassenger.getName());
        }
        ridesList += String.format("<br/><h3><b><u>Stats:</u></b></h3>" +
                "Total Free Spots:       %d <br/>" +
                "Total Overstuff:        %d <br/>" +
                "Total Driverless:       %d <br/>", totalFree, overStuff, driverless.length);
        return ridesList;
    }

    private static Class<? extends RidesCluster> getClusterByIndex(int index) {
        switch (index) {
            case 0:
                return null;
            case 1:
                return SnakeCluster.class;
            case 2:
                return ExpansionistCluster.class;
            case 3:
                return LazyCluster.class;
            case 4:
                return HungryCluster.class;
            default:
                return SnakeCluster.class; //Snake is currently the best cluster type
        }
    }

    private static RidesOptimizer.RidesAlgorithm[] getAlgorithmsByIndex(int index) {
        switch (index) {
            case 0:
                return null;
            case 1:
                return new RidesOptimizer.RidesAlgorithm[]{new SerialArson()};
            case 2:
                return new RidesOptimizer.RidesAlgorithm[]{new FlamboyantElephant()};
            case 3:
                return new RidesOptimizer.RidesAlgorithm[]{new NaiveHungarian()};
            case 4:
                return new RidesOptimizer.RidesAlgorithm[]{new ClusterMatch()};
            default:
                return new RidesOptimizer.RidesAlgorithm[]{new NaiveHungarian()}; //Naive Hungarian is currently the best algorithm
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ridesLoaded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        algorithmIndex = getArguments().getInt(EXTRA_ALGORITHM);
        clusterIndex = getArguments().getInt(EXTRA_CLUSTER_TYPE);
        retainRides = getArguments().getBoolean(EXTRA_RETAIN_RIDES);
        optimize = getArguments().getBoolean(EXTRA_OPTIMIZE);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_display_rides, container, false);

        ridesDisplay = (TextView) rootView.findViewById(R.id.RidesTextList);
        mBar = (ProgressBar) rootView.findViewById(R.id.cProgressBar);

        Button textButton = (Button) rootView.findViewById(R.id.TextRidesButton);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ridesLoaded) {
                    String driverMsg = getString(R.string.rides_driver_text_send_format);
                    String passengerMsg = getString(R.string.rides_passenger_text_send_format);
                    String driverlessMsg = getString(R.string.rides_driverless_text_send_format);
                    SmsManager smsManager = SmsManager.getDefault();
                    for (DriverInfoWrapper toText : rides) {
                        String passengerlist = "";
                        for (ContactInfoWrapper passenger : toText.getPassengersInCar()) {
                            passengerlist += "\n-" + passenger.getName() + "\tPhone: " + passenger.getPhoneNumber();
                            if (passenger.phoneNumberIsValid()) {
                                String firstName = passenger.getName().split(" ")[0];
                                String msg = String.format(passengerMsg, firstName, toText.getName(), toText.getContactInfo().getPhoneNumber());
                                smsManager.sendTextMessage(passenger.getPhoneNumber(), null, msg, null, null);
                            }
                        }
                        if (toText.isContactable() && toText.getContactInfo().phoneNumberIsValid()) {
                            String firstName = toText.getName().split(" ")[0];
                            String msg = String.format(driverMsg, firstName, passengerlist);
                            smsManager.sendTextMessage(toText.getContactInfo().getPhoneNumber(), null, msg, null, null);
                        }
                    }
                    for (ContactInfoWrapper walker : driverless) {
                        if (walker.phoneNumberIsValid()) {
                            String firstName = walker.getName().split(" ")[0];
                            String msg = String.format(driverlessMsg, firstName);
                            smsManager.sendTextMessage(walker.getPhoneNumber(), null, msg, null, null);
                        }
                    }
                    Toast.makeText(getActivity(), "Text Messages Sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "No contactable drivers found.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button emailButton = (Button) rootView.findViewById(R.id.EmailRidesButton);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ridesLoaded) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");

                    List<String> emailAddresses = new ArrayList<String>();
                    for (DriverInfoWrapper driver : rides) {
                        if (driver.isContactable() && driver.getContactInfo().emailIsValid())
                            emailAddresses.add(driver.getContactInfo().getEmail());
                    }
                    for (ContactInfoWrapper walker : driverless) {
                        if (walker.emailIsValid()) emailAddresses.add(walker.getEmail());
                    }
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddresses.toArray(new String[emailAddresses.size()]));

                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Current Rides List");

                    String msg = getString(R.string.rides_email_send_format);
                    String ridesBody = createRidesList(rides, driverless);
                    String fullBody = String.format(msg, ridesBody);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, fullBody);

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(DisplayRidesFragment.this.getActivity(), "Cannot send email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No contactable drivers found.", Toast.LENGTH_LONG).show();
                }
            }
        });

        ridesSub = doRides().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mBar.setVisibility(View.GONE);
                ridesDisplay.setText(Html.fromHtml(s));
                ridesLoaded = true;
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(getActivity(), "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        return rootView;
    }

    private Observable<String> doRides() {
        final DriverInfoWrapper DRIVERLESS_KEY = new DriverInfoWrapper();

        final RidesDatabaseHandler rhandler = new RidesDatabaseHandler(getActivity());
        final ContactDatabaseHandler chandler = new ContactDatabaseHandler(rhandler);
        final Map<DriverInfoWrapper, List<ContactInfoWrapper>> ridesmap = new HashMap<>();

        return rhandler.getDrivers(null)
                .map(new Func1<DriverInfoWrapper, DriverInfoWrapper>() {
                    @Override
                    public DriverInfoWrapper call(DriverInfoWrapper driverInfoWrapper) {
                        ridesmap.put(driverInfoWrapper, driverInfoWrapper.getPassengersInCar());
                        return driverInfoWrapper;
                    }
                })
                .toList()
                .flatMap(new Func1<List<DriverInfoWrapper>, Observable<ContactInfoWrapper>>() {
                    @Override
                    public Observable<ContactInfoWrapper> call(List<DriverInfoWrapper> driverInfoWrappers) {
                        String[] whereclause;
                        whereclause = new String[]{
                                String.format("%s = %d", AppDatabaseContract.ContactListTable.COLUMN_PRESENT, 1),
                                String.format("not %s in (SELECT %s FROM %s)", AppDatabaseContract.ContactListTable._ID,
                                        AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, AppDatabaseContract.RidesListTable.TABLE_NAME)
                        };
                        return chandler.getContacts(whereclause, null);
                    }
                })
                .toList()
                .map(new Func1<List<ContactInfoWrapper>, List<ContactInfoWrapper>>() {
                    @Override
                    public List<ContactInfoWrapper> call(List<ContactInfoWrapper> contactInfoWrappers) {
                        ridesmap.put(DRIVERLESS_KEY, contactInfoWrappers);
                        return contactInfoWrappers;
                    }
                })
                .map(new Func1<List<ContactInfoWrapper>, Map<DriverInfoWrapper, List<ContactInfoWrapper>>>() {
                    @Override
                    public Map<DriverInfoWrapper, List<ContactInfoWrapper>> call(List<ContactInfoWrapper> contactInfoWrappers) {
                        if (retainRides && !optimize) return ridesmap;
                        RidesOptimizer optimizer = new RidesOptimizer();
                        Set<DriverInfoWrapper> allDrivers = ridesmap.keySet();
                        allDrivers.remove(DRIVERLESS_KEY);
                        optimizer.loadDrivers(allDrivers);
                        optimizer.loadDriverless(ridesmap.get(DRIVERLESS_KEY));
                        if (optimize) {
                            optimizer.setUpAlgorithms(getAlgorithmsByIndex(algorithmIndex), retainRides, getClusterByIndex(clusterIndex));
                        } else {
                            optimizer.setUpAlgorithms(null, retainRides, null);
                        }
                        optimizer.optimize();
                        for (DriverInfoWrapper driver : optimizer.getDrivers()) {
                            ridesmap.put(driver, driver.getPassengersInCar());
                        }
                        ridesmap.put(DRIVERLESS_KEY, Arrays.asList(optimizer.getDriverless()));
                        return ridesmap;
                    }
                })
                .map(new Func1<Map<DriverInfoWrapper, List<ContactInfoWrapper>>, Map<DriverInfoWrapper, List<ContactInfoWrapper>>>() {
                    @Override
                    public Map<DriverInfoWrapper, List<ContactInfoWrapper>> call(Map<DriverInfoWrapper, List<ContactInfoWrapper>> ridesmap) {
                        Set<DriverInfoWrapper> drivers = ridesmap.keySet();
                        drivers.remove(DRIVERLESS_KEY);
                        rhandler.updateRides(drivers.toArray(new DriverInfoWrapper[0]), ridesmap.get(DRIVERLESS_KEY).toArray(new ContactInfoWrapper[0]));
                        return ridesmap;
                    }
                })
                .map(new Func1<Map<DriverInfoWrapper, List<ContactInfoWrapper>>, String>() {
                    @Override
                    public String call(Map<DriverInfoWrapper, List<ContactInfoWrapper>> driverInfoWrapperListMap) {
                        Set<DriverInfoWrapper> drivers = ridesmap.keySet();
                        drivers.remove(DRIVERLESS_KEY);
                        return createRidesList(drivers, driverInfoWrapperListMap.get(DRIVERLESS_KEY));
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
