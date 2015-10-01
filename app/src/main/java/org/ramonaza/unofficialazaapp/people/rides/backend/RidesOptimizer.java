package org.ramonaza.unofficialazaapp.people.rides.backend;

import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Set<ContactInfoWrapper> alephsToOptimize;
    private List<DriverInfoWrapper> driversToOptimize;
    private int algorithm;
    private boolean retainPreexisting;

    public RidesOptimizer() {
        this.alephsToOptimize = new HashSet<ContactInfoWrapper>();
        this.driversToOptimize = new ArrayList<DriverInfoWrapper>();
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
        if (algorithm < 0 || driversToOptimize.isEmpty()) return;
        if (!retainPreexisting) {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper aleph : new ArrayList<ContactInfoWrapper>(driver.getAlephsInCar())) {
                    if (distBetweenHouses(driver, aleph) != 0) {
                        driver.removeAlephFromCar(aleph);
                        alephsToOptimize.add(aleph);
                    }
                }
            }
        } else {
            for (DriverInfoWrapper driver : driversToOptimize) {
                for (ContactInfoWrapper contact : driver.getAlephsInCar()) {
                    if (alephsToOptimize.contains(contact)) alephsToOptimize.remove(contact);
                }
            }
        }
        if (alephsToOptimize.isEmpty()) return;
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
        }
    }

    private void latLongAlephsFirst() {
        List<ContactInfoWrapper> allContacts = new ArrayList<ContactInfoWrapper>(alephsToOptimize);
        for (ContactInfoWrapper toOptimize : allContacts) {
            DriverInfoWrapper driver = getClosestDriver(toOptimize);
            if (driver == null) break;
            driver.addAlephToCar(toOptimize);
            alephsToOptimize.remove(toOptimize);
        }
    }

    private DriverInfoWrapper getClosestDriver(ContactInfoWrapper aleph) {
        double minDist = Double.MAX_VALUE;
        DriverInfoWrapper rDriver = null;
        for (DriverInfoWrapper driver : driversToOptimize) {
            if (driver.getFreeSpots() <= 0) continue;
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

    private double distBetweenHouses(DriverInfoWrapper driver, ContactInfoWrapper aleph) {
        return Math.sqrt((
                aleph.getLatitude() - driver.getLatitude())
                * (aleph.getLatitude() - driver.getLatitude())
                + (aleph.getLongitude() - driver.getLongitude())
                * (aleph.getLongitude() - driver.getLongitude()));
    }

    /**
     * Copyright (c) 2012 Kevin L. Stern
     * <p/>
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     * <p/>
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     * <p/>
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     * <p/>
     * An implementation of the Hungarian algorithm for solving the assignment
     * problem. An instance of the assignment problem consists of a number of
     * workers along with a number of jobs and a cost matrix which gives the cost of
     * assigning the i'th worker to the j'th job at position (i, j). The goal is to
     * find an assignment of workers to jobs so that no job is assigned more than
     * one worker and so that no worker is assigned to more than one job in such a
     * manner so as to minimize the total cost of completing the jobs.
     * <p/>
     * <p/>
     * An assignment for a cost matrix that has more workers than jobs will
     * necessarily include unassigned workers, indicated by an assignment value of
     * -1; in no other circumstance will there be unassigned workers. Similarly, an
     * assignment for a cost matrix that has more jobs than workers will necessarily
     * include unassigned jobs; in no other circumstance will there be unassigned
     * jobs. For completeness, an assignment for a square cost matrix will give
     * exactly one unique worker to each job.
     * <p/>
     * <p/>
     * This version of the Hungarian algorithm runs in time O(n^3), where n is the
     * maximum among the number of workers and the number of jobs.
     *
     * @author Kevin L. Stern
     */
    private class HungarianAlgorithm {
        private final double[][] costMatrix;
        private final int rows, cols, dim;
        private final double[] labelByWorker, labelByJob;
        private final int[] minSlackWorkerByJob;
        private final double[] minSlackValueByJob;
        private final int[] matchJobByWorker, matchWorkerByJob;
        private final int[] parentWorkerByCommittedJob;
        private final boolean[] committedWorkers;

        /**
         * Construct an instance of the algorithm.
         *
         * @param costMatrix the cost matrix, where matrix[i][j] holds the cost of assigning
         *                   worker i to job j, for all i, j. The cost matrix must not be
         *                   irregular in the sense that all rows must be the same length.
         */
        public HungarianAlgorithm(double[][] costMatrix) {
            this.dim = Math.max(costMatrix.length, costMatrix[0].length);
            this.rows = costMatrix.length;
            this.cols = costMatrix[0].length;
            this.costMatrix = new double[this.dim][this.dim];
            for (int w = 0; w < this.dim; w++) {
                if (w < costMatrix.length) {
                    if (costMatrix[w].length != this.cols) {
                        throw new IllegalArgumentException("Irregular cost matrix");
                    }
                    this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
                } else {
                    this.costMatrix[w] = new double[this.dim];
                }
            }
            labelByWorker = new double[this.dim];
            labelByJob = new double[this.dim];
            minSlackWorkerByJob = new int[this.dim];
            minSlackValueByJob = new double[this.dim];
            committedWorkers = new boolean[this.dim];
            parentWorkerByCommittedJob = new int[this.dim];
            matchJobByWorker = new int[this.dim];
            Arrays.fill(matchJobByWorker, -1);
            matchWorkerByJob = new int[this.dim];
            Arrays.fill(matchWorkerByJob, -1);
        }

        /**
         * Compute an initial feasible solution by assigning zero labels to the
         * workers and by assigning to each job a label equal to the minimum cost
         * among its incident edges.
         */
        protected void computeInitialFeasibleSolution() {
            for (int j = 0; j < dim; j++) {
                labelByJob[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < labelByJob[j]) {
                        labelByJob[j] = costMatrix[w][j];
                    }
                }
            }
        }

        /**
         * Execute the algorithm.
         *
         * @return the minimum cost matching of workers to jobs based upon the
         * provided cost matrix. A matching value of -1 indicates that the
         * corresponding worker is unassigned.
         */
        public int[] execute() {
    /*
     * Heuristics to improve performance: Reduce rows and columns by their
     * smallest element, compute an initial non-zero dual feasible solution and
     * create a greedy matching from workers to jobs of the cost matrix.
     */
            reduce();
            computeInitialFeasibleSolution();
            greedyMatch();

            int w = fetchUnmatchedWorker();
            while (w < dim) {
                initializePhase(w);
                executePhase();
                w = fetchUnmatchedWorker();
            }
            int[] result = Arrays.copyOf(matchJobByWorker, rows);
            for (w = 0; w < result.length; w++) {
                if (result[w] >= cols) {
                    result[w] = -1;
                }
            }
            return result;
        }

        /**
         * Execute a single phase of the algorithm. A phase of the Hungarian algorithm
         * consists of building a set of committed workers and a set of committed jobs
         * from a root unmatched worker by following alternating unmatched/matched
         * zero-slack edges. If an unmatched job is encountered, then an augmenting
         * path has been found and the matching is grown. If the connected zero-slack
         * edges have been exhausted, the labels of committed workers are increased by
         * the minimum slack among committed workers and non-committed jobs to create
         * more zero-slack edges (the labels of committed jobs are simultaneously
         * decreased by the same amount in order to maintain a feasible labeling).
         * <p/>
         * <p/>
         * The runtime of a single phase of the algorithm is O(n^2), where n is the
         * dimension of the internal square cost matrix, since each edge is visited at
         * most once and since increasing the labeling is accomplished in time O(n) by
         * maintaining the minimum slack values among non-committed jobs. When a phase
         * completes, the matching will have increased in size.
         */
        protected void executePhase() {
            while (true) {
                int minSlackWorker = -1, minSlackJob = -1;
                double minSlackValue = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (parentWorkerByCommittedJob[j] == -1) {
                        if (minSlackValueByJob[j] < minSlackValue) {
                            minSlackValue = minSlackValueByJob[j];
                            minSlackWorker = minSlackWorkerByJob[j];
                            minSlackJob = j;
                        }
                    }
                }
                if (minSlackValue > 0) {
                    updateLabeling(minSlackValue);
                }
                parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
                if (matchWorkerByJob[minSlackJob] == -1) {
        /*
         * An augmenting path has been found.
         */
                    int committedJob = minSlackJob;
                    int parentWorker = parentWorkerByCommittedJob[committedJob];
                    while (true) {
                        int temp = matchJobByWorker[parentWorker];
                        match(parentWorker, committedJob);
                        committedJob = temp;
                        if (committedJob == -1) {
                            break;
                        }
                        parentWorker = parentWorkerByCommittedJob[committedJob];
                    }
                    return;
                } else {
        /*
         * Update slack values since we increased the size of the committed
         * workers set.
         */
                    int worker = matchWorkerByJob[minSlackJob];
                    committedWorkers[worker] = true;
                    for (int j = 0; j < dim; j++) {
                        if (parentWorkerByCommittedJob[j] == -1) {
                            double slack = costMatrix[worker][j] - labelByWorker[worker]
                                    - labelByJob[j];
                            if (minSlackValueByJob[j] > slack) {
                                minSlackValueByJob[j] = slack;
                                minSlackWorkerByJob[j] = worker;
                            }
                        }
                    }
                }
            }
        }

        /**
         * @return the first unmatched worker or {@link #dim} if none.
         */
        protected int fetchUnmatchedWorker() {
            int w;
            for (w = 0; w < dim; w++) {
                if (matchJobByWorker[w] == -1) {
                    break;
                }
            }
            return w;
        }

        /**
         * Find a valid matching by greedily selecting among zero-cost matchings. This
         * is a heuristic to jump-start the augmentation algorithm.
         */
        protected void greedyMatch() {
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
                            && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0) {
                        match(w, j);
                    }
                }
            }
        }

        /**
         * Initialize the next phase of the algorithm by clearing the committed
         * workers and jobs sets and by initializing the slack arrays to the values
         * corresponding to the specified root worker.
         *
         * @param w the worker at which to root the next phase.
         */
        protected void initializePhase(int w) {
            Arrays.fill(committedWorkers, false);
            Arrays.fill(parentWorkerByCommittedJob, -1);
            committedWorkers[w] = true;
            for (int j = 0; j < dim; j++) {
                minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                        - labelByJob[j];
                minSlackWorkerByJob[j] = w;
            }
        }

        /**
         * Helper method to record a matching between worker w and job j.
         */
        protected void match(int w, int j) {
            matchJobByWorker[w] = j;
            matchWorkerByJob[j] = w;
        }

        /**
         * Reduce the cost matrix by subtracting the smallest element of each row from
         * all elements of the row as well as the smallest element of each column from
         * all elements of the column. Note that an optimal assignment for a reduced
         * cost matrix is optimal for the original cost matrix.
         */
        protected void reduce() {
            for (int w = 0; w < dim; w++) {
                double min = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min) {
                        min = costMatrix[w][j];
                    }
                }
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min;
                }
            }
            double[] min = new double[dim];
            for (int j = 0; j < dim; j++) {
                min[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min[j]) {
                        min[j] = costMatrix[w][j];
                    }
                }
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min[j];
                }
            }
        }

        /**
         * Update labels with the specified slack by adding the slack value for
         * committed workers and by subtracting the slack value for committed jobs. In
         * addition, update the minimum slack values appropriately.
         */
        protected void updateLabeling(double slack) {
            for (int w = 0; w < dim; w++) {
                if (committedWorkers[w]) {
                    labelByWorker[w] += slack;
                }
            }
            for (int j = 0; j < dim; j++) {
                if (parentWorkerByCommittedJob[j] != -1) {
                    labelByJob[j] -= slack;
                } else {
                    minSlackValueByJob[j] -= slack;
                }
            }
        }
    }

}
