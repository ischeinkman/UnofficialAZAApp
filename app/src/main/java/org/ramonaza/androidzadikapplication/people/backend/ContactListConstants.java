package org.ramonaza.androidzadikapplication.people.backend;

import org.ramonaza.androidzadikapplication.database.AppDatabaseContract;
import org.ramonazaapi.contacts.ContactInfoWrapper;

/**
 * Created by ilan on 11/28/15.
 */
public final class ContactListConstants {

    public static final String[] ALEPHS_QUERY = {
            AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR + " > " + ContactInfoWrapper.getAlumnusGradYear()
    };

    public static final String[] ADVISORS_QUERY = {
            AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR + " = " + "\"Advisor\""
    };

    public static final String[] ALUMNI_QUERY = {
            AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR + " <= " + ContactInfoWrapper.getAlumnusGradYear()
    };

    public static final String NAME_SORT = AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC";
    public static final String YEAR_SORT = AppDatabaseContract.ContactListTable.COLUMN_GRADYEAR + " DESC" + ", " + AppDatabaseContract.ContactListTable.COLUMN_NAME + " ASC";

    private ContactListConstants() {
    }
}
