package org.ramonazaapi.songs;

import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * Created by ilanscheinkman on 4/28/15.
 */
public class SongInfoWrapper implements InfoWrapper {

    private String name;
    private String lyrics;

    public SongInfoWrapper(String name, String lyrics) {
        this.name = name;
        this.lyrics = lyrics;
    }

    @Override
    public int getId() {
        return name.hashCode() * lyrics.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLyrics() {
        return lyrics;
    }


}
