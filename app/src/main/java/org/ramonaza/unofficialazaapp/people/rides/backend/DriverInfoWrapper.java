package org.ramonaza.unofficialazaapp.people.rides.backend;

import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;

import java.util.ArrayList;
import java.util.List;

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
    private List<ContactInfoWrapper> alephsInCar;

    public DriverInfoWrapper() {
        this.alephsInCar = new ArrayList<ContactInfoWrapper>(spots + 1);
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
        return spots - alephsInCar.size();
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

    public void addAlephToCar(ContactInfoWrapper aleph) {
        alephsInCar.add(aleph);
    }

    public void removeAlephFromCar(ContactInfoWrapper aleph) {
        alephsInCar.remove(aleph);
    }

    public List<ContactInfoWrapper> getAlephsInCar() {
        return alephsInCar;
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof DriverInfoWrapper && ((DriverInfoWrapper) o).getName().equals(getName()));
    }
}
