package org.ramonazaapi.rides.algorithms;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;
import org.ramonazaapi.rides.clusters.RidesCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ilan on 10/21/15.
 */
public class SerialArson implements RidesOptimizer.RidesAlgorithm {

    @Override
    public void optimize(double startx, double starty, Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers) {

        List<ContactInfoWrapper> allContacts = new ArrayList<ContactInfoWrapper>(allPassengers);
        for (ContactInfoWrapper toOptimize : allContacts) {
            DriverInfoWrapper driver = getClosestDriver(toOptimize, allDrivers, false);
            if (driver == null) break;
            driver.addPassengersToCar(toOptimize);
        }
    }

    private DriverInfoWrapper getClosestDriver(ContactInfoWrapper passenger, Collection<DriverInfoWrapper> driversToOptimize, boolean allowOverstuff) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper rDriver = null;
        for (DriverInfoWrapper driver : driversToOptimize) {
            if (driver.getFreeSpots() <= 0 && !allowOverstuff) continue;
            double curDist = RidesOptimizer.distBetweenHouses(driver, passenger);
            if (curDist < minDist) {
                minDist = curDist;
                rDriver = driver;
            }
        }
        return rDriver;
    }

    @Override
    public void optimize(double startx, double starty, Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers, Class<? extends RidesCluster> clusterType) {
        List<RidesCluster> clusters = RidesCluster.clusterPassengers(clusterType, allPassengers);
        for (RidesCluster cluster : clusters) {
            while (cluster.getSize() > 0) {
                DriverInfoWrapper closestDriver = getClosestDriver(cluster, allDrivers, false);
                if (closestDriver == null) break;
                for (ContactInfoWrapper passengerToAdd : cluster.getPassengersInCluster()) {
                    if (closestDriver.getFreeSpots() <= 0) break;
                    closestDriver.addPassengersToCar(passengerToAdd);
                    cluster.removePassengerFromCluster(passengerToAdd);
                }
            }
        }
    }

    private DriverInfoWrapper getClosestDriver(RidesCluster cluster, Collection<DriverInfoWrapper> allDrivers, boolean allowOverstuff) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper closestDriver = null;
        for (DriverInfoWrapper driver : allDrivers) {
            if (driver.getFreeSpots() <= 0 && !allowOverstuff) continue;
            double curDist = RidesOptimizer.distBetweenHouses(driver, cluster.getCenter());
            if (curDist < minDist) {
                minDist = curDist;
                closestDriver = driver;
            }
        }
        return closestDriver;
    }
}
