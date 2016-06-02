package org.ramonaza.unofficialazaapp.people.backend;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.ramonaza.unofficialazaapp.helpers.backend.DatabaseHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class LocationSupport {

    /**
     * Gets latitude and longitude of an address.
     *
     * @param address the address to get the coordinates from
     * @param context the context to use to retrieve the coordinates
     * @return the coordinates for this address
     */
    public static double[] getCoordsFromAddress(String address, Context context) {
        if (!Geocoder.isPresent()) return null;
        Geocoder geocoder = new Geocoder(context);
        Address location = null;
        try {
            location = geocoder.getFromLocationName(address, 1).get(0);
        } catch (Exception e) {
            return new double[]{0, 0};
        }
        return new double[]{
                location.getLatitude(),
                location.getLongitude()
        };
    }

    /**
     * Debug method to recalculate latitudes and longitudes for all contacts.
     *
     * @param context the context to retrieve contacts from and get coordinates
     */
    public static void recalculateLatLong(Context context) {
        ContactDatabaseHandler handler = (ContactDatabaseHandler) DatabaseHandler.getHandler(ContactDatabaseHandler.class);
        ContactInfoWrapper[] toCalc = handler.getContacts(null, null);
        for (ContactInfoWrapper contact : toCalc) {
            double[] coords = getCoordsFromAddress(contact.getAddress(), context);
            if (coords == null) continue;
            contact.setLatitude(coords[0]);
            contact.setLongitude(coords[1]);
            try {
                handler.updateContact(contact);
            } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                contactCSVReadError.printStackTrace();
            }
        }
    }
}
