package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHelper;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayRidesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayRidesFragment extends Fragment {

    private static final String EXTRA_ALGORITHM = "org.ramonaza.unofficialazaapp.algorithm";
    private static final String EXTRA_RETAIN_RIDES = "org.ramonaza.unofficialazaapp.retainrides";
    private static final String EXTRA_CLUSTER_TYPE = "org.ramonaza.unofficialazaapp.clusterType";

    private TextView ridesDisplay;
    private ProgressBar mBar;

    private int algorithmIndex;
    private int clusterIndex;
    private boolean retainRides;

    public DisplayRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DisplayRidesFragment.
     */
    public static DisplayRidesFragment newInstance(int algorithm, int clusterIndex, boolean retain) {
        DisplayRidesFragment fragment = new DisplayRidesFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_ALGORITHM, algorithm);
        args.putBoolean(EXTRA_RETAIN_RIDES, retain);
        args.putInt(EXTRA_CLUSTER_TYPE, clusterIndex);
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
                return null;
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
                return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_display_rides, container, false);
        ridesDisplay = (TextView) rootView.findViewById(R.id.RidesTextList);
        mBar = (ProgressBar) rootView.findViewById(R.id.cProgressBar);
        algorithmIndex = getArguments().getInt(EXTRA_ALGORITHM);
        clusterIndex = getArguments().getInt(EXTRA_CLUSTER_TYPE);
        retainRides = getArguments().getBoolean(EXTRA_RETAIN_RIDES);
        new CreateRidesText().execute();
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class CreateRidesText extends AsyncTask<Void, Void, String> {

        DriverInfoWrapper[] rides;
        ContactInfoWrapper[] driverless;
        private RidesDatabaseHandler rhandler;

        @Override
        protected String doInBackground(Void... params) {
            createRides();
            RidesOptimizer optimizer = new RidesOptimizer();
            optimizer.loadDriver(rides);
            optimizer.loadPassengers(driverless);
            optimizer.setUpAlgorithms(getAlgorithmsByIndex(algorithmIndex), retainRides, getClusterByIndex(clusterIndex));
            optimizer.optimize();
            RidesDatabaseHandler ridesDatabaseHandler = new RidesDatabaseHandler(getActivity());
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
        }

        private void createRides() {
            SQLiteDatabase db = new ContactDatabaseHelper(getActivity()).getWritableDatabase();
            rhandler = new RidesDatabaseHandler(db);
            rides = rhandler.getDrivers(null, ContactDatabaseContract.DriverListTable.COLUMN_NAME + " ASC");
            ContactDatabaseHandler chandler = new ContactDatabaseHandler(db);
            String[] whereclause;
            whereclause = new String[]{
                    String.format("%s = %d", ContactDatabaseContract.ContactListTable.COLUMN_PRESENT, 1),
                    String.format("not %s in (SELECT %s FROM %s)", ContactDatabaseContract.ContactListTable._ID,
                            ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER, ContactDatabaseContract.RidesListTable.TABLE_NAME)
            };
            driverless = chandler.getContacts(whereclause, null);
        }
    }

}
