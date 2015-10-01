package org.ramonaza.unofficialazaapp.people.rides.ui.activities;

import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.fragments.ConfigureRidesDisplayFragment;

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
