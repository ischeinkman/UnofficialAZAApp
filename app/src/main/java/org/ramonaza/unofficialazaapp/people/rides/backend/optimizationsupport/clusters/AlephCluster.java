package org.ramonaza.unofficialazaapp.people.rides.backend.optimizationsupport.clusters;

import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A parent class for aleph clusters.
 * These clusters store and represent alephs close enough locationally
 * to be treated as a single unit for rides purposes.
 * Created by ilan on 10/2/15.
 */
public abstract class AlephCluster {

    protected Set<ContactInfoWrapper> contactsInCluster;

    /**
     * Creates a cluster based on an initial contact
     * @param firstContact the first contact in the cluster
     */
    public AlephCluster(ContactInfoWrapper firstContact) {
        contactsInCluster = new HashSet<ContactInfoWrapper>();
        contactsInCluster.add(firstContact);
        recalculate();
    }

    /**
     * @param clusterType the type of cluster to use
     * @param toCluster   the contacts to cluster
     * @return a list of clusters with alephs
     */
    public static List<AlephCluster> clusterAlephs(Class<? extends AlephCluster> clusterType, Collection<ContactInfoWrapper> toCluster) {
        List<AlephCluster> clusters = new ArrayList<AlephCluster>();
        Set<ContactInfoWrapper> toClusterSet=new HashSet<ContactInfoWrapper>(toCluster);
        Iterator<ContactInfoWrapper> contactIterator=toClusterSet.iterator();
        while(contactIterator.hasNext()) {
            ContactInfoWrapper contact=contactIterator.next();
            boolean inCluster = false;
            for (AlephCluster cluster : clusters) {
                if (cluster.addAlephToCluster(contact)) {
                    inCluster = true;
                    break;
                }
            }
            if (!inCluster) {
                try {
                    Constructor<? extends AlephCluster> constructor = clusterType.getConstructor(ContactInfoWrapper.class);
                    AlephCluster newCluster = constructor.newInstance(contact);
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
     * Checks whether an aleph lies within this cluster's range.
     * @param toCheck the aleph to check
     * @return if the aleph lies within the cluster
     */
    public abstract boolean alephInCluster(ContactInfoWrapper toCheck);

    /**
     * Adds an aleph to the cluster.
     * @param toCheck the aleph to add
     * @return whether the addition was successful
     */
    public boolean addAlephToCluster(ContactInfoWrapper toCheck) {
        if (!alephInCluster(toCheck)) return false;
        boolean rval = contactsInCluster.add(toCheck);
        recalculate();
        return rval;
    }

    public boolean removeAlephFromCluster(ContactInfoWrapper toCheck){
        boolean isSuccess = contactsInCluster.remove(toCheck);
        if(isSuccess) recalculate();
        return isSuccess;
    }

    /**
     * Runs every time an aleph is added.
     * If a radius or center needs to be
     * recalculated whenever an aleph is
     * added, it is done here.
     */
    public abstract void recalculate();

    /**
     * Get the amount of people in the cluster.
     * @return the amount of people in the cluster
     */
    public int getSize() {
        return contactsInCluster.size();
    }

    /**
     * Gets the alephs in this cluster.
     * @return the alephs in this cluster
     */
    public ContactInfoWrapper[] getAlephsInCluster() {
        return contactsInCluster.toArray(new ContactInfoWrapper[contactsInCluster.size()]);
    }

    /**
     * Gets the center of cluster, which is considered to be the
     * coordinate the cluster opperates under for rides purposes.
     * Even non-circular clusters should have centers.
     * @return the center of the cluster
     */
    public abstract double[] getCenter();
}