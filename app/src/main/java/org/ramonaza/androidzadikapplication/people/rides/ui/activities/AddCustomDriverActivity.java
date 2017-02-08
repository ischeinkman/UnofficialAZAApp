package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.AddCustomDriverFragment;

public class AddCustomDriverActivity extends BaseActivity {

    public static final String PRESET_CONTACT_ID = "org.ramonaza.androidzadikapplication.CONTACT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent openingIntent = getIntent();
        int presetID = openingIntent.getIntExtra(PRESET_CONTACT_ID, -1);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, AddCustomDriverFragment.newInstance(presetID))
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
