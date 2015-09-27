package org.ramonaza.unofficialazaapp.people.backend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandler;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;

/**
 * A simple database helper for accessing the contact/rides database.
 * All database interactions should be handled by a separate handler.
 * Created by ilanscheinkman on 3/12/15.
 */
public class ContactDatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME="ContactDriverDatabase";
    public static final int DATABASE_VERSION=4;
    private Context context;

    public ContactDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContactDatabaseContract.DriverListTable.CREATE_TABLE);
        db.execSQL(ContactDatabaseContract.ContactListTable.CREATE_TABLE);
        db.execSQL(ContactDatabaseContract.RidesListTable.CREATE_TABLE);
        try {
            genDatabaseFromCSV(db);
        } catch (ContactCSVReadError contactCSVReadError) {
            contactCSVReadError.printStackTrace();
        }
    }
    public void onDelete(SQLiteDatabase db){
        db.execSQL(ContactDatabaseContract.DriverListTable.DELETE_TABLE);
        db.execSQL(ContactDatabaseContract.ContactListTable.DELETE_TABLE);
        db.execSQL(ContactDatabaseContract.RidesListTable.DELETE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ContactDatabaseContract.DELETE_TABLES);
        db.execSQL(ContactDatabaseContract.DriverListTable.CREATE_TABLE);
        db.execSQL(ContactDatabaseContract.ContactListTable.CREATE_TABLE);
        db.execSQL(ContactDatabaseContract.RidesListTable.CREATE_TABLE);
        try {
            genDatabaseFromCSV(db);
        } catch (ContactCSVReadError contactCSVReadError) {
            contactCSVReadError.printStackTrace();
        }
    }
    public void genDatabaseFromCSV(SQLiteDatabase db) throws ContactCSVReadError{
        ChapterPackHandler c = ChapterPackHandlerSupport.getChapterPackHandler(context);
        if(c != null) c.reLoadContactList(db);

    }

    
    public class ContactCSVReadError extends Exception{
        public ContactCSVReadError(String errorMessage, ContactInfoWrapper erroredAleph){
            super(String.format("%s ON %s",errorMessage,erroredAleph));

        }
    }
}
