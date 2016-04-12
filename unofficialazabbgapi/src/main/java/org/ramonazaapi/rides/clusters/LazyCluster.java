package org.ramonazaapi.rides.clusters;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;

/**
 * A cluster based on drawing a circle around the initial with radius
 * {@link LazyCluster#RADIUS} and adding anything in that circle.
 * Created by ilan on 10/2/15.
 */
public class LazyCluster extends RidesCluster {

    private static final double RADIUS = 0.00619;
    private double[] center;

    public LazyCluster(ContactInfoWrapper firstContact) {
        super(firstContact);
        center = new double[]{firstContact.getX(), firstContact.getY()};
    }

    @Override
    public boolean passengerLiesInCluster(ContactInfoWrapper toCheck) {
        return (RidesOptimizer.distBetweenHouses(toCheck, center) <= RADIUS);
    }

    @Override
    public void recalculate() {
        return;
    }

    public double[] getCenter() {
        return center;
    }

    public double getRadius() {
        return RADIUS;
    }
}
