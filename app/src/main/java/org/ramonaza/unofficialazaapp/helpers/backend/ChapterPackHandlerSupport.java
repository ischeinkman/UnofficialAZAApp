package org.ramonaza.unofficialazaapp.helpers.backend;


import android.content.Context;
import android.os.Environment;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.LocationSupport;
import org.ramonazaapi.chapterpacks.ChapterPackHandler;
import org.ramonazaapi.contacts.ContactCSVHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

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
        return currentHandler;
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
        /*SharedPreferences preferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PreferenceHelper.PREF_CHAPTERPACK, pack.getName());*/
        currentHandler = new ChapterPackHandler(pack);
        PreferenceHelper.getPreferences(context).putChapterPackName(pack.getName()).putEventFeed(currentHandler.getEventUrl());
        contactsLoaded = false;
        return currentHandler;
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
        String packName = (!PreferenceHelper.getPreferences(context).getChapterPackName().equals(""))
                ? PreferenceHelper.getPreferences(context).getChapterPackName()
                : "Chapter Pack :";
        String url = PreferenceHelper.getPreferences(context).getEventFeed();

        if (packName == null || url == null || packName.equals("") || url.equals("")) return false;
        ContactDatabaseHandler handler = getContactHandler(context);
        if (handler == null) return false;
        ContactInfoWrapper[] allContacts = handler.getContacts(null, AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC");

        //Corrects any location errors
        for (ContactInfoWrapper contact : allContacts) {
            if (contact.getX() == 0 && contact.getY() == 0) {
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
