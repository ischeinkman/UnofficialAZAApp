package org.ramonaza.unofficialazaapp.people.rides.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.BaseDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by ilanscheinkman on 7/16/15.
 */
public class RidesDatabaseHandler extends BaseDatabaseHandler<DriverInfoWrapper> {

    private ContactDatabaseHandler passangerHandler;

    public RidesDatabaseHandler(Context context) {
        super(context);
        passangerHandler = new ContactDatabaseHandler(this);
    }

    public RidesDatabaseHandler(SQLiteDatabase db) {
        super(db);
        passangerHandler = new ContactDatabaseHandler(this);
    }

    public RidesDatabaseHandler(BaseDatabaseHandler other) {
        super(other);
        passangerHandler = new ContactDatabaseHandler(this);
    }

    private Observable<DriverInfoWrapper> getDriversFromCursor(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<DriverInfoWrapper>() {
            @Override
            public void call(Subscriber<? super DriverInfoWrapper> subscriber) {
                cursor.moveToFirst();
                do {
                    DriverInfoWrapper temp = new DriverInfoWrapper();
                    temp.setId(cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable._ID)));
                    temp.setName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_NAME)));
                    temp.setSpots(cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_SPACE)));
                    temp.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
                    temp.setLatitude(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
                    temp.setLongitude(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
                    temp.setContactInfoId(cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO)));
                    subscriber.onNext(temp);
                } while (cursor.moveToNext());
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

    private Observable<DriverInfoWrapper> getDriverContactInfo(final DriverInfoWrapper driver) {
        return passangerHandler.getContacts(driver.getContactInfoId()).map(new Func1<ContactInfoWrapper, DriverInfoWrapper>() {
            @Override
            public DriverInfoWrapper call(ContactInfoWrapper contactInfoWrapper) {
                driver.setContactInfo(contactInfoWrapper);
                return driver;
            }
        });
    }

    /**
     * Retrieve drivers from the database
     *
     * @param whereclauses filters for the query
     * @return the drivers from the database
     */
    public Observable<DriverInfoWrapper> getDrivers(@Nullable final String[] whereclauses, @Nullable final String orderBy) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                //Contstruct the query
                String query = String.format("SELECT * FROM %s ", AppDatabaseContract.DriverListTable.TABLE_NAME);
                if (whereclauses != null && whereclauses.length > 0) {
                    query += "WHERE ";
                    for (String wc : whereclauses) query += " " + wc + " AND";
                    query = query.substring(0, query.length() - 3);
                } //Cancel out the extra AND
                if (orderBy != null) query += " ORDER BY " + orderBy;
                subscriber.onNext(query);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).flatMap(new Func1<Cursor, Observable<DriverInfoWrapper>>() {
            @Override
            public Observable<DriverInfoWrapper> call(Cursor cursor) {
                return getDriversFromCursor(cursor);
            }
        }).flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
            @Override
            public Observable<DriverInfoWrapper> call(DriverInfoWrapper driverInfoWrapper) {
                return getDriverContactInfo(driverInfoWrapper);
            }
        }).flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
            @Override
            public Observable<DriverInfoWrapper> call(final DriverInfoWrapper driverInfoWrapper) {
                return getPassengersInCar(driverInfoWrapper.getId())
                        .toList()
                        .map(new Func1<List<ContactInfoWrapper>, DriverInfoWrapper>() {
                            @Override
                            public DriverInfoWrapper call(List<ContactInfoWrapper> contactInfoWrappers) {
                                driverInfoWrapper.addPassengersToCar(contactInfoWrappers);
                                return driverInfoWrapper;
                            }
                        });
            }
        });
    }

    /**
     * Gets the driver for the given ID.
     *
     * @param id the ID of the driver
     * @return the driver with this ID
     */
    public Observable<DriverInfoWrapper> getDrivers(int... id) {
        return Observable.just("SELECT * FROM " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + AppDatabaseContract.DriverListTable._ID + " = " + id)
                .flatMap(new Func1<String, Observable<Cursor>>() {
                    @Override
                    public Observable<Cursor> call(String s) {
                        return query(s);
                    }
                })
                .map(new Func1<Cursor, DriverInfoWrapper>() {
                    @Override
                    public DriverInfoWrapper call(Cursor queryResults) {
                        queryResults.moveToFirst();

                        DriverInfoWrapper driver = new DriverInfoWrapper();
                        driver.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable._ID)));
                        driver.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_NAME)));
                        driver.setSpots(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_SPACE)));
                        driver.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
                        driver.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
                        driver.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
                        driver.setContactInfoId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO)));

                        return driver;
                    }
                })
                .flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
                    @Override
                    public Observable<DriverInfoWrapper> call(final DriverInfoWrapper driverInfoWrapper) {
                        return passangerHandler.getContacts(driverInfoWrapper.getContactInfoId())
                                .map(new Func1<ContactInfoWrapper, DriverInfoWrapper>() {
                                    @Override
                                    public DriverInfoWrapper call(ContactInfoWrapper contactInfoWrapper) {
                                        driverInfoWrapper.setContactInfo(contactInfoWrapper);
                                        return driverInfoWrapper;
                                    }
                                });
                    }
                })
                .flatMap(new Func1<DriverInfoWrapper, Observable<DriverInfoWrapper>>() {
                    @Override
                    public Observable<DriverInfoWrapper> call(final DriverInfoWrapper driverInfoWrapper) {
                        return getPassengersInCar(driverInfoWrapper.getId()).toList()
                                .map(new Func1<List<ContactInfoWrapper>, DriverInfoWrapper>() {
                                    @Override
                                    public DriverInfoWrapper call(List<ContactInfoWrapper> contactInfoWrappers) {
                                        driverInfoWrapper.addPassengersToCar(contactInfoWrappers);
                                        return driverInfoWrapper;
                                    }
                                });
                    }
                });
    }

    /**
     * Adds a driver to the database.
     *
     * @param toAdd the driver to add
     */
    public Observable<Integer> addDriver(final DriverInfoWrapper toAdd) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                ContentValues value = new ContentValues();
                value.put(AppDatabaseContract.DriverListTable.COLUMN_NAME, toAdd.getName());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_SPACE, toAdd.getSpots());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS, toAdd.getAddress());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE, toAdd.getX());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toAdd.getY());
                if (toAdd.getContactInfoId() > -1) {
                    value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, toAdd.getContactInfoId());
                } else {
                    value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, -1);
                }
                long rowId = db.insert(AppDatabaseContract.DriverListTable.TABLE_NAME, null, value);
                if (rowId == -1l) subscriber.onError(new IOException("Null Driver Read"));
                else {
                    toAdd.setId((int) rowId);
                    subscriber.onNext((int) rowId);
                }
                subscriber.onCompleted();
            }
        });
    }

    /**
     * Deletes a driver from the database.
     *
     * @param toDelete the driver to delete
     */
    public Observable<Integer> deleteDrivers(final int... toDelete) {
        return Observable.just("DELETE FROM " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + AppDatabaseContract.DriverListTable._ID + " = " + toDelete)
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        db.execSQL(s);
                        return 1;
                    }
                })
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return "DELETE FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME + " WHERE " +
                                AppDatabaseContract.RidesListTable.COLUMN_CAR + " = " + toDelete;
                    }
                })
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        db.execSQL(s);
                        return 1;
                    }
                });
    }

    /**
     * Updates a driver in the database.
     *
     * @param toUpdate the driver to update
     */
    public Observable<Integer> updateDriver(final DriverInfoWrapper toUpdate) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                ContentValues value = new ContentValues();
                value.put(AppDatabaseContract.DriverListTable.COLUMN_NAME, toUpdate.getName());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_SPACE, toUpdate.getSpots());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS, toUpdate.getAddress());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE, toUpdate.getX());
                value.put(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toUpdate.getY());
                if (toUpdate.getContactInfo() != null) {
                    value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, toUpdate.getContactInfo().getId());
                } else {
                    value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, -1);
                }
                long rowId = db.update(AppDatabaseContract.DriverListTable.TABLE_NAME, value, "?=?", new String[]{
                        AppDatabaseContract.DriverListTable._ID,
                        "" + toUpdate.getId()
                });
                if (rowId < 0) subscriber.onError(new IOException("Null Driver Read"));
                else {
                    subscriber.onNext((int) rowId);
                    subscriber.onCompleted();
                }

            }
        });
    }

    /**
     * Updates the rides table.
     *
     * @param drivers    the current cars
     * @param driverless the people not in cars
     */
    public void updateRides(DriverInfoWrapper[] drivers, ContactInfoWrapper[] driverless) {
        db.execSQL("DELETE FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME);
        passangerHandler.updateContactField(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, "0", null);
        for (DriverInfoWrapper driver : drivers) {
            ContactInfoWrapper[] inCar = driver.getPassengersInCar().toArray(new ContactInfoWrapper[driver.getPassengersInCar().size()]);
            passangerHandler.updateContactField(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, "1", inCar);
            addPassengersToCar(driver.getId(), inCar);
        }
        passangerHandler.updateContactField(AppDatabaseContract.ContactListTable.COLUMN_PRESENT, "1", driverless);
    }

    /**
     * Adds passengers to a given car.
     *
     * @param driverid the ID of the car
     * @param inCar    the passengers to add to the car
     */
    public Observable<ContactInfoWrapper> addPassengersToCar(final int driverid, ContactInfoWrapper[] inCar) {
        return Observable.from(inCar).map(new Func1<ContactInfoWrapper, ContactInfoWrapper>() {
            @Override
            public ContactInfoWrapper call(ContactInfoWrapper passenger) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, passenger.getId());
                contentValues.put(AppDatabaseContract.RidesListTable.COLUMN_CAR, driverid);
                db.insert(AppDatabaseContract.RidesListTable.TABLE_NAME, null, contentValues);
                return passenger;
            }
        });
    }

    /**
     * Sets an passengerID's attendance status to absent.
     *
     * @param passengerID the ID of the passengerID not currently here
     */
    public void setContactAbsent(int passengerID) {
        String updateQuery = String.format("UPDATE %s SET %s=%s WHERE %s=%d",
                AppDatabaseContract.ContactListTable.TABLE_NAME,
                AppDatabaseContract.ContactListTable.COLUMN_PRESENT,
                "0",
                AppDatabaseContract.ContactListTable._ID,
                passengerID
        ); //Build query b/c android didn't like any other way
        db.execSQL(updateQuery);
        String deleteQuery = String.format("DELETE FROM %s WHERE %s=%s",
                AppDatabaseContract.RidesListTable.TABLE_NAME,
                AppDatabaseContract.RidesListTable.COLUMN_PASSENGER,
                "" + passengerID);
        db.execSQL(deleteQuery);

    }

    /**
     * Gets the passengers in the car of a driver.
     *
     * @param driverId the ID of the driver
     * @return the passengers in the driver's car
     */
    public Observable<ContactInfoWrapper> getPassengersInCar(final int driverId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("SELECT * FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME + " JOIN " + AppDatabaseContract.ContactListTable.TABLE_NAME +
                        " ON " + AppDatabaseContract.RidesListTable.TABLE_NAME + "." + AppDatabaseContract.RidesListTable.COLUMN_PASSENGER + "=" + AppDatabaseContract.ContactListTable.TABLE_NAME + "." + AppDatabaseContract.ContactListTable._ID +
                        " WHERE " + AppDatabaseContract.RidesListTable.TABLE_NAME + "." + AppDatabaseContract.RidesListTable.COLUMN_CAR + "=" + driverId +
                        " ORDER BY " + AppDatabaseContract.ContactListTable.TABLE_NAME + "." + AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC"
                );
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).flatMap(new Func1<Cursor, Observable<ContactInfoWrapper>>() {
            @Override
            public Observable<ContactInfoWrapper> call(Cursor cursor) {
                return ContactDatabaseHandler.getContactsFromCursor(cursor);
            }
        });
    }

    /**
     * Removes a passenger from all cars that it is currently in.
     *
     * @param passengerID the ID of the passenger to now render driverless
     */
    public Observable<Integer> removePassengersFromCar(int... passengerID) {
        return Observable.just(String.format("DELETE FROM %s WHERE %s=%d",
                AppDatabaseContract.RidesListTable.TABLE_NAME,
                AppDatabaseContract.RidesListTable.COLUMN_PASSENGER,
                passengerID))
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        try {
                            db.execSQL(s);
                            return 1;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
}
