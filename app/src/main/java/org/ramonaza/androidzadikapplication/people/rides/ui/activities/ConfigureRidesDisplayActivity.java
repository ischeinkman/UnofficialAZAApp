package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.ConfigureRidesDisplayFragment;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class ConfigureRidesDisplayActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ConfigureRidesDisplayFragment.newInstance())
                    .commit();
        }
    }
}
