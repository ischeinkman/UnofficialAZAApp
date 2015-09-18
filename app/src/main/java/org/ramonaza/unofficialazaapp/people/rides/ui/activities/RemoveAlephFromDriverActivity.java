package org.ramonaza.unofficialazaapp.people.rides.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.fragments.RemoveAlephFromDriverFragment;

public class RemoveAlephFromDriverActivity extends BaseActivity {

    private int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        driverId=intent.getIntExtra("DriverId",0);
        setContentView(R.layout.activity_single_fragment);
        FragmentManager fragmentManager=getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, RemoveAlephFromDriverFragment.newInstance(driverId)).commit();
    }
}
