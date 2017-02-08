package org.ramonaza.androidzadikapplication.people.rides.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.database.AppDatabaseContract;
import org.ramonaza.androidzadikapplication.database.AppDatabaseHelper;
import org.ramonaza.androidzadikapplication.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.androidzadikapplication.people.rides.ui.activities.RidesContactManipActivity;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

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
    private PopView popTask;

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
        if (popTask != null) popTask.cancel(true);
        popTask = new PopView(getActivity());
        popTask.execute(contactID);
    }


    private class PopView extends AsyncTask<Integer, Void, Void> {

        private Context context;
        private DriverInfoWrapper[] drivers;

        public PopView(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            SQLiteDatabase db = new AppDatabaseHelper(context).getWritableDatabase();
            RidesDatabaseHandler rhandler = new RidesDatabaseHandler(db);
            mContact = rhandler.getContact(params[0]);
            String[] whereclause = new String[]{
                    String.format("%s in (SELECT %s FROM %s WHERE %s = %s)", AppDatabaseContract.DriverListTable._ID,
                            AppDatabaseContract.RidesListTable.COLUMN_CAR, AppDatabaseContract.RidesListTable.TABLE_NAME,
                            AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, mContact.getId())
            };
            drivers = rhandler.getDrivers(whereclause, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String viewData = "Name: " + mContact.getName() + "\n\n" +
                    "Address: " + mContact.getAddress() + "\n\n" +
                    "School: " + mContact.getSchool() + "\n\n";
            for (DriverInfoWrapper driver : drivers) {
                viewData += "Currently in car: " + driver.getName() + "\n";
            }
            if (drivers.length == 0) viewData += "Not currently in car.";
            dataview.setTextSize(20);
            dataview.setText(viewData);
            getActivity().getActionBar().setTitle(mContact.getName());
            popTask = null;
        }
    }


}
