package org.ramonaza.unofficialazaapp.people.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

/**
 * Created by ilanscheinkman on 7/16/15.
 */
public class ContactDatabaseHandler {

    private SQLiteDatabase db;

    public ContactDatabaseHandler(Context context) {
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public ContactDatabaseHandler(SQLiteDatabase db) {
        this.db = db;
    }

    public static ContactInfoWrapper[] getContactsFromCursor(Cursor queryResults) {
        if (queryResults.getCount() == 0) {
            return new ContactInfoWrapper[0];
        }
        ContactInfoWrapper[] alephs = new ContactInfoWrapper[queryResults.getCount()];
        int i = 0;
        queryResults.moveToFirst();
        do {
            ContactInfoWrapper temp = new ContactInfoWrapper();
            temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable._ID)));
            temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_NAME)));
            temp.setSchool(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_SCHOOL)));
            temp.setPhoneNumber(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_PHONE)));
            temp.setGradYear(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_GRADYEAR)));
            temp.setEmail(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_EMAIL)));
            temp.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_ADDRESS)));
            temp.setArea(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_AREA)));
            temp.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_LATITUDE)));
            temp.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_LONGITUDE)));
            if (queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT)) == 1) {
                temp.setPresent(true);
            } else if (queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT)) == 0) {
                temp.setPresent(false);
            }
            alephs[i] = temp;
            i++;
        } while (queryResults.moveToNext());
        return alephs;
    }

    public void close() {
        db.close();
    }

    public void addContact(ContactInfoWrapper toAdd) throws ContactCSVReadError {
        ContentValues value = new ContentValues();
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_NAME, toAdd.getName());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_ADDRESS, toAdd.getAddress());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_EMAIL, toAdd.getEmail());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_GRADYEAR, toAdd.getGradYear());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_PHONE, toAdd.getPhoneNumber());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_SCHOOL, toAdd.getSchool());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT, toAdd.isPresent());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_LATITUDE, toAdd.getLatitude());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_LONGITUDE, toAdd.getLongitude());
        long rowId = db.insert(ContactDatabaseContract.ContactListTable.TABLE_NAME, null, value);
        if (rowId == -1l) throw new ContactCSVReadError("Null Contact Read", toAdd);
        else toAdd.setId((int) rowId);
    }

    public void updateContact(ContactInfoWrapper toUpdate) throws ContactCSVReadError {
        ContentValues value = new ContentValues();
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_NAME, toUpdate.getName());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_ADDRESS, toUpdate.getAddress());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_EMAIL, toUpdate.getEmail());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_GRADYEAR, toUpdate.getGradYear());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_PHONE, toUpdate.getPhoneNumber());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_SCHOOL, toUpdate.getSchool());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_LATITUDE, toUpdate.getLatitude());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_LONGITUDE, toUpdate.getLongitude());
        value.put(ContactDatabaseContract.ContactListTable.COLUMN_PRESENT, toUpdate.isPresent());
        long rowId = db.update(ContactDatabaseContract.ContactListTable.TABLE_NAME, value,
                ContactDatabaseContract.ContactListTable._ID + "=?", new String[]{"" + toUpdate.getId()});
        if (rowId == -1l) throw new ContactCSVReadError("Null Contact Read", toUpdate);
    }

    public void deleteContact(int toDelete) {
        db.delete(ContactDatabaseContract.ContactListTable.TABLE_NAME, "?=?", new String[]{
                ContactDatabaseContract.ContactListTable._ID,
                "" + toDelete
        });
    }

    public void deleteContacts(@Nullable String whereClauses, @Nullable String[] whereArgs){
        db.delete(ContactDatabaseContract.ContactListTable.TABLE_NAME, whereClauses, whereArgs);
    }

    public ContactInfoWrapper getContact(int id){
        String query=String.format("SELECT * FROM %s WHERE %s=%d LIMIT 1",
                ContactDatabaseContract.ContactListTable.TABLE_NAME,
                ContactDatabaseContract.ContactListTable._ID,
                id
                );
        Cursor cursor=db.rawQuery(query, null);
        ContactInfoWrapper[] contactArray=getContactsFromCursor(cursor);
        return contactArray[0];
    }

    public void updateField(String field, String value, ContactInfoWrapper[] toUpdate) {
        String query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                ContactDatabaseContract.ContactListTable.TABLE_NAME,
                field,
                value,
                ContactDatabaseContract.ContactListTable._ID);
        for (ContactInfoWrapper aleph : toUpdate) query += aleph.getId() + ", ";
        query = query.substring(0, query.length() - 2);
        query += ")";
        db.execSQL(query, new String[0]);
    }

    public void updateFieldByIDs(String field, String value, int[] toUpdate) {
        String query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                ContactDatabaseContract.ContactListTable.TABLE_NAME,
                field,
                value,
                ContactDatabaseContract.ContactListTable._ID);
        for (int aleph : toUpdate) query += aleph + ", ";
        query = query.substring(0, query.length() - 2);
        query += ")";
        db.execSQL(query, new String[0]);
    }

    public ContactInfoWrapper[] getContacts(@Nullable String[] whereclauses, @Nullable String orderBy) {
        String query = String.format("SELECT * FROM %s ", ContactDatabaseContract.ContactListTable.TABLE_NAME);
        if (whereclauses != null && whereclauses.length > 0) {
            query += "WHERE ";
            for (String wc : whereclauses) query += " " + wc + " AND";
            query = query.substring(0, query.length() - 3);
        }
        if (orderBy != null) query += "ORDER BY " + orderBy;
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();
        return getContactsFromCursor(queryResults);
    }

    public class ContactCSVReadError extends Exception {
        public ContactCSVReadError(String errorMessage, ContactInfoWrapper erroredAleph) {
            super(String.format("%s ON %s", errorMessage, erroredAleph));

        }
    }
}
