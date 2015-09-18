package org.ramonaza.unofficialazaapp.events.backend;

import android.content.Context;
import android.os.Environment;

import org.ramonaza.unofficialazaapp.people.backend.ContactCSVSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

/**
 * Created by ilan on 9/17/15.
 */
public class EventRSSSupport {

    private static final String EVENT_FILE_NAME="EventFeed.txt";

    private EventRSSSupport(){

    }

    private static File getEventFile(){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] ddFiles = downloadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(ContactCSVSupport.FOLDER_PREFIX);

            }
        });
        File packFolder=null;
        for(File dirDest:ddFiles){
            if(dirDest.isDirectory()){
                packFolder=dirDest;
                break;
            }
        }
        if(packFolder == null) return null;
        File[] eventFile=packFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(EVENT_FILE_NAME);
            }
        });
        if (eventFile.length > 0) return eventFile[0];
        else return null;
    }

    private static String readUrlFromFile(File file){
        if(file == null) return null;
        Scanner fileStream;
        try {
            fileStream=new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        StringBuilder builder= new StringBuilder(1000);
        while(fileStream.hasNext()){
            builder.append(fileStream.next());
        }
        return builder.toString();
    }

    public static EventRSSHandler getEventRSSHandler(Context context){
        return new EventRSSHandler(readUrlFromFile(getEventFile()), true);
    }
}
