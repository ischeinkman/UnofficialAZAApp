package org.ramonaza.unofficialazaapp.people.rides.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseContract;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHelper;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

/**
 * Created by ilanscheinkman on 7/16/15.
 */
public class RidesDatabaseHandler {

    private SQLiteDatabase db;

    public RidesDatabaseHandler(Context context) {
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public RidesDatabaseHandler(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Retrieve drivers from the database
     *
     * @param whereclauses filters for the query
     * @return the drivers from the database
     */
    public DriverInfoWrapper[] getDrivers(@Nullable String[] whereclauses, @Nullable String orderBy) {
        String query = String.format("SELECT * FROM %s ", ContactDatabaseContract.DriverListTable.TABLE_NAME);
        if (whereclauses != null && whereclauses.length > 0) {
            query += "WHERE ";
            for (String wc : whereclauses) query += " " + wc + " AND";
            query = query.substring(0, query.length() - 3);
        } //Cancel out the extra AND
        if (orderBy != null) query += " ORDER BY " + orderBy;
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();
        if (queryResults.getCount() == 0) {
            return new DriverInfoWrapper[0];
        }
        DriverInfoWrapper[] drivers = new DriverInfoWrapper[queryResults.getCount()];
        int i = 0;
        queryResults.moveToFirst();
        do {
            DriverInfoWrapper temp = new DriverInfoWrapper();
            temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable._ID)));
            temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_NAME)));
            temp.setSpots(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_SPACE)));
            temp.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
            temp.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
            temp.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
            temp.setArea(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_AREA)));
            drivers[i] = temp;
            i++;
        } while (queryResults.moveToNext());
        for (DriverInfoWrapper driver : drivers) {
            ContactInfoWrapper[] allInCar = getPassengersInCar(driver.getId());
            for (ContactInfoWrapper inCar : allInCar) driver.addPassengerToCar(inCar);
        }
        queryResults.close();
        return drivers;
    }

    /**
     * Gets the driver for the given ID.
     *
     * @param id the ID of the driver
     * @return the driver with this ID
     */
    public DriverInfoWrapper getDriver(int id) {
        String query = "SELECT * FROM " + ContactDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + ContactDatabaseContract.DriverListTable._ID + " = " + id;
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();
        DriverInfoWrapper driver = new DriverInfoWrapper();
        driver.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable._ID)));
        driver.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_NAME)));
        driver.setSpots(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_SPACE)));
        driver.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
        driver.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
        driver.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
        driver.setArea(queryResults.getInt(queryResults.getColumnIndexOrThrow(ContactDatabaseContract.DriverListTable.COLUMN_AREA)));
        ContactInfoWrapper[] carPassengers = getPassengersInCar(id);
        for (ContactInfoWrapper passengers : carPassengers) driver.addPassengerToCar(passengers);
        return driver;
    }

    /**
     * Adds a driver to the database.
     *
     * @param toAdd the driver to add
     * @throws DriverReadError if the driver could not be added
     */
    public void addDriver(DriverInfoWrapper toAdd) throws DriverReadError {
        ContentValues value = new ContentValues();
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_NAME, toAdd.getName());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_SPACE, toAdd.getSpots());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_ADDRESS, toAdd.getAddress());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_LATITUDE, toAdd.getLatitude());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toAdd.getLongitude());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_AREA, toAdd.getArea());
        long rowId = db.insert(ContactDatabaseContract.DriverListTable.TABLE_NAME, null, value);
        if (rowId == -1l) throw new DriverReadError("Null Driver Read", toAdd);
        else toAdd.setId((int) rowId);
    }

    /**
     * Deletes a driver from the database.
     *
     * @param toDelete the driver to delete
     */
    public void deleteDriver(int toDelete) {
        String query = "DELETE FROM " + ContactDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + ContactDatabaseContract.DriverListTable._ID + " = " + toDelete;
        db.execSQL(query);
        query = "DELETE FROM " + ContactDatabaseContract.RidesListTable.TABLE_NAME + " WHERE " +
                ContactDatabaseContract.RidesListTable.COLUMN_CAR + " = " + toDelete;
        db.execSQL(query);
    }

    /**
     * Updates a driver in the database.
     *
     * @param toUpdate the driver to update
     * @throws DriverReadError if the driver could not be updated
     */
    public void updateDriver(DriverInfoWrapper toUpdate) throws DriverReadError {
        ContentValues value = new ContentValues();
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_NAME, toUpdate.getName());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_SPACE, toUpdate.getSpots());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_ADDRESS, toUpdate.getAddress());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_LATITUDE, toUpdate.getLatitude());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toUpdate.getLongitude());
        value.put(ContactDatabaseContract.DriverListTable.COLUMN_AREA, toUpdate.getArea());
        long rowId = db.update(ContactDatabaseContract.DriverListTable.TABLE_NAME, value, "?=?", new String[]{
                ContactDatabaseContract.DriverListTable._ID,
                "" + toUpdate.getId()
        });
        if (rowId == -1l) throw new DriverReadError("Null Driver Read", toUpdate);
    }

    /**
     * Updates the rides table.
     *
     * @param drivers    the current cars
     * @param driverless the people not in cars
     */
    public void updateRides(DriverInfoWrapper[] drivers, ContactInfoWrapper[] driverless) {
        String driverlessIDs;
        if (driverless.length != 0) {
            driverlessIDs = "(";
            for (ContactInfoWrapper driverlessPassenger : driverless) {
                driverlessIDs += driverlessPassenger.getId() + ",";
            }
            driverlessIDs = driverlessIDs.substring(0, driverlessIDs.length() - 1);
            driverlessIDs += ")";
            db.execSQL("DELETE FROM " + ContactDatabaseContract.RidesListTable.TABLE_NAME +
                    " WHERE " + ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER +
                    " IN " + driverlessIDs);
        }
        for (DriverInfoWrapper driver : drivers) {
            addPassengersToCar(driver.getId(), driver.getPassengersInCar().toArray(new ContactInfoWrapper[driver.getPassengersInCar().size()
                    ]));
        }
    }

    /**
     * Adds passengers to a given car.
     *
     * @param driverid the ID of the car
     * @param inCar    the passengers to add to the car
     */
    public void addPassengersToCar(int driverid, ContactInfoWrapper[] inCar) {
        for (ContactInfoWrapper passenger : inCar) {
            Cursor checkPreexist = db.rawQuery("SELECT * FROM " + ContactDatabaseContract.RidesListTable.TABLE_NAME +
                    " WHERE " + ContactDatabaseContract.RidesListTable.COLUMN_CAR + " = " + driverid +
                    " AND " + ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER + " = " + passenger.getId(), null);
            if (checkPreexist.getCount() > 0) continue;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER, passenger.getId());
            contentValues.put(ContactDatabaseContract.RidesListTable.COLUMN_CAR, driverid);
            db.insert(ContactDatabaseContract.RidesListTable.TABLE_NAME, null, contentValues);
        }
    }

    /**
     * Sets an passengerID's attendance status to absent.
     *
     * @param passengerID the ID of the passengerID not currently here
     */
    public void setContactAbsent(int passengerID) {
        String updateQuery = String.format("UPDATE %s SET %s=%s WHERE %s=%d",
                ContactDatabaseContract.ContactListTable.TABLE_NAME,
                ContactDatabaseContract.ContactListTable.COLUMN_PRESENT,
                "0",
                ContactDatabaseContract.ContactListTable._ID,
                passengerID
        ); //Build query b/c android didn't like any other way
        db.execSQL(updateQuery);
        String deleteQuery = String.format("DELETE FROM %s WHERE %s=%s",
                ContactDatabaseContract.RidesListTable.TABLE_NAME,
                ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER,
                "" + passengerID);
        db.execSQL(deleteQuery);

    }

    /**
     * Gets the passengers in the car of a driver.
     *
     * @param driverId the ID of the driver
     * @return the passengers in the driver's car
     */
    public ContactInfoWrapper[] getPassengersInCar(int driverId) {
        String query = "SELECT * FROM " + ContactDatabaseContract.RidesListTable.TABLE_NAME + " JOIN " + ContactDatabaseContract.ContactListTable.TABLE_NAME +
                " ON " + ContactDatabaseContract.RidesListTable.TABLE_NAME + "." + ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER + "=" + ContactDatabaseContract.ContactListTable.TABLE_NAME + "." + ContactDatabaseContract.ContactListTable._ID +
                " WHERE " + ContactDatabaseContract.RidesListTable.TABLE_NAME + "." + ContactDatabaseContract.RidesListTable.COLUMN_CAR + "=" + driverId +
                " ORDER BY " + ContactDatabaseContract.ContactListTable.TABLE_NAME + "." + ContactDatabaseContract.ContactListTable.COLUMN_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        return ContactDatabaseHandler.getContactsFromCursor(cursor);
    }

    /**
     * Removes a passenger from all cars that it is currently in.
     *
     * @param passengerID the ID of the passenger to now render driverless
     */
    public void removePassengerFromCar(int passengerID) {
        String query = String.format("DELETE FROM %s WHERE %s=%d",
                ContactDatabaseContract.RidesListTable.TABLE_NAME,
                ContactDatabaseContract.RidesListTable.COLUMN_PASSENGER,
                passengerID);
        db.execSQL(query);
    }

    public class DriverReadError extends Exception {
        public DriverReadError(String errorMessage, DriverInfoWrapper erroredDriver) {
            super(String.format("%s ON %s", errorMessage, erroredDriver));

        }
    }
}
