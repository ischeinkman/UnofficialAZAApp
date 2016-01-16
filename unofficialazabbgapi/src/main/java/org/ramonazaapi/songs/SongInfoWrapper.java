package org.ramonazaapi.songs;

import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * Created by ilanscheinkman on 4/28/15.
 */
public class SongInfoWrapper implements InfoWrapper {

    private final String name;
    private final String lyrics;
    private final int id;

    public SongInfoWrapper(String name, String lyrics) {
        this(-1, name, lyrics);
    }

    public SongInfoWrapper(int id, String name, String lyrics) {
        this.id = id;
        this.name = name;
        this.lyrics = lyrics;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLyrics() {
        return lyrics;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * lyrics.hashCode();
    }
}
