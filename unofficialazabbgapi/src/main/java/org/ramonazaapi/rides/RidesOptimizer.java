package org.ramonazaapi.rides;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.optimizationsupport.HungarianAlgorithm;
import org.ramonazaapi.rides.optimizationsupport.clusters.RidesCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class RidesOptimizer {

    /**
     * Calculate rides based on latitude and longitude, iterating over the passengers
     * and assigning them a driver.
     */
    public static final int ALGORITHM_LATLONG_PASSENGERS_FIRST = 0;

    /**
     * Calculate rides based on latitude and longitude, iterating over the drivers
     * and assigning them passengers.
     */
    public static final int ALGORITHM_LATLONG_DRIVERS_FIRST = 1;

    /**
     * Calculate rides based on latitude and longitude, finding the optimal assignment of passengers
     * to drivers in order to minimize total distance traveled by all drivers. Makes the simplifying
     * assumption that all drivers return to their home in between each drop-off.
     */
    public static final int ALGORITHM_NAIVE_HUNGARIAN = 2;

    /**
     * Calculate rides by grouping all passengers into clusters, and then creating cars based on the clusters
     * of the passengers already in the car. We assume that all drivers at least have 1 passenger representing
     * themselves.
     * WARNING: setting no cluster type will perform no action.
     */
    public static final int ALGORITHM_CLUSTER_MATCHING = 3;

    private Set<ContactInfoWrapper> passengersToOptimize;
    private List<DriverInfoWrapper> driversToOptimize;
    private int algorithm;
    private Class<? extends RidesCluster> clusterType;
    private boolean retainPreexisting;

    public RidesOptimizer() {
        this.passengersToOptimize = new HashSet<ContactInfoWrapper>();
        this.driversToOptimize = new ArrayList<DriverInfoWrapper>();
    }

    public static double distBetweenHouses(DriverInfoWrapper driver, ContactInfoWrapper passenger) {
        return Math.sqrt((
                passenger.getLatitude() - driver.getLatitude())
                * (passenger.getLatitude() - driver.getLatitude())
                + (passenger.getLongitude() - driver.getLongitude())
                * (passenger.getLongitude() - driver.getLongitude()));
    }

    public static double distBetweenHouses(ContactInfoWrapper passenger1, ContactInfoWrapper passenger2) {
        return Math.sqrt((
                passenger1.getLatitude() - passenger2.getLatitude())
                * (passenger1.getLatitude() - passenger2.getLatitude())
                + (passenger1.getLongitude() - passenger2.getLongitude())
                * (passenger1.getLongitude() - passenger2.getLongitude()));
    }

    public static double distBetweenHouses(ContactInfoWrapper passenger, double[] coords) {
        return Math.sqrt((
                passenger.getLatitude() - coords[0])
                * (passenger.getLatitude() - coords[0])
                + (passenger.getLongitude() - coords[1])
                * (passenger.getLongitude() - coords[1]));
    }

    public static double distBetweenHouses(DriverInfoWrapper driver, double[] coords) {
        return Math.sqrt((
                driver.getLatitude() - coords[0])
                * (driver.getLatitude() - coords[0])
                + (driver.getLongitude() - coords[1])
                * (driver.getLongitude() - coords[1]));
    }

    /**
     * Get the loaded contacts not currently in a car.
     *
     * @return the driverless contacts
     */
    public ContactInfoWrapper[] getDriverless() {
        return passengersToOptimize.toArray(new ContactInfoWrapper[passengersToOptimize.size()]);
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
     * @param algorithm         the algorithm to use, based on public constants.
     * @param retainPreexisting whether or not the optimizer should keep preexisting rides settings.
     *                          If set to false, current rides are clears and all preconfigured passengers
     *                          are loaded as driverless passengers.
     * @param clusterType       the type of cluster to use. Set to null if not clustering.
     * @return this
     */
    public RidesOptimizer setAlgorithm(int algorithm, boolean retainPreexisting, Class<? extends RidesCluster> clusterType) {
        this.algorithm = algorithm;
        this.retainPreexisting = retainPreexisting;
        this.clusterType = clusterType;
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
        } else {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper contact : driver.getPassengersInCar()) {
                    if (passengersToOptimize.contains(contact))
                        passengersToOptimize.remove(contact);
                }
            }
        }

        //Double check and correct if all drivers and passengers really need optimization
        Iterator<DriverInfoWrapper> toOptimizeIterator = driversToOptimize.iterator();
        while (toOptimizeIterator.hasNext()) {
            DriverInfoWrapper toTest = toOptimizeIterator.next();
            for (ContactInfoWrapper passenger : toTest.getPassengersInCar())
                passengersToOptimize.remove(passenger);
            if (toTest.getFreeSpots() <= 0) toOptimizeIterator.remove();
        }

        //Do nothing if we have no algorithm or nothing to optimize
        if (algorithm < 0 || driversToOptimize.isEmpty() || passengersToOptimize.isEmpty()) return;


        switch (algorithm) {
            case ALGORITHM_LATLONG_PASSENGERS_FIRST:
                if (clusterType == null) latLongPassengersFirst();
                else latLongPassengersFirstCluster(clusterType);
                break;
            case ALGORITHM_LATLONG_DRIVERS_FIRST:
                if (clusterType == null) latLongDriversFirst();
                else latLongDriverFistCluster(clusterType);
                break;
            case ALGORITHM_NAIVE_HUNGARIAN:
                if (clusterType == null) naiveHungarian();
                else naiveHungarian(clusterType);
                break;
            case ALGORITHM_CLUSTER_MATCHING:
                if (clusterType != null) clusterMatch(clusterType);
                break;
        }
    }

    private void latLongPassengersFirst() {
        List<ContactInfoWrapper> allContacts = new ArrayList<ContactInfoWrapper>(passengersToOptimize);
        for (ContactInfoWrapper toOptimize : allContacts) {
            DriverInfoWrapper driver = getClosestDriver(toOptimize, false);
            if (driver == null) break;
            driver.addPassengerToCar(toOptimize);
            passengersToOptimize.remove(toOptimize);
        }
    }

    private DriverInfoWrapper getClosestDriver(ContactInfoWrapper passenger, boolean allowOverstuff) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper rDriver = null;
        for (DriverInfoWrapper driver : driversToOptimize) {
            if (driver.getFreeSpots() <= 0 && !allowOverstuff) continue;
            double curDist = distBetweenHouses(driver, passenger);
            if (curDist < minDist) {
                minDist = curDist;
                rDriver = driver;
            }
        }
        return rDriver;
    }

    private void latLongDriversFirst() {
        boolean allFull = false;
        while (!passengersToOptimize.isEmpty() && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : driversToOptimize) {
                if (toOptimize.getFreeSpots() <= 0) continue;
                ContactInfoWrapper passenger = getClosestPassenger(toOptimize);
                if (passenger == null) break;
                toOptimize.addPassengerToCar(passenger);
                passengersToOptimize.remove(passenger);
                allFull = false;
            }
        }
    }

    private ContactInfoWrapper getClosestPassenger(DriverInfoWrapper driver) {
        double minDist = Double.MAX_VALUE;
        ContactInfoWrapper rPassenger = null;
        for (ContactInfoWrapper passenger : passengersToOptimize) {
            double curDist = distBetweenHouses(driver, passenger);
            if (curDist < minDist) {
                minDist = curDist;
                rPassenger = passenger;
            }
        }
        return rPassenger;
    }

    private void naiveHungarian() {
        List<Integer> driverIndicies = new ArrayList<Integer>();
        List<ContactInfoWrapper> indexedPassengers = new ArrayList<ContactInfoWrapper>(passengersToOptimize);
        for (DriverInfoWrapper driver : driversToOptimize)
            for (int i = 0; i < driver.getFreeSpots(); i++)
                driverIndicies.add(driversToOptimize.indexOf(driver));

        double[][] costs = new double[driverIndicies.size()][indexedPassengers.size()];

        for (int r = 0; r < driverIndicies.size(); r++) {
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(r));
            for (int c = 0; c < indexedPassengers.size(); c++) {
                ContactInfoWrapper passenger = indexedPassengers.get(c);
                costs[r][c] = distBetweenHouses(driver, passenger);
            }
        }

        int[] assignments = (new HungarianAlgorithm(costs)).execute();

        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == -1) continue;
            ContactInfoWrapper passenger = indexedPassengers.get(assignments[i]);
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(i));
            driver.addPassengerToCar(passenger);
            passengersToOptimize.remove(passenger);
        }
    }

    private void naiveHungarian(Class<? extends RidesCluster> clusterType) {
        List<Integer> driverIndicies = new ArrayList<Integer>();
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (int i = 0; i < driver.getFreeSpots(); i++) {
                driverIndicies.add(driversToOptimize.indexOf(driver));
            }
        }

        List<RidesCluster> clusters = RidesCluster.clusterPassengers(clusterType, passengersToOptimize);
        List<Integer> clusterIndecies = new ArrayList<Integer>();
        for (RidesCluster cluster : clusters) {
            for (int i = 0; i < cluster.getSize(); i++) {
                clusterIndecies.add(clusters.indexOf(cluster));
            }
        }

        double[][] cost = new double[driverIndicies.size()][clusterIndecies.size()];

        for (int r = 0; r < driverIndicies.size(); r++) {
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(r));
            for (int c = 0; c < clusterIndecies.size(); c++) {
                RidesCluster cluster = clusters.get(clusterIndecies.get(c));
                cost[r][c] = distBetweenHouses(driver, cluster.getCenter());
            }
        }

        int[] assignments = new HungarianAlgorithm(cost).execute();

        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == -1) continue;
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(i));
            RidesCluster assignedCluster = clusters.get(clusterIndecies.get(assignments[i]));
            ContactInfoWrapper toAdd = assignedCluster.getPassengersInCluster()[0];
            driver.addPassengerToCar(toAdd);
            assignedCluster.removePassengerFromCluster(toAdd);
            passengersToOptimize.remove(toAdd);
        }
    }

    private void latLongDriverFistCluster(Class<? extends RidesCluster> clusterType) {
        Set<RidesCluster> clusters = new HashSet<RidesCluster>(RidesCluster.clusterPassengers(clusterType, passengersToOptimize));
        boolean allFull = false;
        while (!clusters.isEmpty() && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : driversToOptimize) {
                if (toOptimize.getFreeSpots() <= 0) continue;
                RidesCluster ridesCluster = getClosestCluster(toOptimize, clusters, false);
                if (ridesCluster == null) break;
                allFull = false;
                ContactInfoWrapper[] inCluster = ridesCluster.getPassengersInCluster();
                for (ContactInfoWrapper clusterContact : inCluster) {
                    if (toOptimize.getFreeSpots() <= 0) break;
                    toOptimize.addPassengerToCar(clusterContact);
                    ridesCluster.removePassengerFromCluster(clusterContact);
                    passengersToOptimize.remove(clusterContact);
                }
                if (ridesCluster.getSize() <= 0) clusters.remove(ridesCluster);
            }
        }
    }

    private RidesCluster getClosestCluster(DriverInfoWrapper driver, Collection<RidesCluster> allClusters, boolean allowSplit) {
        double minDist = Double.MAX_VALUE;
        RidesCluster closestCluster = null;
        for (RidesCluster cluster : allClusters) {
            if (!allowSplit && cluster.getSize() > driver.getFreeSpots()) continue;
            double curDist = distBetweenHouses(driver, cluster.getCenter());
            if (curDist < minDist) {
                closestCluster = cluster;
                minDist = curDist;
            }
        }
        return closestCluster;
    }

    private void latLongPassengersFirstCluster(Class<? extends RidesCluster> clusterType) {
        List<RidesCluster> clusters = RidesCluster.clusterPassengers(clusterType, passengersToOptimize);
        for (RidesCluster cluster : clusters) {
            while (cluster.getSize() > 0) {
                DriverInfoWrapper closestDriver = getClosestDriver(cluster, driversToOptimize, false);
                if (closestDriver == null) break;
                for (ContactInfoWrapper passengerToAdd : cluster.getPassengersInCluster()) {
                    if (closestDriver.getFreeSpots() <= 0) break;
                    closestDriver.addPassengerToCar(passengerToAdd);
                    cluster.removePassengerFromCluster(passengerToAdd);
                    passengersToOptimize.remove(passengerToAdd);
                }
            }
        }
    }

    private DriverInfoWrapper getClosestDriver(RidesCluster cluster, Collection<DriverInfoWrapper> allDrivers, boolean allowOverstuff) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper closestDriver = null;
        for (DriverInfoWrapper driver : allDrivers) {
            if (driver.getFreeSpots() <= 0 && !allowOverstuff) continue;
            double curDist = distBetweenHouses(driver, cluster.getCenter());
            if (curDist < minDist) {
                minDist = curDist;
                closestDriver = driver;
            }
        }
        return closestDriver;
    }

    private void clusterMatch(Class<? extends RidesCluster> clusterType) {
        Set<ContactInfoWrapper> allPassengers = new HashSet<ContactInfoWrapper>(passengersToOptimize);
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (ContactInfoWrapper passenger : driver.getPassengersInCar()) {
                allPassengers.add(passenger);
            }
        }
        List<RidesCluster> clusters = RidesCluster.clusterPassengers(clusterType, allPassengers);
        Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings = new HashMap<DriverInfoWrapper, Set<RidesCluster>>();
        Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings = new HashMap<RidesCluster, Set<DriverInfoWrapper>>();

        //Create a mapping from drivers to all clusters they have passengers in.
        //We assume that each driver has a passenger representing themselves already
        //in their car.
        createMappings(clusters, driverMappings, clusterMappings);

        //Optimize 1-1 mappings
        oneToOneOptimize(driverMappings, clusterMappings);

        //Optimize 1 cluster to many drivers, to maximize
        //cleared clusters
        oneClusterManyDriversOptimize(driverMappings, clusterMappings);

        //Optimize 1 driver to many clusters
        oneDriverManyClustersOptimize(driverMappings, clusterMappings);

        //Optimize the rest of the clusters
        manyToManyOptimize(driverMappings, clusterMappings);

    }

    private void createMappings(List<RidesCluster> clusters,
                                Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings,
                                Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings) {

        for (DriverInfoWrapper driver : driversToOptimize)
            driverMappings.put(driver, new HashSet<RidesCluster>());
        for (RidesCluster cluster : clusters)
            clusterMappings.put(cluster, new HashSet<DriverInfoWrapper>());
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (RidesCluster cluster : clusters) {
                for (ContactInfoWrapper passenger : driver.getPassengersInCar()) {
                    if (cluster.containsContact(passenger)) {
                        driverMappings.get(driver).add(cluster);
                        clusterMappings.get(cluster).add(driver);
                    }
                }
            }
        }
    }

    private void oneToOneOptimize(Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings,
                                  Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings) {

        for (DriverInfoWrapper driver : driverMappings.keySet()) {
            Set<RidesCluster> driversClusters = driverMappings.get(driver);
            if (driversClusters == null || driversClusters.size() != 1) continue;
            RidesCluster onlyCluster = driversClusters.toArray(new RidesCluster[1])[0];
            Set<DriverInfoWrapper> clustersDrivers = clusterMappings.get(onlyCluster);
            if (clustersDrivers == null || clustersDrivers.size() != 1 || !clustersDrivers.contains(driver))
                continue;
            for (ContactInfoWrapper inCluster : onlyCluster.getPassengersInCluster()) {
                if (driver.getFreeSpots() <= 0) {
                    driversToOptimize.remove(driver);
                    break;
                }
                driver.addPassengerToCar(inCluster);
                passengersToOptimize.remove(inCluster);
                onlyCluster.removePassengerFromCluster(inCluster);
            }
        }
    }

    private void oneClusterManyDriversOptimize(Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings,
                                               Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings) {

        for (RidesCluster cluster : clusterMappings.keySet()) {
            Iterator<DriverInfoWrapper> clustersDriversIterator = clusterMappings.get(cluster).iterator();
            while (clustersDriversIterator.hasNext()) {
                DriverInfoWrapper testDriver = clustersDriversIterator.next();
                Set<RidesCluster> driversClusters = driverMappings.get(testDriver);
                if (driversClusters.size() > 1) clustersDriversIterator.remove();
            }
            if (clusterMappings.get(cluster) == null || clusterMappings.get(cluster).size() <= 1)
                continue;
            for (DriverInfoWrapper driver : clusterMappings.get(cluster)) {
                ContactInfoWrapper[] passengersInCluster = cluster.getPassengersInCluster();
                for (ContactInfoWrapper inCluster : passengersInCluster) {
                    if (driver.getFreeSpots() <= 0) {
                        driversToOptimize.remove(driver);
                        break;
                    }
                    driver.addPassengerToCar(inCluster);
                    cluster.removePassengerFromCluster(inCluster);
                    passengersToOptimize.remove(inCluster);
                }
            }
        }
    }

    private void oneDriverManyClustersOptimize(Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings,
                                               Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings) {

        for (DriverInfoWrapper driver : driverMappings.keySet()) {
            Set<RidesCluster> driversClusters = driverMappings.get(driver);
            for (RidesCluster testCluster : driverMappings.get(driver)) {
                Set<DriverInfoWrapper> clustersDrivers = clusterMappings.get(testCluster);
                if (testCluster.getSize() == 0 || clustersDrivers.size() > 1)
                    driversClusters.remove(testCluster);
            }
            if (driversClusters == null || driversClusters.size() <= 1) continue;
            for (RidesCluster cluster : driversClusters) {
                ContactInfoWrapper[] passengersInCluster = cluster.getPassengersInCluster();
                for (ContactInfoWrapper inCluster : passengersInCluster) {
                    if (driver.getFreeSpots() <= 0) {
                        driversToOptimize.remove(driver);
                        break;
                    }
                    driver.addPassengerToCar(inCluster);
                    cluster.removePassengerFromCluster(inCluster);
                    passengersToOptimize.remove(inCluster);
                }
            }
        }
    }

    private void manyToManyOptimize(Map<DriverInfoWrapper, Set<RidesCluster>> driverMappings,
                                    Map<RidesCluster, Set<DriverInfoWrapper>> clusterMappings) {

        for (DriverInfoWrapper driver : driverMappings.keySet()) {
            for (RidesCluster validCluster : driverMappings.get(driver)) {
                if (!driversToOptimize.contains(driver)) break;
                for (ContactInfoWrapper passengerInCluster : validCluster.getPassengersInCluster()) {
                    if (driver.getFreeSpots() <= 0) {
                        driversToOptimize.remove(driver);
                        break;
                    }
                    driver.addPassengerToCar(passengerInCluster);
                }
            }
        }

    }
}


