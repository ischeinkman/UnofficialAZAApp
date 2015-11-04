package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextWithButtonFragment;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.PresentListedContactActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesContactManipActivity;
import org.ramonaza.unofficialazaapp.people.ui.activities.AddCustomContactActivity;
import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PassengersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PassengersFragment extends InfoWrapperTextWithButtonFragment {

    private static final String EXTRA_PARENT_ACTIVITY = "parent activity";

    private SQLiteDatabase db;

    public PassengersFragment() {

    }

    public static PassengersFragment newInstance() {
        PassengersFragment fragment = new PassengersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String buttonName() {
        return "Delete";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatabaseHelper dbh = new AppDatabaseHelper(getActivity());
        this.db = dbh.getWritableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayoutId = R.layout.fragment_rides_passengers;
        View rootView = super.onCreateView(inflater, container, savedInstanceState); //Retrieve the parent's view to manipulate
        Button listAdd = (Button) rootView.findViewById(R.id.AddPresetContactButton);
        listAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent presListIntent = new Intent(getActivity(), PresentListedContactActivity.class);
                startActivity(presListIntent);
            }
        });
        Button customAdd = (Button) rootView.findViewById(R.id.AddCustomContactButton);
        customAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customContactIntent = new Intent(getActivity(), AddCustomContactActivity.class);
                customContactIntent.putExtra(EXTRA_PARENT_ACTIVITY, getActivity().getClass());
                startActivity(customContactIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        RidesDatabaseHandler handler = new RidesDatabaseHandler(db);
        handler.setContactAbsent(mWrapper.getId());
        refreshData();
    }

    @Override
    public void onTextClick(InfoWrapper mWrapper) {
        Intent intent = new Intent(getActivity(), RidesContactManipActivity.class);
        intent.putExtra(RidesContactManipActivity.EXTRA_CONTACTID, mWrapper.getId());
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        ContactDatabaseHandler handler = new ContactDatabaseHandler(db);
        return handler.getContacts(new String[]{
                AppDatabaseContract.ContactListTable.COLUMN_PRESENT + "=1",
        }, AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    public void onResume() {
        AppDatabaseHelper databaseHelper = new AppDatabaseHelper(getActivity());
        db = databaseHelper.getWritableDatabase();
        super.onResume();
    }
}
