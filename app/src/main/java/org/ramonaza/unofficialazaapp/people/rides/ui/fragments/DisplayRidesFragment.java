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
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.DriverInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesOptimizer;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.AlephCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.ExpansionistCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.HungryCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.LazyCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.SnakeCluster;

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

    private int optimizationAlgorithm;
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
        for (DriverInfoWrapper driver : drivers) {
            ridesList += String.format("<h1><b><u>%s</u></b></h1>", driver.getName(), driver.getFreeSpots());
            for (ContactInfoWrapper alephInCar : driver.getAlephsInCar()) {
                ridesList += String.format("-%s<br/>", alephInCar.getName());
            }
            ridesList += "<b>Free Spots: " + driver.getFreeSpots();
            ridesList += "</b><br/><br/>";
        }
        if (driverless.length > 0) {
            ridesList += "<h1><b><u>Driverless</u></b></h1>";
            for (ContactInfoWrapper driverlessAleph : driverless)
                ridesList += String.format("-%s<br/>", driverlessAleph.getName());
        }
        return ridesList;
    }

    private static Class<? extends AlephCluster> getClusterByIndex(int index) {
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
        optimizationAlgorithm = getArguments().getInt(EXTRA_ALGORITHM);
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

        private RidesDatabaseHandler rhandler;

        DriverInfoWrapper[] rides;
        ContactInfoWrapper[] driverless;

        @Override
        protected String doInBackground(Void... params) {
            createRides();
            RidesOptimizer optimizer = new RidesOptimizer();
            optimizer.loadDriver(rides);
            optimizer.loadPassengers(driverless);
            optimizer.setAlgorithm(optimizationAlgorithm, retainRides, getClusterByIndex(clusterIndex));
            optimizer.optimize();
            RidesDatabaseHandler ridesDatabaseHandler = new RidesDatabaseHandler(getActivity());
            ridesDatabaseHandler.updateRides(optimizer.getDrivers(), optimizer.getDriverless());
            rides = optimizer.getDrivers();
            driverless = optimizer.getDriverless();
            rhandler.updateRides(rides,driverless);
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
                            ContactDatabaseContract.RidesListTable.COLUMN_ALEPH, ContactDatabaseContract.RidesListTable.TABLE_NAME)
            };
            driverless = chandler.getContacts(whereclause, null);
        }
    }

}
