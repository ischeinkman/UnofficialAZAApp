package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
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
import org.ramonaza.unofficialazaapp.helpers.backend.DatabaseHandler;
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
import java.util.List;

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

        new CreateRidesText().execute();
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class CreateRidesText extends AsyncTask<Void, Void, String> {

        private RidesDatabaseHandler rhandler;

        @Override
        protected String doInBackground(Void... params) {
            createRides();
            if (retainRides && !optimize) {
                return createRidesList(rides, driverless);
            }
            RidesOptimizer optimizer = new RidesOptimizer();
            optimizer.loadDrivers(rides);
            optimizer.loadPassengers(driverless);
            if (optimize) {
                optimizer.setUpAlgorithms(getAlgorithmsByIndex(algorithmIndex), retainRides, getClusterByIndex(clusterIndex));
            } else {
                optimizer.setUpAlgorithms(null, retainRides, null);
            }
            optimizer.optimize();
            RidesDatabaseHandler ridesDatabaseHandler = (RidesDatabaseHandler) DatabaseHandler.getHandler(RidesDatabaseHandler.class);;
            ridesDatabaseHandler.updateRides(optimizer.getDrivers(), optimizer.getDriverless());
            driverless = optimizer.getDriverless();
            rhandler.updateRides(rides, driverless);
            return createRidesList(rides, driverless);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mBar.setVisibility(View.GONE);
            ridesDisplay.setText(Html.fromHtml(s));
            ridesLoaded = true;
        }

        private void createRides() {
            rhandler = (RidesDatabaseHandler) DatabaseHandler.getHandler(RidesDatabaseHandler.class);;
            rides = rhandler.getDrivers(null, AppDatabaseContract.DriverListTable.COLUMN_NAME + " ASC");
            String[] whereclause;
            whereclause = new String[]{
                    String.format("%s = %d", AppDatabaseContract.ContactListTable.COLUMN_PRESENT, 1),
                    String.format("not %s in (SELECT %s FROM %s)", AppDatabaseContract.ContactListTable._ID,
                            AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, AppDatabaseContract.RidesListTable.TABLE_NAME)
            };
            ContactDatabaseHandler chandler = (ContactDatabaseHandler) DatabaseHandler.getHandler(ContactDatabaseHandler.class);
            driverless = chandler.getContacts(whereclause, null);
        }
    }

}
