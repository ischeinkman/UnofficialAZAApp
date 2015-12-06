package org.ramonazaapi.rides.algorithms;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.DriverInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;
import org.ramonazaapi.rides.clusters.RidesCluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ilan on 10/21/15.
 */
public class ClusterMatch implements RidesOptimizer.RidesAlgorithm {

    private Set<DriverInfoWrapper> driversToOptimize;
    private Set<ContactInfoWrapper> passengersToOptimize;

    @Override
    public void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers) {
        return;
    }

    @Override
    public void optimize(Collection<ContactInfoWrapper> allPassengers, Collection<DriverInfoWrapper> allDrivers, Class<? extends RidesCluster> clusterType) {

        driversToOptimize = new HashSet<>(allDrivers);
        passengersToOptimize = new HashSet<>(allPassengers);
        for (DriverInfoWrapper driver : allDrivers) {
            if (driver.getFreeSpots() <= 0) driversToOptimize.remove(driver);
        }
        for (DriverInfoWrapper driver : driversToOptimize) {
            for (ContactInfoWrapper passenger : driver.getPassengersInCar()) {
                passengersToOptimize.add(passenger);
            }
        }
        List<RidesCluster> clusters = RidesCluster.clusterPassengers(clusterType, passengersToOptimize);
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
