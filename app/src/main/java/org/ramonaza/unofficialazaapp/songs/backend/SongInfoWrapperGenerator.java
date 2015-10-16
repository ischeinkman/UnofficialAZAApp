package org.ramonaza.unofficialazaapp.songs.backend;

import android.content.Context;

import org.ramonazaapi.songs.SongInfoWrapper;


/**
 * Created by ilanscheinkman on 4/28/15.
 */
public abstract class SongInfoWrapperGenerator {

    /**
     * An array containing the names of every single song in the app in order of display.
     */
    public static final String[] allSongNames = {
            "Up You Men",
            "For Tomorow And Today",
            "World of AZA",
            "AZA All The Way",
            "Proud To Be An Aleph",
            "El HaMaayan",
            "Never Too Many",
            "Gentlemen"
    };

    /**
     * Get the song object from a song name.
     *
     * @param name    the name of the song
     * @param context the context to use
     * @return the SongInfoWrapper of this song
     */
    public static SongInfoWrapper fromName(String name, Context context) {
        String lyrics = getStringResourceByName(name.toLowerCase().replace(" ", "") + "text", context);
        return new SongInfoWrapper(name, lyrics);
    }

    /**
     * Get all the songs available in the app.
     *
     * @param context the context to use
     * @return every available SongInfoWrapper based on {@link #allSongNames}
     */
    public static SongInfoWrapper[] allSongs(Context context) {
        int totalSongs = allSongNames.length;
        SongInfoWrapper[] songs = new SongInfoWrapper[totalSongs];
        for (int i = 0; i < totalSongs; i++) {
            songs[i] = fromName(allSongNames[i], context);
        }
        return songs;
    }

    private static String getStringResourceByName(String aString, Context context) {
        String packageName = "org.ramonaza.unofficialazaapp";
        int resId = context.getResources().getIdentifier(aString, "string", packageName);
        return context.getString(resId);
    }
}
