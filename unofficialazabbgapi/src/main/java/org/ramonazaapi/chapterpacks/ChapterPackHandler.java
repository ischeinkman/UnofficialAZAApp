package org.ramonazaapi.chapterpacks;

import org.ramonazaapi.contacts.ContactCSVHandler;
import org.ramonazaapi.events.EventRSSHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by ilan on 9/17/15.
 */
public class ChapterPackHandler {

    public static final String PREFIX = "Chapter Pack";

    public static final String EVENT_FILE_NAME = "EventFeed.txt";

    public static final String CSV_NAME = "ContactList.csv";
    public static final String DEFAULT_CSV_NAME = "DefaultContactFileTemplate.csv";

    private File chapterPack;
    private boolean isZip;
    private boolean eventsLoaded;
    private boolean contactsLoaded;
    private String eventUrl;
    private ContactCSVHandler csvHandler;

    /**
     * This object is used to manipulate data in files/folders called
     * "Chapter Packs". These Chapter Packs contain event feed information
     * and contact information in either a zip file or directory, and can
     * then be loaded into the app's database and preferences.
     *  @param chapterPackFile the Chapter Pack file to read from
     *
     */
    public ChapterPackHandler(File chapterPackFile) {
        isZip = chapterPackFile.getName().contains(".zip");
        this.chapterPack = chapterPackFile;
    }

    /**
     * Get the name of the Chapter Pack.
     *
     * @return the Chapter Pack's name
     */
    public String getPackName() {
        return chapterPack.getName();
    }

    /**
     * Loads event feed information into app storage.
     *
     * @return whether the information was loaded successfully
     */
    public boolean loadEventFeed() {
        if (eventsLoaded) return true;
        if (isZip) eventsLoaded = loadZipEvents();
        else eventsLoaded = loadFolderEvents();
        return eventsLoaded;
    }

    private boolean loadZipEvents() {
        ZipFile file;
        try {
            file = new ZipFile(chapterPack);
        } catch (IOException e) {
            return false;
        }
        ZipEntry eventZipFile = file.getEntry(EVENT_FILE_NAME);
        InputStream fileStream;
        try {
            fileStream = file.getInputStream(eventZipFile);
        } catch (IOException e) {
            return false;
        }
        Scanner streamScanner = new Scanner(fileStream);
        StringBuilder builder = new StringBuilder(100);
        while (streamScanner.hasNext()) {
            builder.append(streamScanner.next());
        }
        eventUrl = builder.toString();
        return true;
    }

    private boolean loadFolderEvents() {
        File[] eventFile = chapterPack.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(EVENT_FILE_NAME);
            }
        });
        if (eventFile == null || eventFile.length <= 0) return false;
        Scanner fileStream;
        try {
            fileStream = new Scanner(eventFile[0]);
        } catch (FileNotFoundException e) {
            return false;
        }
        StringBuilder builder = new StringBuilder(1000);
        while (fileStream.hasNext()) {
            builder.append(fileStream.next());
        }
        eventUrl = builder.toString();
        return true;
    }

    /**
     * Loads contact information into app storage.
     *
     * @return whether the information was loaded successfully
     */
    public boolean loadContactList() {
        if (contactsLoaded) return true;
        csvHandler = (isZip) ? loadZipContactList() : loadFolderContactList();
        if (csvHandler == null) {
            contactsLoaded = false;
            return false;
        }
        contactsLoaded = true;
        return contactsLoaded;
    }

    private ContactCSVHandler loadZipContactList() {
        ZipFile file;
        try {
            file = new ZipFile(chapterPack);
        } catch (IOException e) {
            return null;
        }
        ZipEntry contactZipEntry = file.getEntry(CSV_NAME);
        InputStream fileStream;
        try {
            fileStream = file.getInputStream(contactZipEntry);
        } catch (IOException e) {
            return null;
        }
        return new ContactCSVHandler(fileStream);
    }

    private ContactCSVHandler loadFolderContactList() {
        File[] eventFile = chapterPack.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(CSV_NAME);
            }
        });
        if (eventFile.length <= 0) return null;
        try {
            return new ContactCSVHandler(eventFile[0]);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public ContactCSVHandler getCsvHandler() {
        return csvHandler;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    /**
     * Gets the handler to access and manipulate this pack's event feed
     * and the events at that feed.
     *
     * @return the event handler based on this pack's event feed
     */
    public EventRSSHandler getEventRSSHandler() {
        loadEventFeed();
        return new EventRSSHandler(eventUrl, true);
    }


}
