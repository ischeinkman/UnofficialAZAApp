package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.RidesDriverManipFragment;

public class RidesDriverManipActivity extends BaseActivity {

    public static final String EXTRA_DRIVERID = "org.ramonaza.androidzadikapplication.DRIVER_ID";
    private int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent intent = getIntent();
        driverId = intent.getIntExtra(EXTRA_DRIVERID, 0);
        if (driverId == 0 && savedInstanceState != null)
            driverId = savedInstanceState.getInt(EXTRA_DRIVERID, 0);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, RidesDriverManipFragment.newInstance(driverId)).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_DRIVERID, driverId);
    }
}
