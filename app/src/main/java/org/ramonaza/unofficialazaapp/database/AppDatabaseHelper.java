package org.ramonaza.unofficialazaapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.DatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.chapterpacks.ChapterPackHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

/**
 * A simple database helper for accessing the contact/rides database.
 * All database interactions should be handled by a separate handler.
 * Created by ilanscheinkman on 3/12/15.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ContactDriverDatabase";
    public static final int DATABASE_VERSION = 5;
    private Context context;

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppDatabaseContract.DriverListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.ContactListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.RidesListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.EventListTable.CREATE_TABLE);
        try {
            genDatabaseFromCSV();
        } catch (ContactCSVReadError contactCSVReadError) {
            contactCSVReadError.printStackTrace();
        }
    }

    public void onDelete(SQLiteDatabase db) {
        db.execSQL(AppDatabaseContract.DriverListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.ContactListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.RidesListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.EventListTable.DELETE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 4 && newVersion >= 5) {
            String addTableStatement = "ALTER TABLE " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                    " ADD " + AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO + " " + AppDatabaseContract.VTYPE_INT;
            db.execSQL(addTableStatement);
            String setDefault = "UPDATE " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                    " SET " + AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO + " =-1 ";
            db.execSQL(setDefault);
        }
    }

    public void genDatabaseFromCSV() throws ContactCSVReadError {
        ChapterPackHandler c = ChapterPackHandlerSupport.getChapterPackHandler(context);
        if (c != null && c.getCsvHandler() != null) {
            ContactInfoWrapper[] allInCSV = c.getCsvHandler().getCtactInfoListFromCSV();
            if (allInCSV.length <= 0) return;
            ContactDatabaseHandler handler = (ContactDatabaseHandler) DatabaseHandler.getHandler(ContactDatabaseHandler.class);
            handler.deleteContacts(null, null);
            for (ContactInfoWrapper inCsv : allInCSV) {
                try {
                    handler.addContact(inCsv);
                } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                    contactCSVReadError.printStackTrace();
                }
            }
        }

    }


    public class ContactCSVReadError extends Exception {
        public ContactCSVReadError(String errorMessage, ContactInfoWrapper erroredContact) {
            super(String.format("%s ON %s", errorMessage, erroredContact));

        }
    }
}
