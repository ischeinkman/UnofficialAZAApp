package org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters;

import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesOptimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A cluster based on setting the center to
 * the average of the locations of the contacts
 * and expanding outwards from that point.
 * Created by ilan on 10/2/15.
 */
public class HungryCluster extends AlephCluster {

    public static final double RADIUS_ADDITION = 0.00619; //The smallest number that puts a certain Aleph X into my car that should go in my car

    private double[] center;
    private double innerRadius;

    public HungryCluster(ContactInfoWrapper firstContact) {
        super(firstContact);
    }

    public static List<? extends AlephCluster> clusterAlephs(Collection<ContactInfoWrapper> toCluster) {
        List<HungryCluster> clusters = new ArrayList<HungryCluster>();
        for (ContactInfoWrapper aleph : toCluster) {
            HungryCluster shortestCluster = null;
            double distToShortest = Double.MAX_VALUE;
            for (HungryCluster cluster : clusters) {
                double currentDist = RidesOptimizer.distBetweenHouses(aleph, cluster.getCenter());
                if (currentDist < distToShortest) {
                    distToShortest = currentDist;
                    shortestCluster = cluster;
                }
            }
            if (shortestCluster == null || !shortestCluster.addAlephToCluster(aleph)) {
                clusters.add(new HungryCluster(aleph));
            }
        }
        return clusters;
    }

    private void recalculateCenter() {
        int lSize = contactsInCluster.size();
        center = new double[]{0.0, 0.0};
        for (ContactInfoWrapper inCluster : contactsInCluster) {
            center[0] += inCluster.getLatitude();
            center[1] += inCluster.getLongitude();
        }
        center[0] /= lSize;
        center[1] /= lSize;
    }

    private void recalculateRadius() {
        innerRadius = 0.0;
        for (ContactInfoWrapper inCluster : contactsInCluster) {
            double currentDist = RidesOptimizer.distBetweenHouses(inCluster, center);
            if (innerRadius < currentDist) innerRadius = currentDist;
        }
    }

    @Override
    public boolean alephLiesInCluster(ContactInfoWrapper toCheck) {
        if (toCheck.getLatitude() == 0.0 && toCheck.getLongitude() == 0.0) return false;
        double distToCenter = RidesOptimizer.distBetweenHouses(toCheck, center);
        double outerRadius = innerRadius + RADIUS_ADDITION;
        return (distToCenter <= outerRadius);
    }

    @Override
    public void recalculate() {
        recalculateCenter();
        recalculateRadius();
    }

    public double[] getCenter() {
        return center;
    }

    public double getRadius() {
        return innerRadius + RADIUS_ADDITION;
    }


}