package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.app.ActionBar;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.AddDriverFromContactFragment;

/**
 * Created by ilanscheinkman on 8/25/15.
 */
public class AddContactDriverActivity extends org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, AddDriverFromContactFragment.newInstance())
                    .commit();
        }
    }

}
