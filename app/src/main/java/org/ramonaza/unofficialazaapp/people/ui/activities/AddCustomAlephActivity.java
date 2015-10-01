package org.ramonaza.unofficialazaapp.people.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.ui.fragments.AddCustomAlephFragment;

public class AddCustomAlephActivity extends BaseActivity {


    public static final String EXTRA_PARENT_ACTIVITY = "parent activity";
    private Class<? extends Activity> parentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Add Aleph...");
        Intent callingIntent = getIntent();
        parentActivity = (Class<? extends Activity>) callingIntent.getSerializableExtra(EXTRA_PARENT_ACTIVITY);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, AddCustomAlephFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backIntent = new Intent(this, parentActivity);
                startActivity(backIntent);
        }
        return super.onOptionsItemSelected(item);
    }

}
