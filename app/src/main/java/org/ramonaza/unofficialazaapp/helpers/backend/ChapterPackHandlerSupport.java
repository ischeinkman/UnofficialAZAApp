package org.ramonaza.unofficialazaapp.helpers.backend;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.LocationSupport;
import org.ramonazaapi.chapterpacks.ChapterPackHandler;
import org.ramonazaapi.contacts.ContactCSVHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.events.EventRSSHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by ilan on 9/18/15.
 */
public class ChapterPackHandlerSupport {

    public static final String NEW_PACK_DIRECTORY = "Generated Packs/";
    public static final String PREF_CHAPTERPACK = "Cpack";
    public static final String PREF_EVENT_FEED = "EventFeed";
    private static ChapterPackHandler currentHandler;
    private static boolean contactsLoaded = false;

    /**
     * Check if we currently have a Chapter Pack leaded into the app.
     *
     * @return if we have a loaded Chapter Pack
     */
    public static boolean chapterPackIsLoaded() {
        return currentHandler != null;
    }

    /**
     * Get all possible Chapter Pack files in the Download directory.
     *
     * @return an array containing all Chapter Pack files
     */
    public static File[] getOptions() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] ddFiles = downloadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.contains(ChapterPackHandler.PREFIX)
                        && (dir.isDirectory()
                        || filename.contains(".zip")));

            }
        });
        return ddFiles;
    }

    /**
     * Gets the current Chapter Pack.
     * If one currently exists in the Downloads directory, we get that pack.
     * If no pack is or could be loaded, we return null.
     *
     * @param context the context to use
     * @return the currently loaded Chapter Pack, or null
     */
    public static ChapterPackHandler getChapterPackHandler(Context context) {
        /*if (getOptions().length > 0) {
            contactsLoaded = false;
            return getChapterPackHandler(context, getOptions()[0]);
        }
        else*/
        if (currentHandler != null) return currentHandler;
        else return null;
    }

    /**
     * Loads the current Chapter Pack from a given File object.
     *
     * @param context the context to use
     * @param pack    the file to load the pack from. Can either be a
     *                folder or zip file.
     * @return the current Chapter Pack
     */
    public static ChapterPackHandler getChapterPackHandler(Context context, File pack) {
        if (currentHandler != null && currentHandler.getPackName().equals(pack.getName()))
            return currentHandler;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(PREF_CHAPTERPACK, pack.getName());
        currentHandler = new ChapterPackHandler(pack);
        contactsLoaded = false;
        return currentHandler;
    }

    private static File moveChapterPack(Context context, File pack) {
        File dataDir = context.getExternalFilesDir(null);
        File newFile;
        if (pack.getName().contains(".zip")) {
            newFile = new File(dataDir, "lastloadedpack.zip");
            if (newFile.exists()) newFile.delete();
            boolean renamed = pack.renameTo(newFile);
            if (!renamed) return null;
            return newFile;
        } else {
            newFile = new File(dataDir, "lastloadedpack");
            if (!newFile.exists() || newFile.list().length == 0) {
                boolean mkdired = newFile.mkdirs();
                boolean deleted = newFile.delete();
            } else {
                for (File prevPack : newFile.listFiles()) {
                    boolean deleted = prevPack.delete();
                }
            }
            boolean renamed = pack.renameTo(newFile);
            if (!(renamed)) return null;
            return newFile;
        }
    }

    /**
     * Gets the current EventRSSHandler from the environment.
     * First checks if we have a newly loaded Chapter Pack, and if we
     * do return the pack's event handler. If we have no pack, we attempt
     * to retrieve a previous pack's feed from the app's preferences.
     * Failing that we return null.
     *
     * @param context the context to use
     * @return the currently loaded event handler, or null
     */
    public static EventRSSHandler getEventHandler(Context context) {
        if (currentHandler != null || getChapterPackHandler(context) != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE).edit();
            editor.putString(PREF_EVENT_FEED, currentHandler.getEventUrl());
            editor.commit();
            //PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_EVENT_FEED, currentHandler.getEventUrl());
            return currentHandler.getEventRSSHandler();
        }
        String url = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_EVENT_FEED, null);
        if (url == null) return null;
        return new EventRSSHandler(url, true);
    }

    /**
     * Gets the current ContactDatabaseHandler from the environment.
     * First checks if we have a newly loaded Chapter Pack, and if we do
     * return the pack's handler. If not we return a handler created from
     * the provided context.
     *
     * @param context the context to use
     * @return the currently loaded contact handler
     */
    public static ContactDatabaseHandler getContactHandler(Context context) {
        ContactDatabaseHandler returnHandler = new ContactDatabaseHandler(context);
        if (!contactsLoaded &&
                (currentHandler != null || getChapterPackHandler(context) != null) &&
                currentHandler.getCsvHandler() != null) {
            ContactInfoWrapper[] inPack = currentHandler.getCsvHandler().getCtactInfoListFromCSV();
            if (!(inPack.length <= 0)) {
                returnHandler.deleteContacts(null, null);
                for (ContactInfoWrapper contact : inPack) {
                    try {
                        returnHandler.addContact(contact);
                    } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                        contactCSVReadError.printStackTrace();
                    }
                }
            }
        }
        return returnHandler;
    }

    /**
     * Loads all the data from the current Chapter Pack into the
     * app's storage.
     *
     * @param context the context to use
     * @return whether the loading was successful or not
     */
    public static boolean loadChapterPack(Context context) {
        if (currentHandler == null) getChapterPackHandler(context);
        return (currentHandler.loadEventFeed() && currentHandler.loadContactList());
    }

    public static boolean createChapterPack(Context context) {

        //Build the chapter packs data
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String packName = preferences.getString(PREF_CHAPTERPACK, "Chapter Pack :");
        String url = preferences.getString(PREF_EVENT_FEED, "");
        if (packName == null || url == null || packName.equals("") || url.equals("")) return false;
        ContactDatabaseHandler handler = getContactHandler(context);
        if (handler == null) return false;
        ContactInfoWrapper[] allContacts = handler.getContacts(null, AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");

        //Corrects any location errors
        for (ContactInfoWrapper contact : allContacts) {
            if (contact.getLatitude() == 0 && contact.getLongitude() == 0) {
                double[] coords = LocationSupport.getCoordsFromAddress(contact.getAddress(), context);
                contact.setLatitude(coords[0]);
                contact.setLongitude(coords[1]);
                try {
                    handler.updateContact(contact);
                } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                    contactCSVReadError.printStackTrace();
                }
            }
        }

        //Create the necessary files and streams
        Calendar cal = Calendar.getInstance();
        String suffix = String.format(
                " -- %d - %d - %d - %d:%d:%d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
        );
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File generalPackDir = new File(downloadDir, NEW_PACK_DIRECTORY);
        File newPackDir = new File(generalPackDir, packName + suffix);
        File eventDataFile = new File(newPackDir, ChapterPackHandler.EVENT_FILE_NAME);
        File contactDataFile = new File(newPackDir, ChapterPackHandler.CSV_NAME);
        try {
            boolean rootCreated = newPackDir.mkdirs();
            boolean eventFileCreated = eventDataFile.createNewFile();
            boolean contactFileCreated = contactDataFile.createNewFile();
            if (!(rootCreated && eventFileCreated && contactFileCreated)) return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        BufferedWriter eventWriter;
        try {
            eventWriter = new BufferedWriter(new FileWriter(eventDataFile));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //Write the event information
        try {
            eventWriter.write(url);
            eventWriter.flush();
            eventWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //Write the contact information
        try {
            ContactCSVHandler csvWritingHandler = new ContactCSVHandler(contactDataFile);
            csvWritingHandler.writesContactsToCSV(allContacts, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
