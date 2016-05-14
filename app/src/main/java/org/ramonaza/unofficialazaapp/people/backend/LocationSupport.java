package org.ramonaza.unofficialazaapp.people.backend;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

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
    public static Observable<ContactInfoWrapper> recalculateLatLong(final Context context) {
        final ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(context);

        ConnectableObservable<ContactInfoWrapper> rval = handler.getContacts(null, null).map(new Func1<ContactInfoWrapper, ContactInfoWrapper>() {
            @Override
            public ContactInfoWrapper call(ContactInfoWrapper contact) {
                double[] coords = getCoordsFromAddress(contact.getAddress(), context);
                if (coords != null) {
                    contact.setLatitude(coords[0]);
                    contact.setLongitude(coords[1]);
                }
                return contact;
            }
        }).flatMap(new Func1<ContactInfoWrapper, Observable<ContactInfoWrapper>>() {
            @Override
            public Observable<ContactInfoWrapper> call(ContactInfoWrapper contactInfoWrapper) {
                return handler.updateContacts(contactInfoWrapper);
            }
        }).subscribeOn(Schedulers.computation()).publish();
        rval.connect();
        return rval;
    }
}
