package org.ramonazaapi.contacts;

import org.ramonazaapi.interfaces.InfoWrapperJsonHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilan on 12/19/15.
 */
public class ContactJsonHandler extends InfoWrapperJsonHandler<ContactInfoWrapper> {
    @Override
    protected Map<String, String> getAttributes(ContactInfoWrapper toGet) {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("id", "" + toGet.getId());
        attrs.put("name", toGet.getName());
        attrs.put("email", toGet.getEmail());
        attrs.put("phonenumber", toGet.getPhoneNumber());
        attrs.put("school", toGet.getSchool());
        attrs.put("graduationyear", toGet.getGradYear());
        attrs.put("address", toGet.getAddress());
        attrs.put("addresslat", "" + toGet.getX());
        attrs.put("addresslong", "" + toGet.getY());
        attrs.put("atevent", (toGet.isPresent()) ? "1" : "0");
        attrs.put("areanumber", "" + toGet.getArea());
        return attrs;
    }

    @Override
    protected ContactInfoWrapper createFromAttributes(Map<String, String> attributes) {
        ContactInfoWrapper toReturn = new ContactInfoWrapper();
        toReturn.setId(Integer.parseInt(attributes.get("id")));
        toReturn.setName(attributes.get("name"));
        toReturn.setEmail(attributes.get("email"));
        toReturn.setPhoneNumber(attributes.get("phonenumber"));
        toReturn.setSchool(attributes.get("school"));
        toReturn.setGradYear(attributes.get("graduationyear"));
        toReturn.setAddress(attributes.get("address"));
        toReturn.setLatitude(attributes.get("addresslat"));
        toReturn.setLongitude(attributes.get("addresslong"));
        toReturn.setPresent((attributes.get("atevent").equals("1") ? true : false));
        return toReturn;
    }
}
