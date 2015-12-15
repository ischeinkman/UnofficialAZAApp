package org.ramonazaapi.songs;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ilan on 12/5/15.
 */
public abstract class SongList {

    /**
     * Get all song titles in the song list.
     *
     * @return all song titles in the list
     */
    public abstract Collection<String> allTitles();

    /**
     * Get lyrics of a specific song.
     *
     * @param songTitle the title of the song
     * @return the song lyrics
     */
    public abstract String getLyrics(String songTitle);

    public SongInfoWrapper getSong(String title) {
        return new SongInfoWrapper(title, getLyrics(title));
    }

    public SongInfoWrapper[] getAllSongs() {
        ArrayList<SongInfoWrapper> allSongs = new ArrayList<>();
        for (String title : allTitles()) {
            allSongs.add(new SongInfoWrapper(title, getLyrics(title)));
        }
        return allSongs.toArray(new SongInfoWrapper[allSongs.size()]);
    }
}
