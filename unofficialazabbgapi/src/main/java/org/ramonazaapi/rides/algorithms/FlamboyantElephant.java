package org.ramonazaapi.rides.algorithms;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;
import org.ramonazaapi.rides.clusters.RidesCluster;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ilan on 10/21/15.
 */
public class FlamboyantElephant implements RidesOptimizer.RidesAlgorithm {

    Set<ContactInfoWrapper> optimizedPassengers;
    Set<DriverInfoWrapper> optimizedDrivers;

    @Override
    public void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers) {
        optimizedDrivers = new HashSet<>();
        optimizedPassengers = new HashSet<>();
        boolean allFull = false;
        int allPassengerSize = allPassengers.size();
        while (optimizedPassengers.size() < allPassengerSize && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : allDrivers) {
                if (toOptimize.getFreeSpots() <= 0) {
                    optimizedDrivers.add(toOptimize);
                    continue;
                } else {
                    ContactInfoWrapper passenger = getClosestPassenger(toOptimize, allPassengers);
                    if (passenger == null) break;
                    toOptimize.addPassengerToCar(passenger);
                    optimizedPassengers.add(passenger);
                    allFull = false;
                }
            }
        }
    }

    private ContactInfoWrapper getClosestPassenger(DriverInfoWrapper driver, Collection<ContactInfoWrapper> passengersToOptimize) {
        double minDist = Double.MAX_VALUE;
        ContactInfoWrapper rPassenger = null;
        for (ContactInfoWrapper passenger : passengersToOptimize) {
            double curDist = RidesOptimizer.distBetweenHouses(driver, passenger);
            if (curDist < minDist) {
                minDist = curDist;
                rPassenger = passenger;
            }
        }
        return rPassenger;
    }

    @Override
    public void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers, Class<? extends RidesCluster> clusterType) {
        optimizedDrivers = new HashSet<>();
        optimizedPassengers = new HashSet<>();
        Set<RidesCluster> clusters = new HashSet<RidesCluster>(RidesCluster.clusterPassengers(clusterType, allPassengers));
        boolean allFull = false;
        while (!clusters.isEmpty() && !allFull) {
            allFull = true;
            for (DriverInfoWrapper toOptimize : allDrivers) {
                if (toOptimize.getFreeSpots() <= 0) continue;
                RidesCluster ridesCluster = getClosestCluster(toOptimize, clusters, false);
                if (ridesCluster == null) break;
                allFull = false;
                ContactInfoWrapper[] inCluster = ridesCluster.getPassengersInCluster();
                for (ContactInfoWrapper clusterContact : inCluster) {
                    if (toOptimize.getFreeSpots() <= 0) break;
                    toOptimize.addPassengerToCar(clusterContact);
                    ridesCluster.removePassengerFromCluster(clusterContact);
                    optimizedPassengers.add(clusterContact);
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
            double curDist = RidesOptimizer.distBetweenHouses(driver, cluster.getCenter());
            if (curDist < minDist) {
                closestCluster = cluster;
                minDist = curDist;
            }
        }
        return closestCluster;
    }
}
