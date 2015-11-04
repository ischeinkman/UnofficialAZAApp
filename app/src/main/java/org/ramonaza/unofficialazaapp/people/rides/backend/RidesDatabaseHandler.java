package org.ramonaza.unofficialazaapp.people.rides.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;

/**
 * Created by ilanscheinkman on 7/16/15.
 */
public class RidesDatabaseHandler {

    private SQLiteDatabase db;

    public RidesDatabaseHandler(Context context) {
        AppDatabaseHelper dbHelper = new AppDatabaseHelper(context);
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

        //Contstruct the query
        String query = String.format("SELECT * FROM %s ", AppDatabaseContract.DriverListTable.TABLE_NAME);
        if (whereclauses != null && whereclauses.length > 0) {
            query += "WHERE ";
            for (String wc : whereclauses) query += " " + wc + " AND";
            query = query.substring(0, query.length() - 3);
        } //Cancel out the extra AND
        if (orderBy != null) query += " ORDER BY " + orderBy;

        //Get the cursor
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();
        if (queryResults.getCount() == 0) {
            return new DriverInfoWrapper[0];
        }

        ContactDatabaseHandler contactDatabaseHandler = null; //For contact info retrieval

        DriverInfoWrapper[] drivers = new DriverInfoWrapper[queryResults.getCount()];

        int i = 0;
        queryResults.moveToFirst();
        do {
            DriverInfoWrapper temp = new DriverInfoWrapper();
            temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable._ID)));
            temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_NAME)));
            temp.setSpots(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_SPACE)));
            temp.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
            temp.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
            temp.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
            int contactInfoId = queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO));
            if (contactInfoId >= 0) {
                if (contactDatabaseHandler == null)
                    contactDatabaseHandler = new ContactDatabaseHandler(db);
                temp.setContactInfo(contactDatabaseHandler.getContact(contactInfoId));
            }
            drivers[i] = temp;
            i++;
        } while (queryResults.moveToNext());

        //Set up car
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
        ContactDatabaseHandler contactDatabaseHandler = null;

        String query = "SELECT * FROM " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + AppDatabaseContract.DriverListTable._ID + " = " + id;
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();

        DriverInfoWrapper driver = new DriverInfoWrapper();
        driver.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable._ID)));
        driver.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_NAME)));
        driver.setSpots(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_SPACE)));
        driver.setAddress(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS)));
        driver.setLatitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE)));
        driver.setLongitude(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE)));
        int contactInfo = queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO));
        if (contactInfo >= 0) {
            if (contactDatabaseHandler == null)
                contactDatabaseHandler = new ContactDatabaseHandler(db);
            driver.setContactInfo(contactDatabaseHandler.getContact(contactInfo));
        }

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
        value.put(AppDatabaseContract.DriverListTable.COLUMN_NAME, toAdd.getName());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_SPACE, toAdd.getSpots());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS, toAdd.getAddress());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE, toAdd.getLatitude());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toAdd.getLongitude());
        if (toAdd.getContactInfo() != null) {
            value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, toAdd.getContactInfo().getId());
        } else {
            value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, -1);
        }
        long rowId = db.insert(AppDatabaseContract.DriverListTable.TABLE_NAME, null, value);
        if (rowId == -1l) throw new DriverReadError("Null Driver Read", toAdd);
        else toAdd.setId((int) rowId);
    }

    /**
     * Deletes a driver from the database.
     *
     * @param toDelete the driver to delete
     */
    public void deleteDriver(int toDelete) {
        String query = "DELETE FROM " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                " WHERE " + AppDatabaseContract.DriverListTable._ID + " = " + toDelete;
        db.execSQL(query);
        query = "DELETE FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME + " WHERE " +
                AppDatabaseContract.RidesListTable.COLUMN_CAR + " = " + toDelete;
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
        value.put(AppDatabaseContract.DriverListTable.COLUMN_NAME, toUpdate.getName());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_SPACE, toUpdate.getSpots());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_ADDRESS, toUpdate.getAddress());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_LATITUDE, toUpdate.getLatitude());
        value.put(AppDatabaseContract.DriverListTable.COLUMN_LONGITUDE, toUpdate.getLongitude());
        if (toUpdate.getContactInfo() != null) {
            value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, toUpdate.getContactInfo().getId());
        } else {
            value.put(AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO, -1);
        }
        long rowId = db.update(AppDatabaseContract.DriverListTable.TABLE_NAME, value, "?=?", new String[]{
                AppDatabaseContract.DriverListTable._ID,
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
            db.execSQL("DELETE FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME +
                    " WHERE " + AppDatabaseContract.RidesListTable.COLUMN_PASSENGER +
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
            Cursor checkPreexist = db.rawQuery("SELECT * FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME +
                    " WHERE " + AppDatabaseContract.RidesListTable.COLUMN_CAR + " = " + driverid +
                    " AND " + AppDatabaseContract.RidesListTable.COLUMN_PASSENGER + " = " + passenger.getId(), null);
            if (checkPreexist.getCount() > 0) continue;
            ContentValues contentValues = new ContentValues();
            contentValues.put(AppDatabaseContract.RidesListTable.COLUMN_PASSENGER, passenger.getId());
            contentValues.put(AppDatabaseContract.RidesListTable.COLUMN_CAR, driverid);
            db.insert(AppDatabaseContract.RidesListTable.TABLE_NAME, null, contentValues);
        }
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
    public ContactInfoWrapper[] getPassengersInCar(int driverId) {
        String query = "SELECT * FROM " + AppDatabaseContract.RidesListTable.TABLE_NAME + " JOIN " + AppDatabaseContract.ContactListTable.TABLE_NAME +
                " ON " + AppDatabaseContract.RidesListTable.TABLE_NAME + "." + AppDatabaseContract.RidesListTable.COLUMN_PASSENGER + "=" + AppDatabaseContract.ContactListTable.TABLE_NAME + "." + AppDatabaseContract.ContactListTable._ID +
                " WHERE " + AppDatabaseContract.RidesListTable.TABLE_NAME + "." + AppDatabaseContract.RidesListTable.COLUMN_CAR + "=" + driverId +
                " ORDER BY " + AppDatabaseContract.ContactListTable.TABLE_NAME + "." + AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC";
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
                AppDatabaseContract.RidesListTable.TABLE_NAME,
                AppDatabaseContract.RidesListTable.COLUMN_PASSENGER,
                passengerID);
        db.execSQL(query);
    }

    public class DriverReadError extends Exception {
        public DriverReadError(String errorMessage, DriverInfoWrapper erroredDriver) {
            super(String.format("%s ON %s", errorMessage, erroredDriver));

        }
    }
}
