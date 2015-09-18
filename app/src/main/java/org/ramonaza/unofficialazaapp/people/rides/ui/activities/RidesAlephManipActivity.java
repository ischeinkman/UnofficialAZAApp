package org.ramonaza.unofficialazaapp.people.rides.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.fragments.RidesAlephManipFragment;

public class RidesAlephManipActivity extends BaseActivity{

    private int alephID;
    public static final String EXTRA_ALEPHID="org.ramonaza.unofficialazaapp.ALEPH_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Intent intent=getIntent();
        alephID=intent.getIntExtra(EXTRA_ALEPHID,0);
        if(alephID == 0 && savedInstanceState != null) alephID=savedInstanceState.getInt(EXTRA_ALEPHID, 0);
        FragmentManager fragmentManager=getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, RidesAlephManipFragment.newInstance(alephID)).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ALEPHID, alephID);
    }

}
