package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.PresentContactFromListFragment;

public class PresentListedContactActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add Contacts...");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, PresentContactFromListFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }


}
