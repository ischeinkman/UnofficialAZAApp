package org.ramonaza.unofficialazaapp.people.rides.backend;

import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.HungarianAlgorithm;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.AlephCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.ExpansionistCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.HungryCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.LazyCluster;
import org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters.SnakeCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ilanscheinkman on 9/1/15.
 */
public class RidesOptimizer {

    /**
     * Calculate rides based on latitude and longitude, iterating over the passengers
     * and assigning them a driver.
     */
    public static final int ALGORITHM_LATLONG_ALEPHS_FIRST = 0;

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

    public static final int ALGORITHM_LLAF_SNAKE=3;
    public static final int ALGORITHM_LLAF_EXP=4;
    public static final int ALGORITHM_LLAF_HUNGRY=5;
    public static final int ALGORITHM_LLAF_LAZY=6;

    public static final int ALGORITHM_LLDF_SNAKE=7;
    public static final int ALGORITHM_LLDF_EXP=8;
    public static final int ALGORITHM_LLDF_HUNGRY=9;
    public static final int ALGORITHM_LLDF_LAZY=10;



    private Set<ContactInfoWrapper> alephsToOptimize;
    private List<DriverInfoWrapper> driversToOptimize;
    private int algorithm;
    private boolean retainPreexisting;

    public RidesOptimizer() {
        this.alephsToOptimize = new HashSet<ContactInfoWrapper>();
        this.driversToOptimize = new ArrayList<DriverInfoWrapper>();
    }

    public static double distBetweenHouses(DriverInfoWrapper driver, ContactInfoWrapper aleph) {
        return Math.sqrt((
                aleph.getLatitude() - driver.getLatitude())
                * (aleph.getLatitude() - driver.getLatitude())
                + (aleph.getLongitude() - driver.getLongitude())
                * (aleph.getLongitude() - driver.getLongitude()));
    }

    public static double distBetweenHouses(ContactInfoWrapper aleph1, ContactInfoWrapper aleph2) {
        return Math.sqrt((
                aleph1.getLatitude() - aleph2.getLatitude())
                * (aleph1.getLatitude() - aleph2.getLatitude())
                + (aleph1.getLongitude() - aleph2.getLongitude())
                * (aleph1.getLongitude() - aleph2.getLongitude()));
    }

    public static double distBetweenHouses(ContactInfoWrapper aleph, double[] coords) {
        return Math.sqrt((
                aleph.getLatitude() - coords[0])
                * (aleph.getLatitude() - coords[0])
                + (aleph.getLongitude() - coords[1])
                * (aleph.getLongitude() - coords[1]));
    }

    public static double distBetweenHouses(DriverInfoWrapper driver, double[] coords) {
        return Math.sqrt((
                driver.getLatitude() - coords[0])
                * (driver.getLatitude() - coords[0])
                + (driver.getLongitude() - coords[1])
                * (driver.getLongitude() - coords[1]));
    }

    /**
     * Get the loaded alephs not currently in a car.
     *
     * @return the driverless alephs
     */
    public ContactInfoWrapper[] getDriverless() {
        return alephsToOptimize.toArray(new ContactInfoWrapper[alephsToOptimize.size()]);
    }

    /**
     * Get the currently loaded drivers (including all of the alephs in their cars).
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
        for (ContactInfoWrapper a : passengersToLoad) alephsToOptimize.add(a);
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
     * @return this
     */
    public RidesOptimizer setAlgorithm(int algorithm, boolean retainPreexisting) {
        this.algorithm = algorithm;
        this.retainPreexisting = retainPreexisting;
        return this;
    }

