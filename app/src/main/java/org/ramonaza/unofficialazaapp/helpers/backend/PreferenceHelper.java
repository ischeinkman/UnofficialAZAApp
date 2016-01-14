package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ilan on 1/13/16.
 */
public class PreferenceHelper {

    public static final String PREF_FILE_TITLE = "APP_PREFS";
    public static final String PREF_CHAPTERPACK = "Cpack";
    public static final String PREF_EVENT_FEED = "EventFeed";
    public static final String PREF_RIDES_MODE = "rides";
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String PREF_DEBUG_MODE = "admin";
    public static final String PREF_NOTIFY_BEFORE_TIME = "notifiybeforetime";

    private static PreferenceHelper mHelper;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private PreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_FILE_TITLE, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static PreferenceHelper getPreferences(Context context) {
        if (mHelper == null) {
            mHelper = new PreferenceHelper(context.getApplicationContext());
        }
        return mHelper;
    }

    public String getEventFeed() {
        return prefs.getString(PREF_EVENT_FEED, "");
    }

    public PreferenceHelper putEventFeed(String feed) {
        prefs.edit().putString(PREF_EVENT_FEED, feed).commit();
        return this;
    }

    public String getChapterPackName() {
        return prefs.getString(PREF_CHAPTERPACK, "");
    }

    public PreferenceHelper putChapterPackName(String name) {
        prefs.edit().putString(PREF_CHAPTERPACK, name).commit();
        return this;
    }

    public boolean isRidesMode() {
        return prefs.getBoolean(PREF_RIDES_MODE, false);
    }

    public PreferenceHelper setRidesMode(boolean ridesMode) {
        prefs.edit().putBoolean(PREF_RIDES_MODE, ridesMode).commit();
        return this;
    }

    public PreferenceHelper setUserLearnedDrawer(boolean userLearnedDrawer) {
        prefs.edit().putBoolean(PREF_USER_LEARNED_DRAWER, userLearnedDrawer).commit();
        return this;
    }

    public boolean hasUserLearnedDrawer() {
        return prefs.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    public boolean isDebugMode() {
        return prefs.getBoolean(PREF_DEBUG_MODE, false);
    }

    public PreferenceHelper setDebugMode(boolean debugMode) {
        prefs.edit().putBoolean(PREF_DEBUG_MODE, debugMode);
        return this;
    }

    public long getNotifyBeforeTime() {
        return prefs.getLong(PreferenceHelper.PREF_NOTIFY_BEFORE_TIME, 1000 * 60 * 30);
    }

    public PreferenceHelper setNotifyBeforeTime(long newTime) {
        prefs.edit().putLong(PreferenceHelper.PREF_NOTIFY_BEFORE_TIME, newTime).commit();
        return this;
    }
}
