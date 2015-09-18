package org.ramonaza.unofficialazaapp.people.backend;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class LocationSupport {

    public static double[] getCoordsFromAddress(String address, Context context){
        if(!Geocoder.isPresent()) return null;
        Geocoder geocoder=new Geocoder(context);
        Address location= null;
        try {
            location = geocoder.getFromLocationName(address, 1).get(0);
        } catch (Exception e) {
            return null;
        }
        return new double[]{
          location.getLatitude(),
          location.getLongitude()
        };
    }

    public static void recalculateLatLong(Context context){
        ContactDatabaseHandler handler=new ContactDatabaseHandler(context);
        ContactInfoWrapper[] toCalc = handler.getContacts(null, null);
        for(ContactInfoWrapper aleph:toCalc){
            double[] coords=getCoordsFromAddress(aleph.getAddress(), context);
            if(coords == null) continue;
            aleph.setLatitude(coords[0]);
            aleph.setLongitude(coords[1]);
            try {
                handler.updateContact(aleph);
            } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                contactCSVReadError.printStackTrace();
            }
        }
    }
}
