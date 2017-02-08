package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.RemovePassengerFromDriverFragment;

public class RemovePassengerFromDriverActivity extends BaseActivity {

    private int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        driverId = intent.getIntExtra("DriverId", 0);
        setContentView(R.layout.activity_single_fragment);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, RemovePassengerFromDriverFragment.newInstance(driverId)).commit();
    }
}
