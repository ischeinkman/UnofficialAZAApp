package org.ramonaza.unofficialazaapp.events.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.events.ui.fragments.GeneralEventFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;

public class EventPageActivity extends BaseActivity {

    public static final String EVENT_DATA = "org.ramonaza.unofficialazaapp.EVENT_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent rIntent = getIntent();
        int eventID = rIntent.getIntExtra(EVENT_DATA, -1);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, GeneralEventFragment.newInstance(eventID))
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
