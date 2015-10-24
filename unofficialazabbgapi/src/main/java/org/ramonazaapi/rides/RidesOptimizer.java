package org.ramonazaapi.rides;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.clusters.RidesCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class RidesOptimizer {

    private Set<ContactInfoWrapper> passengersToOptimize;
    private List<DriverInfoWrapper> driversToOptimize;
    private RidesAlgorithm[] algorithmChain;
    private Class<? extends RidesCluster> clusterType;
    private boolean retainPreexisting;

    public RidesOptimizer() {
        this.passengersToOptimize = new HashSet<>();
        this.driversToOptimize = new ArrayList<>();
    }

    public static double distBetweenHouses(PersonInfoWrapper person1, PersonInfoWrapper person2) {
        return Math.sqrt((
                person2.getLatitude() - person1.getLatitude())
                * (person2.getLatitude() - person1.getLatitude())
                + (person2.getLongitude() - person1.getLongitude())
                * (person2.getLongitude() - person1.getLongitude()));
    }

    public static double distBetweenHouses(PersonInfoWrapper person, double[] coords) {
        return Math.sqrt((
                person.getLatitude() - coords[0])
                * (person.getLatitude() - coords[0])
                + (person.getLongitude() - coords[1])
                * (person.getLongitude() - coords[1]));
    }

    /**
     * Get the loaded contacts not currently in a car.
     *
     * @return the driverless contacts
     */
    public ContactInfoWrapper[] getDriverless() {
        Set<ContactInfoWrapper> driverless = new HashSet<>(passengersToOptimize);
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (ContactInfoWrapper passenger : driver.getPassengersInCar()) {
                driverless.remove(passenger);
            }
        }
        return driverless.toArray(new ContactInfoWrapper[driverless.size()]);
    }

    /**
     * Get the currently loaded drivers (including all of the passengers in their cars).
     *
     * @return the loaded drivers
     */
    public DriverInfoWrapper[] getDrivers() {
        return driversToOptimize.toArray(new DriverInfoWrapper[driversToOptimize.size()]);
    }

    /**
     * Load driverless passengers into the optimizer.
     *
     * @param passengersToLoad the passengers to load
     * @return this
     */
    public RidesOptimizer loadPassengers(ContactInfoWrapper... passengersToLoad) {
        for (ContactInfoWrapper a : passengersToLoad) passengersToOptimize.add(a);
        return this;
    }

    /**
     * Load drivers into the optimizer.
     *
     * @param driversToLoad the drivers to load
     * @return this
     */
    public RidesOptimizer loadDriver(DriverInfoWrapper... driversToLoad) {
        for (DriverInfoWrapper d : driversToLoad) driversToOptimize.add(d);
        return this;
    }

    /**
     * Set the algorithm and strength of the optimization.
     *
     * @param algorithmChain    the algorithms to use, in the order to use them in.
     * @param retainPreexisting whether or not the optimizer should keep preexisting rides settings.
     *                          If set to false, current rides are clears and all preconfigured passengers
     *                          are loaded as driverless passengers.
     * @param clusterType       the type of cluster to use. Set to null if not clustering.
     * @return this
     */
    public RidesOptimizer setUpAlgorithms(RidesAlgorithm[] algorithmChain, boolean retainPreexisting, Class<? extends RidesCluster> clusterType) {
        this.retainPreexisting = retainPreexisting;
        this.clusterType = clusterType;
        this.algorithmChain = algorithmChain;
        return this;
    }

    /**
     * Optimize the rides. This app results in either all loaded passengers being in a car or all
     * loaded cars being full.
     */
    public void optimize() {

        //Clear rides if we are not retaining
        if (!retainPreexisting) {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper passenger : new ArrayList<ContactInfoWrapper>(driver.getPassengersInCar())) {
                    if (distBetweenHouses(driver, passenger) != 0) {
                        driver.removePassengerFromCar(passenger);
                        passengersToOptimize.add(passenger);
                    }
                }
            }
        }


        if (algorithmChain == null
                || algorithmChain.length == 0
                || driversToOptimize.isEmpty()
                || passengersToOptimize.isEmpty()) return;

        for (RidesAlgorithm algorithm : algorithmChain) {
            if (algorithm == null) continue;
            cleanDriverless();
            if (clusterType == null) algorithm.optimize(passengersToOptimize, driversToOptimize);
            else algorithm.optimize(passengersToOptimize, driversToOptimize, clusterType);
        }
    }

    private void cleanDriverless() {
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (ContactInfoWrapper contact : driver.getPassengersInCar()) {
                passengersToOptimize.remove(contact);
            }
        }
    }

    public interface RidesAlgorithm {
        void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers);

        void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers, Class<? extends RidesCluster> clusterType);
    }
}


