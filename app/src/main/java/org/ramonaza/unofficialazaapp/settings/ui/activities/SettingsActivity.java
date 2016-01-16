package org.ramonaza.unofficialazaapp.settings.ui.activities;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.events.backend.services.EventNotificationService;
import org.ramonaza.unofficialazaapp.events.backend.services.EventUpdateService;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonaza.unofficialazaapp.settings.ui.fragments.ChapterPackSelectorFragment;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getPreferenceManager().setSharedPreferencesName(PreferenceHelper.PREF_FILE_TITLE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        Preference loadPackButton = findPreference("loadpackbutton");
        if (loadPackButton != null) {
            loadPackButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = new ChapterPackSelectorFragment();
                    newFragment.show(ft, "dialog");

                    return true;
                }
            });
        }

        if (PreferenceHelper.getPreferences(this).isDebugMode()) {
            Button chapterPackButton = new Button(this);
            chapterPackButton.setText(getString(R.string.pref_title_create_pack_button));
            chapterPackButton.setBackgroundResource(R.drawable.general_textbutton_layout);
            chapterPackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new PackCreator().execute();
                }
            });
            ListView view = getListView();
            view.addFooterView(chapterPackButton);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    protected void onPause() {
        EventUpdateService.cancelRepeater(this);
        EventNotificationService.cancelNotifications(this);
        EventUpdateService.startRepeater(this);
        EventNotificationService.setUpNotifications(this);
        super.onPause();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            Preference loadPackButton = findPreference("loadpackbutton");
            if (loadPackButton != null) {
                loadPackButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        DialogFragment newFragment = new ChapterPackSelectorFragment();
                        newFragment.show(ft, "dialog");

                        return true;
                    }
                });
            }
        }
    }

    private class PackCreator extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return ChapterPackHandlerSupport.createChapterPack(SettingsActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(SettingsActivity.this, aBoolean.toString(), Toast.LENGTH_LONG);
        }
    }
}
