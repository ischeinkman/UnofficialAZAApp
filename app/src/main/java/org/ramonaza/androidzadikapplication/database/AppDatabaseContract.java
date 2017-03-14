package org.ramonaza.androidzadikapplication.database;

import android.provider.BaseColumns;

/**
 * Created by ilanscheinkman on 3/12/15.
 */
public final class AppDatabaseContract {

    public static final String VTYPE_TEXT = "TEXT";
    public static final String VTYPE_INT = "INTEGER";
    public static final String CREATE_TABLES = ContactListTable.CREATE_TABLE + "; " + DriverListTable.CREATE_TABLE + "; " + RidesListTable.CREATE_TABLE + "; " + EventListTable.CREATE_TABLE;
    public static final String DELETE_TABLES = ContactListTable.DELETE_TABLE + "; " + DriverListTable.DELETE_TABLE + "; " + RidesListTable.DELETE_TABLE + "; " + EventListTable.DELETE_TABLE;

    public static abstract class ContactListTable implements BaseColumns {
        public static final String TABLE_NAME = "alephs";
        public static final String COLUMN_NAME = "alephname";
        public static final String COLUMN_EMAIL = "alephemail";
        public static final String COLUMN_PHONE = "alephphonenumber";
        public static final String COLUMN_ADDRESS = "alephaddress";
        public static final String COLUMN_SCHOOL = "alephschool";
        public static final String COLUMN_GRADYEAR = "alephgraduationyear";
        public static final String COLUMN_AREA = "alephareanumber";
        public static final String COLUMN_PRESENT = "alephatevent";
        public static final String COLUMN_LATITUDE = "alephaddresslat";
        public static final String COLUMN_LONGITUDE = "alephaddresslong";
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                ContactListTable._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " " + VTYPE_TEXT + "," +
                COLUMN_PHONE + " " + VTYPE_TEXT + "," +
                COLUMN_EMAIL + " " + VTYPE_TEXT + "," +
                COLUMN_SCHOOL + " " + VTYPE_TEXT + "," +
                COLUMN_GRADYEAR + " " + VTYPE_INT + "," +
                COLUMN_ADDRESS + " " + VTYPE_TEXT + "," +
                COLUMN_LATITUDE + " " + VTYPE_TEXT + "," +
                COLUMN_LONGITUDE + " " + VTYPE_TEXT + "," +
                COLUMN_AREA + " " + VTYPE_INT + "," +
                COLUMN_PRESENT + " " + VTYPE_INT + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


    public static abstract class EventListTable implements BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final String COLUMN_NAME = "eventname";
        public static final String COLUMN_DESC = "eventdesc";
        public static final String COLUMN_PLANNER = "eventplanner";
        public static final String COLUMN_MEET = "eventmeet";
        public static final String COLUMN_LOCATION = "eventlocation";
        public static final String COLUMN_BRING = "eventbring";
        public static final String COLUMN_DATE = "eventdate";
        public static final String COLUMN_FOOD = "eventfood";
        public static final String COLUMN_HOUSE = "eventhouse";
        public static final String COLUMN_STAFF = "eventstaff";
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                EventListTable._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " " + VTYPE_TEXT + "," +
                COLUMN_DESC + " " + VTYPE_TEXT + "," +
                COLUMN_PLANNER + " " + VTYPE_TEXT + "," +
                COLUMN_MEET + " " + VTYPE_TEXT + "," +
                COLUMN_LOCATION + " " + VTYPE_TEXT + "," +
                COLUMN_BRING + " " + VTYPE_TEXT + "," +
                COLUMN_DATE + " " + VTYPE_TEXT + ", " +
                COLUMN_FOOD + " " + VTYPE_TEXT + ", " +
                COLUMN_HOUSE + " " + VTYPE_TEXT + ", " +
                COLUMN_STAFF + " " + VTYPE_TEXT +
                ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class DriverListTable implements BaseColumns {
        public static final String TABLE_NAME = "drivers";
        public static final String COLUMN_NAME = "drivername";
        public static final String COLUMN_SPACE = "driverspots";
        public static final String COLUMN_AREA = "driverareanumber";
        public static final String COLUMN_ADDRESS = "driveraddress";
        public static final String COLUMN_LATITUDE = "driveraddresslat";
        public static final String COLUMN_LONGITUDE = "driveraddresslong";
        public static final String COLUMN_CONTACT_INFO = "drivercontactinfo";
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                DriverListTable._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " " + VTYPE_TEXT + ", " +
                COLUMN_ADDRESS + " " + VTYPE_TEXT + ", " +
                COLUMN_LATITUDE + " " + VTYPE_TEXT + ", " +
                COLUMN_LONGITUDE + " " + VTYPE_TEXT + ", " +
                COLUMN_AREA + " " + VTYPE_INT + ", " +
                COLUMN_SPACE + " " + VTYPE_INT + ", " +
                COLUMN_CONTACT_INFO + " " + VTYPE_INT +
                ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class RidesListTable implements BaseColumns {
        public static final String TABLE_NAME = "rides";
        public static final String COLUMN_PASSENGER = "aleph_id";
        public static final String COLUMN_CAR = "driver_id";
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                RidesListTable._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_PASSENGER + " " + VTYPE_INT + "," +
                COLUMN_CAR + " " + VTYPE_TEXT + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


}
