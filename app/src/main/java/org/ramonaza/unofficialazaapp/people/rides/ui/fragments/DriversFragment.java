package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextWithButtonFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.AddContactDriverActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.AddCustomDriverActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesDriverManipActivity;
import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriversFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriversFragment extends InfoWrapperTextWithButtonFragment {


    public DriversFragment() {
        // Required empty public constructor
    }

    public static DriversFragment newInstance() {
        DriversFragment fragment = new DriversFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayoutId = R.layout.fragment_rides_drivers;
        View rootView = super.onCreateView(inflater, container, savedInstanceState); //Retrieve the parent's view to manipulate
        Button presetButton = (Button) rootView.findViewById(R.id.AddPresetDriverButton);
        presetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent presetDriverIntent = new Intent(getActivity(), AddContactDriverActivity.class);
                startActivity(presetDriverIntent);
            }
        });
        Button customButton = (Button) rootView.findViewById(R.id.AddCustomDriverButton);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customDriverIntent = new Intent(getActivity(), AddCustomDriverActivity.class);
                startActivity(customDriverIntent);
            }
        });
        refreshData();
        return rootView;
    }

    @Override
    public String buttonName() {
        return "Delete";
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        new DeleteCar(getActivity()).execute(mWrapper.getId());
    }

    @Override
    public void onTextClick(InfoWrapper mWrapper) {
        Intent intent = new Intent(getActivity(), RidesDriverManipActivity.class);
        intent.putExtra(RidesDriverManipActivity.EXTRA_DRIVERID, mWrapper.getId());
        startActivity(intent);
    }


    @Override
    public InfoWrapper[] generateInfo() {
        RidesDatabaseHandler handler = new RidesDatabaseHandler(getActivity());
        return handler.getDrivers(null, ContactDatabaseContract.DriverListTable.COLUMN_NAME + " ASC");
    }


    public class DeleteCar extends AsyncTask<Integer, Void, Void> {

        private Context context;

        public DeleteCar(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            RidesDatabaseHandler handler = new RidesDatabaseHandler(context);
            for (int id : params) {
                handler.deleteDriver(id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            refreshData();
        }
    }
}




