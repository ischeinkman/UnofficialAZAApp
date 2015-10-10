package org.ramonaza.unofficialazaapp.people.rides.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.fragments.DisplayRidesFragment;

public class DisplayRidesActivity extends BaseActivity {

    public static final String EXTRA_ALGORITHM = "org.ramonaza.unofficialazaapp.algorithm";
    public static final String EXTRA_RETAIN_RIDES = "org.ramonaza.unofficialazaapp.retainrides";
    public static final String EXTRA_CLUSTER_TYPE = "org.ramonaza.unofficialazaapp.clusterType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent callingIntent = getIntent();
        int algorithm = callingIntent.getIntExtra(EXTRA_ALGORITHM, -1);
        int clusterIndex = callingIntent.getIntExtra(EXTRA_CLUSTER_TYPE, 0);
        boolean retainRides = callingIntent.getBooleanExtra(EXTRA_RETAIN_RIDES, true);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, DisplayRidesFragment.newInstance(algorithm, clusterIndex, retainRides))
                    .commit();
        }
    }

}
