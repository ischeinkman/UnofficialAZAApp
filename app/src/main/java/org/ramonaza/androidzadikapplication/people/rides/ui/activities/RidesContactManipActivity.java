package org.ramonaza.androidzadikapplication.people.rides.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.ui.activities.BaseActivity;
import org.ramonaza.androidzadikapplication.people.rides.ui.fragments.RidesContactManipFragment;

public class RidesContactManipActivity extends BaseActivity {

    public static final String EXTRA_CONTACTID = "org.ramonaza.androidzadikapplication.CONTACT_ID";
    private int contactID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent intent = getIntent();
        contactID = intent.getIntExtra(EXTRA_CONTACTID, 0);
        if (contactID == 0 && savedInstanceState != null)
            contactID = savedInstanceState.getInt(EXTRA_CONTACTID, 0);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, RidesContactManipFragment.newInstance(contactID)).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CONTACTID, contactID);
    }

}
