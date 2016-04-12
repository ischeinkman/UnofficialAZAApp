package org.ramonaza.unofficialazaapp.people.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonazaapi.contacts.ContactInfoWrapper;


/**
 * An object for easily manipulating the Contact-Rides database.
 * Created by ilanscheinkman on 7/16/15.
 */
public class ContactDatabaseHandler {

    protected SQLiteDatabase db;

    /**
     * Creates a handler based on a context.
     *
     * @param context the context to retrieve the database from
     */
    public ContactDatabaseHandler(Context context) {
        AppDatabaseHelper dbHelper = new AppDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Creates a handler based on a preexisting database.
     *
     * @param db the database to use
     */
    public ContactDatabaseHandler(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Retrieve the ContactInfoWrapper objects from raw cursor data.
     *
     * @param queryResults the cursor to read from
     * @return the retrieved contacts
     */
    public static ContactInfoWrapper[] getContactsFromCursor(Cursor queryResults) {
        if (queryResults.getCount() == 0) {
            return new ContactInfoWrapper[0];
        }
        ContactInfoWrapper[] contacts = new ContactInfoWrapper[queryResults.getCount()];
        int i = 0;
        queryResults.moveToFirst();
        do {
            ContactInfoWrapper temp = new ContactInfoWrapper();
            temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable._ID)));
            temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_NAME)));
            temp.setSchool(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_SCHOOL)));
            temp.setPhoneNumber(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_PHONE)));
            temp.setGradYear(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR)));
            temp.setEmail(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_EMAIL)));
            temp.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_ADDRESS)));
            temp.setArea(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_AREA)));
            temp.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_LATITUDE)));
            temp.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_LONGITUDE)));
            if (queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_PRESENT)) == 1) {
                temp.setPresent(true);
            } else if (queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.ContactListTable.COLUMN_PRESENT)) == 0) {
                temp.setPresent(false);
            }
            contacts[i] = temp;
            i++;
        } while (queryResults.moveToNext());
        return contacts;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        db.close();
    }

    /**
     * Adds a contact to the database.
     *
     * @param toAdd the contact to add
     * @throws ContactCSVReadError
     */
    public void addContact(ContactInfoWrapper toAdd) throws ContactCSVReadError {
        ContentValues value = new ContentValues();
        value.put(AppDatabaseContract.ContactListTable.COLUMN_NAME, toAdd.getName());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_ADDRESS, toAdd.getAddress());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_EMAIL, toAdd.getEmail());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR, toAdd.getGradYear());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_PHONE, toAdd.getPhoneNumber());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_SCHOOL, toAdd.getSchool());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, toAdd.isPresent());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_LATITUDE, toAdd.getX());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_LONGITUDE, toAdd.getY());
        long rowId = db.insert(AppDatabaseContract.ContactListTable.TABLE_NAME, null, value);
        if (rowId == -1l) throw new ContactCSVReadError("Null Contact Read", toAdd);
        else toAdd.setId((int) rowId);
    }

    /**
     * Updates a preexisting contact in the database.
     *
     * @param toUpdate the contact to update
     * @throws ContactCSVReadError
     */
    public void updateContact(ContactInfoWrapper toUpdate) throws ContactCSVReadError {
        ContentValues value = new ContentValues();
        value.put(AppDatabaseContract.ContactListTable.COLUMN_NAME, toUpdate.getName());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_ADDRESS, toUpdate.getAddress());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_EMAIL, toUpdate.getEmail());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR, toUpdate.getGradYear());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_PHONE, toUpdate.getPhoneNumber());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_SCHOOL, toUpdate.getSchool());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_LATITUDE, toUpdate.getX());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_LONGITUDE, toUpdate.getY());
        value.put(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, toUpdate.isPresent());
        long rowId = db.update(AppDatabaseContract.ContactListTable.TABLE_NAME, value,
                AppDatabaseContract.ContactListTable._ID + "=?", new String[]{"" + toUpdate.getId()});
        if (rowId == -1l) throw new ContactCSVReadError("Null Contact Read", toUpdate);
    }

    /**
     * Delete a contact by its ID.
     *
     * @param toDelete the ID of the contact to delete
     */
    public void deleteContact(int toDelete) {
        db.delete(AppDatabaseContract.ContactListTable.TABLE_NAME, "?=?", new String[]{
                AppDatabaseContract.ContactListTable._ID,
                "" + toDelete
        });
    }

    /**
     * Delete multiple contacts.
     *
     * @param whereClauses an SQL string detailing the where clause using android's native format
     * @param whereArgs    an array of arguements for the where clauses
     */
    public void deleteContacts(@Nullable String whereClauses, @Nullable String[] whereArgs) {
        db.delete(AppDatabaseContract.ContactListTable.TABLE_NAME, whereClauses, whereArgs);
    }

    /**
     * Get a contact by its ID.
     *
     * @param id the ID of the contact
     * @return the retrieved contact
     */
    public ContactInfoWrapper getContact(int id) {
        String query = String.format("SELECT * FROM %s WHERE %s=%d LIMIT 1",
                AppDatabaseContract.ContactListTable.TABLE_NAME,
                AppDatabaseContract.ContactListTable._ID,
                id
        );
        Cursor cursor = db.rawQuery(query, null);
        ContactInfoWrapper[] contactArray = getContactsFromCursor(cursor);
        return contactArray[0];
    }

    /**
     * Update a certain field on a group of contacts.
     *
     * @param field    the field to update
     * @param value    the value to update to
     * @param toUpdate the contacts to update. If toUpdate is set to null, update all contacts.
     */
    public void updateContactField(String field, String value, @Nullable ContactInfoWrapper[] toUpdate) {
        String query;
        if (toUpdate == null) {
            query = String.format("UPDATE %s SET %s=%s ",
                    AppDatabaseContract.ContactListTable.TABLE_NAME,
                    field,
                    value);
        } else if (toUpdate.length > 0) {
            query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                    AppDatabaseContract.ContactListTable.TABLE_NAME,
                    field,
                    value,
                    AppDatabaseContract.ContactListTable._ID);
            for (ContactInfoWrapper contact : toUpdate) query += contact.getId() + ", ";
            query = query.substring(0, query.length() - 2);
            query += ")";
        } else {
            return;
        }
        db.execSQL(query, new String[0]);
    }

    /**
     * Update a certain field on a group of contacts by their IDs.
     *
     * @param field    the field to update
     * @param value    the value to update to
     * @param toUpdate an array containing the IDs of the contacts to update. If toUpdate is set to null, update all contacts.
     */
    public void updateContactFieldByIDs(String field, String value, @Nullable int[] toUpdate) {
        String query;
        if (toUpdate == null) {
            query = String.format("UPDATE %s SET %s=%s ",
                    AppDatabaseContract.ContactListTable.TABLE_NAME,
                    field);
        } else if (toUpdate.length > 0) {
            query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                    AppDatabaseContract.ContactListTable.TABLE_NAME,
                    field,
                    value,
                    AppDatabaseContract.ContactListTable._ID);
        } else {
            return;
        }
        for (int contact : toUpdate) query += contact + ", ";
        query = query.substring(0, query.length() - 2);
        query += ")";
        db.execSQL(query, new String[0]);
    }

    /**
     * Retrieves contacts from the database.
     *
     * @param whereclauses a string array containing raw SQL where clauses
     * @param orderBy      an SQL string dictating the order to return the contacts in
     * @return the retrieved contacts
     */
    public ContactInfoWrapper[] getContacts(@Nullable String[] whereclauses, @Nullable String orderBy) {
        String query = String.format("SELECT * FROM %s ", AppDatabaseContract.ContactListTable.TABLE_NAME);
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
        public ContactCSVReadError(String errorMessage, ContactInfoWrapper erroredContact) {
            super(String.format("%s ON %s", errorMessage, erroredContact));

        }
    }
}
