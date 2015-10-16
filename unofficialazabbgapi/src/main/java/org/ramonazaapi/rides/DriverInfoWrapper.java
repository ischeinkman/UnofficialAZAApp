package org.ramonazaapi.rides;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ilanscheinkman on 3/14/15.
 */
public class DriverInfoWrapper implements InfoWrapper {
    private int spots;
    private String name;
    private int area;
    private String address;
    private double latitude;
    private double longitude;
    private int id;
    private Set<ContactInfoWrapper> passengersInCar;

    public DriverInfoWrapper() {
        this.passengersInCar = new HashSet<ContactInfoWrapper>(spots + 1);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = Double.valueOf(latitude);
    }

    public double getLongitude() {
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

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return id * name.hashCode() + id;
    }
}
