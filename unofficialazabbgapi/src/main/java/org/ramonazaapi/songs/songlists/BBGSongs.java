package org.ramonazaapi.songs.songlists;

import org.ramonazaapi.songs.SongList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilan on 12/5/15.
 */
public class BBGSongs extends SongList {

    public static final Map<String, String> SONGDATA = new HashMap<>();

    public BBGSongs() {
        SONGDATA.put("We Pledge To Thee",
                "We pledge to thee, oh BBG" +
                        "\nOur love, our youth, our loyalty" +
                        "\nWe sing to thee with joyous sounds " +
                        "\nOur voices reach the sky " +
                        "\nFrom Zion came the white and blue " +
                        "\nTo give these colors bold and true" +
                        "\nOur loyal daughters gather ‘round" +
                        "\nTo raise our flag on high"
        );
        SONGDATA.put("Identity",
                "Identity, Identity\n" +
                        "Who am I? I’m a BBG\n" + "" +
                        "I am one, though only one\n" +
                        "There’s so much I can do"

        );
        SONGDATA.put("Friends, Friends, Friends",
                "Friends, friends, friends\n" +
                        "We will always be\n" +
                        "Whether in fair or in dark stormy weather\n" +
                        "BBYO will keep us together\n" +
                        "White and blue\n" +
                        "We will ‘ere be true\n" +
                        "Love will pervade us\n" +
                        "‘Til death separate us\n" +
                        "We’re friends, friends, friends");

        SONGDATA.put("Honor And Courage",
                "Honor and courage to you\n" +
                        "Devotion and loyalty, too\n" +
                        "Memories of the days we recall\n" +
                        "We owe to you...\n\n" +
                        "Under the stars that shine bright\n" +
                        "Knowledge is our guiding light\n" +
                        "Lighting the way to a brighter day\n" +
                        "And come what may...\n\n" +
                        "Come disappointment or fear\n" +
                        "Our mighty foundation is here\n" +
                        "Nothing can break us\n" +
                        "No one can take us\n" +
                        "We stand strong...\n\n" +
                        "When are two Orders unite\n" +
                        "With our goals in sight\n" +
                        "B’nai B’rith is gleaming\n" +
                        "We go on dreaming\n" +
                        "Through the night...");

        SONGDATA.put("This Is Our Order",
                "This is our Order\n" +
                        "Greatest on Earth\n" +
                        "This is our Order\n" +
                        "Fraternal since birth\n" +
                        "We pledge thee our devotion\n" +
                        "Loudly we call\n" +
                        "For this is our Order\n" +
                        "Sisterhood for all\n" +
                        "Give a call (give a call)\n" +
                        "To them all (to them all)\n" +
                        "Who work for BBYO\n" +
                        "Let them sing (let them sing)\n" +
                        "And rejoice (and rejoice)\n" +
                        "And lift their voice\n" +
                        "For all B’nai B’rith Youth\n" +
                        "Five fold and full\n" +
                        "Will be for AZA\n" +
                        "Six fold and full\n" +
                        "Information\n" +
                        "For BBG the same\n" +
                        "Harmony, Benevolence, Fraternity\n" +
                        "For all B’nai B’rith Youth");

        SONGDATA.put("BBG Prep Song",
                "B’nai B’rith Girls\n" +
                        "We’re standing tall\n" +
                        "We’re one for all\n" +
                        "B’nai B’rith Girls\n" +
                        "We’re here to stay\n" +
                        "We’ll pave the way\n" +
                        "For all Jewish youth\n\n" +
                        "B’nai B’rith Girls\n" +
                        "We’re standing here as one (hey!)\n" +
                        "B’nai B’rith Girls\n" +
                        "For sisterhood and fun (hey!)\n\n" +
                        "For love and laughter\n" +
                        "Friends that last\n" +
                        "Times so good\n" +
                        "They go so fast\n" +
                        "We stand and sing with all our might\n" +
                        "B’nai B’rith Girls\n" +
                        "Our future’s so bright, so...\n\n" +
                        "B’nai B’rith Girls\n" +
                        "We’re standing here as one (hey!)\n" +
                        "B’nai B’rith Girls\n" +
                        "For sisterhood and fun (hey!)\n\n" +
                        "Together as one\n" +
                        "Forever united\n" +
                        "B’nai B’rith Girls!\n" +
                        "B’nai B’rith Girls!");
    }

    public Collection<String> allTitles() {
        return SONGDATA.keySet();
    }

    public String getLyrics(String songTitle) {
        return SONGDATA.get(songTitle);
    }


}
