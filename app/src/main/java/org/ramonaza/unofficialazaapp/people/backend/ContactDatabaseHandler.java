package org.ramonaza.unofficialazaapp.people.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.BaseDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * An object for easily manipulating the Contact-Rides database.
 * Created by ilanscheinkman on 7/16/15.
 */
public class ContactDatabaseHandler extends BaseDatabaseHandler<ContactInfoWrapper> {

    public ContactDatabaseHandler(Context context) {
        super(context);
    }

    public ContactDatabaseHandler(SQLiteDatabase db) {
        super(db);
    }

    public ContactDatabaseHandler(BaseDatabaseHandler other) {
        super(other);
    }

    /**
     * Retrieve the ContactInfoWrapper objects from raw cursor data.
     *
     * @param queryResults the cursor to read from
     * @return the retrieved contacts
     */
    public static Observable<ContactInfoWrapper> getContactsFromCursor(final Cursor queryResults) {
        if (queryResults.getCount() == 0) {
            return Observable.empty();
        }
        return Observable.create(new Observable.OnSubscribe<ContactInfoWrapper>() {
            @Override
            public void call(Subscriber<? super ContactInfoWrapper> subscriber) {
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
                    subscriber.onNext(temp);
                } while (queryResults.moveToNext());
            }
        }).subscribeOn(Schedulers.io()).publish().refCount();
    }

    private static Observable<ContentValues> prepareContactsForCursor(final ContactInfoWrapper... contacts) {
        return Observable.create(new Observable.OnSubscribe<ContentValues>() {
            @Override
            public void call(Subscriber<? super ContentValues> subscriber) {
                for (ContactInfoWrapper contact : contacts) {
                    ContentValues value = new ContentValues();
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_NAME, contact.getName());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_ADDRESS, contact.getAddress());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_EMAIL, contact.getEmail());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR, contact.getGradYear());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_PHONE, contact.getPhoneNumber());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_SCHOOL, contact.getSchool());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, contact.isPresent());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_LATITUDE, contact.getX());
                    value.put(AppDatabaseContract.ContactListTable.COLUMN_LONGITUDE, contact.getY());

                    subscriber.onNext(value);
                }
                subscriber.onCompleted();
            }
        });
    }
    /**
     * Closes the database connection.
     */
    public void close() {
        db.close();
    }

    public Observable<ContactInfoWrapper> addContacts(Collection<? extends ContactInfoWrapper> toAdd) {
        return addContacts(toAdd.toArray(new ContactInfoWrapper[0]));
    }

    /**
     * Adds a contact to the database.
     *
     * @param toAdd the contact to add
     */
    public Observable<ContactInfoWrapper> addContacts(final ContactInfoWrapper... toAdd) {

        return Observable.create(new Observable.OnSubscribe<ContactInfoWrapper>() {
            @Override
            public void call(final Subscriber<? super ContactInfoWrapper> subscriber) {
                try {
                    for (final ContactInfoWrapper contact : toAdd) {
                        prepareContactsForCursor(contact).map(new Func1<ContentValues, Long>() {

                            @Override
                            public Long call(ContentValues value) {
                                return db.insert(AppDatabaseContract.ContactListTable.TABLE_NAME, null, value);
                            }
                        }).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (aLong < 0) {
                                    subscriber.onError(new IOException("Contact not inserted."));
                                    return;
                                }
                                contact.setId(aLong.intValue());
                                subscriber.onNext(contact);
                            }
                        });
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Updates a preexisting contact in the database.
     *
     * @param toUpdate the contacts to update
     */
    public Observable<ContactInfoWrapper> updateContacts(final ContactInfoWrapper... toUpdate) {
        return Observable.create(new Observable.OnSubscribe<ContactInfoWrapper>() {
            @Override
            public void call(final Subscriber<? super ContactInfoWrapper> subscriber) {
                try {
                    for (final ContactInfoWrapper contact : toUpdate) {
                        prepareContactsForCursor(contact).map(new Func1<ContentValues, Long>() {
                            @Override
                            public Long call(ContentValues value) {
                                return (long) db.update(AppDatabaseContract.ContactListTable.TABLE_NAME, value,
                                        AppDatabaseContract.ContactListTable._ID + "=?", new String[]{"" + contact.getId()});
                            }
                        }).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (aLong < 0) {
                                    subscriber.onError(new IOException("Contact not updated."));
                                    return;
                                }
                                subscriber.onNext(contact);
                            }
                        });
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Delete a contact by its ID.
     *
     * @param toDelete the ID of the contact to delete
     */
    public Observable<Integer> deleteContacts(ContactInfoWrapper... toDelete) {
        return Observable.from(toDelete).map(new Func1<ContactInfoWrapper, Integer>() {
            @Override
            public Integer call(ContactInfoWrapper contactInfoWrapper) {
                return contactInfoWrapper.getId();
            }
        }).toList().first().map(new Func1<List<Integer>, Integer>() {
            @Override
            public Integer call(List<Integer> integers) {
                StringBuilder builder = new StringBuilder("(");
                for (Integer i : integers) builder.append(i + ",");
                builder.append(")");
                return db.delete(AppDatabaseContract.ContactListTable.TABLE_NAME, "? IN ?", new String[]{
                        AppDatabaseContract.ContactListTable._ID,
                        "" + builder.toString()});
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Delete multiple contacts.
     *
     * @param whereClauses an SQL string detailing the where clause using android's native format
     * @param whereArgs    an array of arguements for the where clauses
     */
    public Observable<Integer> deleteContacts(@Nullable final String whereClauses, @Nullable final String[] whereArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    int delVal = db.delete(AppDatabaseContract.ContactListTable.TABLE_NAME, whereClauses, whereArgs);
                    if (delVal < 0) subscriber.onError(new IOException("Delete failed."));
                    subscriber.onNext(delVal);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Get a contact by its ID.
     *
     * @return the retrieved contact
     */
    public Observable<ContactInfoWrapper> getContacts(final int... ids) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String query;
                    if (ids != null) {
                        String queryBase = String.format("SELECT * FROM %s WHERE %s IN (",
                                AppDatabaseContract.ContactListTable.TABLE_NAME,
                                AppDatabaseContract.ContactListTable._ID);
                        StringBuilder builder = new StringBuilder(queryBase);
                        for (int id : ids) {
                            builder.append(id + ",");
                        }
                        builder.append(")");
                        query = builder.toString();
                    } else {
                        query = String.format("SELECT * FROM %s",
                                AppDatabaseContract.ContactListTable.TABLE_NAME);
                    }
                    subscriber.onNext(query);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).map(new Func1<Cursor, Cursor>() {
            @Override
            public Cursor call(Cursor cursor) {
                cursor.moveToFirst();
                return cursor;
            }
        }).flatMap(new Func1<Cursor, Observable<ContactInfoWrapper>>() {
            @Override
            public Observable<ContactInfoWrapper> call(Cursor cursor) {
                return getContactsFromCursor(cursor);
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Update a certain field on a group of contacts.
     *
     * @param field    the field to update
     * @param value    the value to update to
     * @param toUpdate the contacts to update. If toUpdate is set to null, update all contacts.
     */
    public Observable<Void> updateContactField(final String field, final String value, @Nullable final ContactInfoWrapper[] toUpdate) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
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
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.computation()).publish().refCount();
    }

    /**
     * Update a certain field on a group of contacts by their IDs.
     *
     * @param field    the field to update
     * @param value    the value to update to
     * @param toUpdate an array containing the IDs of the contacts to update. If toUpdate is set to null, update all contacts.
     */
    public Observable<Void> updateContactFieldByIDs(final String field, final String value, @Nullable final int[] toUpdate) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                String query = "";

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
                    subscriber.onCompleted();
                }

                for (int contact : toUpdate) {
                    query += contact + ", ";
                }
                query = query.substring(0, query.length() - 2);
                query += ")";
                db.execSQL(query, new String[0]);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });

    }

    /**
     * Retrieves contacts from the database.
     *
     * @param whereclauses a string array containing raw SQL where clauses
     * @param orderBy      an SQL string dictating the order to return the contacts in
     * @return the retrieved contacts
     */
    public Observable<ContactInfoWrapper> getContacts(@Nullable final String[] whereclauses, @Nullable final String orderBy) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String query = String.format("SELECT * FROM %s ", AppDatabaseContract.ContactListTable.TABLE_NAME);
                    if (whereclauses != null && whereclauses.length > 0) {
                        query += "WHERE ";
                        for (String wc : whereclauses) query += " " + wc + " AND";
                        query = query.substring(0, query.length() - 3);
                    }
                    if (orderBy != null) query += "ORDER BY " + orderBy;
                    subscriber.onNext(query);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).map(new Func1<Cursor, Cursor>() {
            @Override
            public Cursor call(Cursor cursor) {
                cursor.moveToFirst();
                return cursor;
            }
        }).flatMap(new Func1<Cursor, Observable<ContactInfoWrapper>>() {
            @Override
            public Observable<ContactInfoWrapper> call(Cursor cursor) {
                return getContactsFromCursor(cursor);
            }
        }).publish().refCount();
    }
}