    /**
     * Optimize the rides. This app results in either all loaded passengers being in a car or all
     * loaded cars being full.
     */
    public void optimize() {
        if (!retainPreexisting) {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper aleph : new ArrayList<ContactInfoWrapper>(driver.getAlephsInCar())) {
                    if (distBetweenHouses(driver, aleph) != 0) {
                        driver.removeAlephFromCar(aleph);
                        alephsToOptimize.add(aleph);
                    }
                }
            }
        }
        else {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper contact : driver.getAlephsInCar()) {
                    if (alephsToOptimize.contains(contact)) alephsToOptimize.remove(contact);
                }
            }
        }
        if (algorithm < 0 || driversToOptimize.isEmpty() || alephsToOptimize.isEmpty()) return;
        switch (algorithm) {
            case ALGORITHM_LATLONG_ALEPHS_FIRST:
                latLongAlephsFirst();
                break;
            case ALGORITHM_LATLONG_DRIVERS_FIRST:
                latLongDriversFirst();
                break;
            case ALGORITHM_NAIVE_HUNGARIAN:
                naiveHungarian();
                break;
            case ALGORITHM_LLAF_EXP:
                latLongAlephsFirstCluster(ExpansionistCluster.class);
                break;
            case ALGORITHM_LLAF_HUNGRY:
                latLongAlephsFirstCluster(HungryCluster.class);
                break;
            case ALGORITHM_LLAF_LAZY:
                latLongAlephsFirstCluster(LazyCluster.class);
                break;
            case ALGORITHM_LLAF_SNAKE:
                latLongAlephsFirstCluster(SnakeCluster.class);
                break;
            case ALGORITHM_LLDF_EXP:
                latLongDriverFistCluster(ExpansionistCluster.class);
                break;
            case ALGORITHM_LLDF_HUNGRY:
                latLongDriverFistCluster(HungryCluster.class);
                break;
            case ALGORITHM_LLDF_LAZY:
                latLongDriverFistCluster(LazyCluster.class);
                break;
            case ALGORITHM_LLDF_SNAKE:
                latLongDriverFistCluster(SnakeCluster.class);
                break;

        }
    }

    private void latLongAlephsFirst() {
        List<ContactInfoWrapper> allContacts = new ArrayList<ContactInfoWrapper>(alephsToOptimize);
        for (ContactInfoWrapper toOptimize : allContacts) {
            DriverInfoWrapper driver = getClosestDriver(toOptimize, false);
            if (driver == null) break;
            driver.addAlephToCar(toOptimize);
            alephsToOptimize.remove(toOptimize);
        }
    }

    private DriverInfoWrapper getClosestDriver(ContactInfoWrapper aleph, boolean allowOverstuff) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper rDriver = null;
        for (DriverInfoWrapper driver : driversToOptimize) {
            if (driver.getFreeSpots() <= 0 && !allowOverstuff) continue;
            double curDist = distBetweenHouses(driver, aleph);
            if (curDist < minDist) {
                minDist = curDist;
                rDriver = driver;
            }
        }
        return rDriver;
    }

    private void latLongDriversFirst() {
        boolean allFull = false;
        while (!alephsToOptimize.isEmpty() && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : driversToOptimize) {
                if (toOptimize.getFreeSpots() <= 0) continue;
                ContactInfoWrapper aleph = getClosestAleph(toOptimize);
                if (aleph == null) break;
                toOptimize.addAlephToCar(aleph);
                alephsToOptimize.remove(aleph);
                allFull = false;
            }
        }
    }

    private ContactInfoWrapper getClosestAleph(DriverInfoWrapper driver) {
        double minDist = Double.MAX_VALUE;
        ContactInfoWrapper rAleph = null;
        for (ContactInfoWrapper aleph : alephsToOptimize) {
            double curDist = distBetweenHouses(driver, aleph);
            if (curDist < minDist) {
                minDist = curDist;
                rAleph = aleph;
            }
        }
        return rAleph;
    }

    private void naiveHungarian() {
        List<Integer> driverIndicies = new ArrayList<Integer>();
        List<ContactInfoWrapper> indexedAlephs = new ArrayList<ContactInfoWrapper>(alephsToOptimize);
        for (DriverInfoWrapper driver : driversToOptimize)
            for (int i = 0; i < driver.getFreeSpots(); i++)
                driverIndicies.add(driversToOptimize.indexOf(driver));
        double[][] costs = new double[driverIndicies.size()][indexedAlephs.size()];
        for (int r = 0; r < driverIndicies.size(); r++) {
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(r));
            for (int c = 0; c < indexedAlephs.size(); c++) {
                ContactInfoWrapper aleph = indexedAlephs.get(c);
                costs[r][c] = distBetweenHouses(driver, aleph);
            }
        }
        int[] assignments = (new HungarianAlgorithm(costs)).execute();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == -1) continue;
            ContactInfoWrapper aleph = indexedAlephs.get(assignments[i]);
            DriverInfoWrapper driver = driversToOptimize.get(driverIndicies.get(assignments[i]));
            driver.addAlephToCar(aleph);
            alephsToOptimize.remove(aleph);
        }
    }

    private void latLongDriverFistCluster(Class<? extends AlephCluster> clusterType){
        Set<AlephCluster> clusters = new HashSet<AlephCluster>(AlephCluster.clusterAlephs(clusterType, alephsToOptimize));
        boolean allFull = false;
        while (!clusters.isEmpty() && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : driversToOptimize) {
                if (toOptimize.getFreeSpots() <= 0) continue;
                AlephCluster alephCluster = getClosestCluster(toOptimize, clusters, false);
                if (alephCluster == null) break;
                allFull = false;
                ContactInfoWrapper[] inCluster=alephCluster.getAlephsInCluster();
                for(ContactInfoWrapper clusterContact:inCluster){
                    if(toOptimize.getFreeSpots()<=0) break;
                    toOptimize.addAlephToCar(clusterContact);
                    alephCluster.removeAlephFromCluster(clusterContact);
                    alephsToOptimize.remove(clusterContact);
                }
                if(alephCluster.getSize()<=0) clusters.remove(alephCluster);
            }
        }
    }

    private AlephCluster getClosestCluster(DriverInfoWrapper driver, Collection<AlephCluster> allClusters, boolean allowSplit){
        double minDist=Double.MAX_VALUE;
        AlephCluster closestCluster=null;
        for(AlephCluster cluster:allClusters){
            if(!allowSplit && cluster.getSize()>driver.getFreeSpots()) continue;
            double curDist=distBetweenHouses(driver,cluster.getCenter());
            if(curDist<minDist){
                closestCluster=cluster;
                minDist=curDist;
            }
        }
        return closestCluster;
    }

    private void latLongAlephsFirstCluster(Class<? extends AlephCluster> clusterType){
        List<AlephCluster> clusters=AlephCluster.clusterAlephs(clusterType, alephsToOptimize);
        for(AlephCluster cluster: clusters){
            while(cluster.getSize()>0){
                DriverInfoWrapper closestDriver=getClosestDriver(cluster,driversToOptimize,false);
                if(closestDriver == null) break;
                for(ContactInfoWrapper passengerToAdd:cluster.getAlephsInCluster()){
                    if(closestDriver.getFreeSpots()<=0) break;
                    closestDriver.addAlephToCar(passengerToAdd);
                    cluster.removeAlephFromCluster(passengerToAdd);
                    alephsToOptimize.remove(passengerToAdd);
                }
            }
        }
    }

    private DriverInfoWrapper getClosestDriver(AlephCluster cluster, Collection<DriverInfoWrapper> allDrivers, boolean allowOverstuff){
        double minDist=Double.MAX_VALUE;
        DriverInfoWrapper closestDriver=null;
        for(DriverInfoWrapper driver: allDrivers){
            if(driver.getFreeSpots()<=0 && !allowOverstuff) continue;
            double curDist=distBetweenHouses(driver,cluster.getCenter());
            if(curDist<minDist){
                minDist=curDist;
                closestDriver=driver;
            }
        }
        return closestDriver;
    }

}


