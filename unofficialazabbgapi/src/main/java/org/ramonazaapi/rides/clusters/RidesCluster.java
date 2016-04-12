package org.ramonazaapi.rides.clusters;

import org.ramonazaapi.contacts.ContactInfoWrapper;
import org.ramonazaapi.interfaces.LocationPoint;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A parent class for rides clusters.
 * These clusters store and represent people close enough locationally
 * to be treated as a single unit for rides purposes.
 * Created by ilan on 10/2/15.
 */
public abstract class RidesCluster implements LocationPoint {

    protected Set<ContactInfoWrapper> contactsInCluster;

    /**
     * Creates a cluster based on an initial contact
     *
     * @param firstContact the first contact in the cluster
     */
    public RidesCluster(ContactInfoWrapper firstContact) {
        contactsInCluster = new HashSet<ContactInfoWrapper>();
        contactsInCluster.add(firstContact);
        recalculate();
    }

    /**
     * @param clusterType the type of cluster to use
     * @param toCluster   the contacts to cluster
     * @return a list of clusters with people
     */
    public static List<RidesCluster> clusterPassengers(Class<? extends RidesCluster> clusterType, Collection<ContactInfoWrapper> toCluster) {
        List<RidesCluster> clusters = new ArrayList<RidesCluster>();
        Set<ContactInfoWrapper> toClusterSet = new HashSet<ContactInfoWrapper>(toCluster);
        Iterator<ContactInfoWrapper> contactIterator = toClusterSet.iterator();
        while (contactIterator.hasNext()) {
            ContactInfoWrapper contact = contactIterator.next();
            boolean inCluster = false;
            for (RidesCluster cluster : clusters) {
                if (cluster.addPassengerToCluster(contact)) {
                    inCluster = true;
                    break;
                }
            }
            if (!inCluster) {
                try {
                    Constructor<? extends RidesCluster> constructor = clusterType.getConstructor(ContactInfoWrapper.class);
                    RidesCluster newCluster = constructor.newInstance(contact);
                    clusters.add(newCluster);
                } catch (Exception e) {
                    return null;
                }
            }
            contactIterator.remove();
        }
        return clusters;
    }

    /**
     * Checks whether someone lies within this cluster's range.
     *
     * @param toCheck the person to check
     * @return if the person lies within the cluster
     */
    public abstract boolean passengerLiesInCluster(ContactInfoWrapper toCheck);

    /**
     * Adds a passenger to the cluster.
     *
     * @param toCheck the passenger to add
     * @return whether the addition was successful
     */
    public boolean addPassengerToCluster(ContactInfoWrapper toCheck) {
        if (!passengerLiesInCluster(toCheck)) return false;
        boolean rval = contactsInCluster.add(toCheck);
        recalculate();
        return rval;
    }

    public boolean removePassengerFromCluster(ContactInfoWrapper toCheck) {
        boolean isSuccess = contactsInCluster.remove(toCheck);
        if (isSuccess) recalculate();
        return isSuccess;
    }

    /**
     * Runs every time an person is added.
     * If a radius or center needs to be
     * recalculated whenever a person is
     * added, it is done here.
     */
    public abstract void recalculate();

    /**
     * Get the amount of people in the cluster.
     *
     * @return the amount of people in the cluster
     */
    public int getSize() {
        return contactsInCluster.size();
    }

    /**
     * Gets the people in this cluster.
     *
     * @return the people in this cluster
     */
    public ContactInfoWrapper[] getPassengersInCluster() {
        return contactsInCluster.toArray(new ContactInfoWrapper[contactsInCluster.size()]);
    }

    /**
     * Gets the center of cluster, which is considered to be the
     * coordinate the cluster opperates under for rides purposes.
     * Even non-circular clusters should have centers.
     *
     * @return the center of the cluster
     */
    public abstract double[] getCenter();

    @Override
    public double getX() {
        return getCenter()[0];
    }

    @Override
    public double getY() {
        return getCenter()[1];
    }

    public boolean containsContact(ContactInfoWrapper contact) {
        return contactsInCluster.contains(contact);
    }


    public boolean containsContact(String name) {
        ContactInfoWrapper nameContact = new ContactInfoWrapper();
        nameContact.setName(name);
        return contactsInCluster.contains(nameContact);
    }
}