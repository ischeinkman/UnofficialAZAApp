package org.ramonazaapi.rides.algorithms;

import org.ramonazaapi.interfaces.LocationPoint;
import org.ramonazaapi.rides.DriverInfoWrapper;
import org.ramonazaapi.rides.RidesOptimizer;

/**
 * Implementation of the naive hungarian algorithm that also takes the initial position into account with the costs.
 * Created by ilan on 3/3/16.
 */
public class NaiveHungarianWithStart extends NaiveHungarian {
    @Override
    public double getCost(double startx, double starty, DriverInfoWrapper driver, LocationPoint passenger) {
        return RidesOptimizer.distBetweenHouses(driver, passenger) + RidesOptimizer.distBetweenHouses(passenger, new double[]{startx, starty});
    }
}
