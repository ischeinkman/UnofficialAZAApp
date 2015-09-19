package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.ramonaza.unofficialazaapp.events.backend.EventRSSHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by ilan on 9/18/15.
 */
public class ChapterPackHandlerSupport {

    private static ChapterPackHandler currentHandler;

    public static File[] getOptions(){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] ddFiles = downloadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.contains(ChapterPackHandler.PREFIX)
                        &&(dir.isDirectory()
                        || filename.contains(".zip")));

            }
        });
        return ddFiles;
    }

    public static ChapterPackHandler getChapterPackHandler(Context context){
        if(currentHandler != null) return currentHandler;
        String packName= PreferenceManager.getDefaultSharedPreferences(context).getString(ChapterPackHandler.PREF_CHAPTERPACK, null);
        if(packName == null) return null;
        for(File file : getOptions()){
            if(file.getName().equals(packName)){
                currentHandler= new ChapterPackHandler(file, context);
                return currentHandler;
            }
        }
        return null;
    }

    public static ChapterPackHandler getChapterPackHandler(Context context, File pack){
        if(currentHandler != null && currentHandler.getPackName().equals(pack.getName())) return currentHandler;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ChapterPackHandler.PREF_CHAPTERPACK, pack.getName());
        currentHandler=new ChapterPackHandler(pack, context);
        return currentHandler;
    }

    public static EventRSSHandler getEventHandler(Context context){
        if(currentHandler == null) getChapterPackHandler(context);
        return currentHandler.getEventRSSHandler();
    }

    public static ContactDatabaseHandler getContactHandler(Context context){
        if(currentHandler == null) getChapterPackHandler(context);
        return currentHandler.getContactDatabase();
    }

    public static boolean loadChapterPack(Context context){
        if(currentHandler == null) getChapterPackHandler(context);
        return (currentHandler.loadEventFeed() && currentHandler.loadContactList());
    }
}
