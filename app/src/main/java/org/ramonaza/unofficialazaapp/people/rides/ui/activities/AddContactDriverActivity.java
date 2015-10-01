package org.ramonaza.unofficialazaapp.people.rides.ui.activities;

import android.app.ActionBar;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.people.rides.ui.fragments.AddContactDriverFragment;

/**
 * Created by ilanscheinkman on 8/25/15.
 */
public class AddContactDriverActivity extends org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, AddContactDriverFragment.newInstance())
                    .commit();
        }
    }

}
