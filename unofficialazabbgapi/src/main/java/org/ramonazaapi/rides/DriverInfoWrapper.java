package org.ramonazaapi.rides;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;
import org.ramonazaapi.interfaces.LocationPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ilanscheinkman on 3/14/15.
 */
public class DriverInfoWrapper implements LocationPoint, InfoWrapper {
    private int spots;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int id;
    private ContactInfoWrapper contactInfo;


    private Set<ContactInfoWrapper> passengersInCar;

    public DriverInfoWrapper() {
        this.passengersInCar = new HashSet<ContactInfoWrapper>(spots + 1);
        this.contactInfo = null;
    }


    public ContactInfoWrapper getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfoWrapper contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isContactable() {
        return contactInfo != null;
    }

    public double getX() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = Double.valueOf(latitude);
    }

    public double getY() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = Double.valueOf(longitude);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFreeSpots() {
        return spots - passengersInCar.size();
    }

    public int getSpots() {
        return spots;
    }

    public void setSpots(int spots) {
        this.spots = spots;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void addPassengerToCar(ContactInfoWrapper passenger) {
        passengersInCar.add(passenger);
    }

    public void removePassengerFromCar(ContactInfoWrapper passenger) {
        passengersInCar.remove(passenger);
    }

    public List<ContactInfoWrapper> getPassengersInCar() {
        return new ArrayList<ContactInfoWrapper>(passengersInCar);
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof DriverInfoWrapper && ((DriverInfoWrapper) o).getName().equals(getName()));
    }


    @Override
    public int hashCode() {
        return (id + 1) * name.hashCode() + (id + 1) * spots;
    }
}
