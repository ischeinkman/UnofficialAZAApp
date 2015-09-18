package org.ramonaza.unofficialazaapp.frontpage.ui.activities;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.events.ui.fragments.EventListFragment;
import org.ramonaza.unofficialazaapp.frontpage.ui.fragments.NavigationDrawerFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesActivity;
import org.ramonaza.unofficialazaapp.people.ui.fragments.ContactListFragment;
import org.ramonaza.unofficialazaapp.songs.ui.fragments.SongListFragment;


public class FrontalActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    public static final String EXTRA_OPENEDPAGE="org.ramonaza.unofficialazaapp.OPENED_PAGE";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SharedPreferences sharedPrefs;
    private int fragSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_front_page);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        Intent intent=getIntent();
        int pgVal=intent.getIntExtra(EXTRA_OPENEDPAGE,0);
        if(pgVal == 0 && savedInstanceState != null){
            pgVal = savedInstanceState.getInt(EXTRA_OPENEDPAGE, 0);
        }
        this.fragSwitch=pgVal;

        mTitle = getActionBarTitle(fragSwitch);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        switch (fragSwitch){
            case 0:
                getFragmentManager().beginTransaction().replace(R.id.container, EventListFragment.newInstance(0)).commit();
                break;
            case 1:
                getFragmentManager().beginTransaction().replace(R.id.container,SongListFragment.newInstance(1)).commit();
                break;
            case 2:
                getFragmentManager().beginTransaction().replace(R.id.container,ContactListFragment.newInstance(2)).commit();
                break;

        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.fragSwitch=savedInstanceState.getInt(EXTRA_OPENEDPAGE, 0);
        mTitle=getActionBarTitle(fragSwitch);
        switch (fragSwitch){
            case 0:
                getFragmentManager().beginTransaction().replace(R.id.container, EventListFragment.newInstance(0)).commit();
                break;
            case 1:
                getFragmentManager().beginTransaction().replace(R.id.container,SongListFragment.newInstance(1)).commit();
                break;
            case 2:
                getFragmentManager().beginTransaction().replace(R.id.container,ContactListFragment.newInstance(2)).commit();
                break;

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getActionBarTitle(fragSwitch);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }



    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if (position == 0) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, EventListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch=0;
        } else if (position == 1) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SongListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch=1;
        } else if (position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ContactListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch=2;
        } else if(position ==3 &&sharedPrefs.getBoolean("rides",false)){
            Intent ridesIntent=new Intent(this,RidesActivity.class);
            startActivity(ridesIntent);
        }
    }

    public void onSectionAttached(int number) {
        mTitle=getActionBarTitle(number -1);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_default, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_OPENEDPAGE, fragSwitch);
    }

    private CharSequence getActionBarTitle(int sectionNumber){
        switch (sectionNumber){
            case 0:
                return getString(R.string.title_section1);
            case 1:
                return getString(R.string.title_section2);
            case 2:
                return getString(R.string.title_section4);
            default:
                return getTitle();
        }
    }
}
