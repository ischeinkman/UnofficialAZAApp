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
import org.ramonaza.unofficialazaapp.colorbook.ui.fragments.ColorBookFragment;
import org.ramonaza.unofficialazaapp.events.ui.fragments.EventListFragment;
import org.ramonaza.unofficialazaapp.frontpage.ui.fragments.NavigationDrawerFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.RidesActivity;
import org.ramonaza.unofficialazaapp.people.ui.fragments.ContactListFragment;
import org.ramonaza.unofficialazaapp.songs.ui.fragments.SongListFragment;


public class FrontalActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ColorBookFragment.ColorBookCallbacks {

    public static final String EXTRA_OPENEDPAGE = "org.ramonaza.unofficialazaapp.OPENED_PAGE";
    public static final int EVENTS_PAGE_INDEX = 0;
    public static final int SONGS_PAGE_INDEX = 1;
    public static final int BLUEBOOK_PAGE_INDEX = 2;
    public static final int CONTACTS_PAGE_INDEX = 3;
    public static final int RIDES_LINK_INDEX = 4;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SharedPreferences sharedPrefs;
    private int fragSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_front_page);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        Intent intent = getIntent();
        int pgVal = intent.getIntExtra(EXTRA_OPENEDPAGE, 0);
        if (pgVal == 0 && savedInstanceState != null) {
            pgVal = savedInstanceState.getInt(EXTRA_OPENEDPAGE, 0);
        }
        this.fragSwitch = pgVal;

        mTitle = getActionBarTitle(fragSwitch);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        switch (fragSwitch) {
            case EVENTS_PAGE_INDEX:
                getFragmentManager().beginTransaction().replace(R.id.container, EventListFragment.newInstance(0)).commit();
                break;
            case SONGS_PAGE_INDEX:
                getFragmentManager().beginTransaction().replace(R.id.container, SongListFragment.newInstance(1)).commit();
                break;
            case BLUEBOOK_PAGE_INDEX:
                getFragmentManager().beginTransaction().replace(R.id.container, ColorBookFragment.newInstance()).commit();
                break;
            case CONTACTS_PAGE_INDEX:
                getFragmentManager().beginTransaction().replace(R.id.container, ContactListFragment.newInstance(2)).commit();
                break;

        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.fragSwitch = savedInstanceState.getInt(EXTRA_OPENEDPAGE, 0);
        mTitle = getActionBarTitle(fragSwitch);
        switch (fragSwitch) {
            case EVENTS_PAGE_INDEX:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, EventListFragment.newInstance(0))
                        .commit();
                break;
            case SONGS_PAGE_INDEX:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, SongListFragment.newInstance(1))
                        .commit();
                break;
            case BLUEBOOK_PAGE_INDEX:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, ColorBookFragment.newInstance())
                        .commit();
                break;
            case CONTACTS_PAGE_INDEX:
                getFragmentManager().beginTransaction().replace(R.id.container, ContactListFragment.newInstance(2)).commit();
                break;

        }
    }

    @Override
    protected void onResume() {
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
        if (position == EVENTS_PAGE_INDEX) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, EventListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch = EVENTS_PAGE_INDEX;
        } else if (position == SONGS_PAGE_INDEX) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SongListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch = SONGS_PAGE_INDEX;
        } else if (position == BLUEBOOK_PAGE_INDEX) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ColorBookFragment.newInstance())
                    .commit();
            fragSwitch = BLUEBOOK_PAGE_INDEX;
        } else if (position == CONTACTS_PAGE_INDEX) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ContactListFragment.newInstance(position + 1))
                    .commit();
            fragSwitch = CONTACTS_PAGE_INDEX;
        } else if (position == RIDES_LINK_INDEX && sharedPrefs.getBoolean("rides", false)) {
            Intent ridesIntent = new Intent(this, RidesActivity.class);
            startActivity(ridesIntent);
        }
        mTitle = getActionBarTitle(fragSwitch);
    }

    @Override
    public void setDrawerLockMode(int mode) {
        mNavigationDrawerFragment.setDrawerLockMode(mode);
    }

    public void onSectionAttached(int number) {
        mTitle = getActionBarTitle(number - 1);
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

    private CharSequence getActionBarTitle(int sectionNumber) {
        switch (sectionNumber) {
            case EVENTS_PAGE_INDEX:
                return getString(R.string.title_section1);
            case SONGS_PAGE_INDEX:
                return getString(R.string.title_section2);
            case BLUEBOOK_PAGE_INDEX:
                return getString(R.string.title_section_bluebook);
            case CONTACTS_PAGE_INDEX:
                return getString(R.string.title_section4);
            default:
                return getTitle();
        }
    }

    public void onUiHide() {
        if (!(getActionBar() == null)) getActionBar().hide();
        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void onUiShow() {
        if (!(getActionBar() == null)) getActionBar().show();
        setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}
