package org.ramonaza.unofficialazaapp.people.backend;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ilanscheinkman on 9/3/15.
 */
public class ContactCSVSupport {


    private static final String CSV_NAME = "AlephNameSchYAddMailNum.csv";
    private static final String DEFAULT_CSV_NAME="DefaultContactFileTemplate.csv";
    public static final String FOLDER_PREFIX="Chapter Pack";

    private ContactCSVSupport(){
    }

    private static File getCSVFile(){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] ddFiles = downloadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(FOLDER_PREFIX);

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
        File[] contactFile=packFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(CSV_NAME);
            }
        });
        if (contactFile.length > 0) return contactFile[0];
        else return null;
    }

    private static InputStream getContactReader(Context context){
        File csvFile=getCSVFile();
        if(csvFile != null) try {
            return new FileInputStream(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            return context.getAssets().open(DEFAULT_CSV_NAME);
        } catch (IOException e) {
            return null;
        }
    }

    public static ContactCSVHandler getCSVHandler(Context context){
        File csvFile=getCSVFile();
        if(csvFile != null) try {
            return new ContactCSVHandler(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new ContactCSVHandler(getContactReader(context));
    }
}
