package com.sysag_cds.people;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.stream.IntStream;

/**
 * Returns a random status of the disease given a specified probability distribution.
 *
 */
public class RandomSEIR {

    private String[] diseaseStatus = new String[]{"SUSCEPTIBLE", "EXPOSED", "INFECTIOUS", "RECOVERED"};
    EnumeratedIntegerDistribution distribution;

    /**
     * Instantiates a new Random seir distribution.
     *
     * @param sp the susceptible probability sp
     * @param ep the exposed probability ep
     * @param ip the infectious probability ip
     * @param rp the recovered probability rp
     */
    public RandomSEIR(double sp, double ep, double ip, double rp) {
        int[] dsIndex = IntStream.range(0,diseaseStatus.length).toArray();
        double[] probabilities = new double[]{sp,ep,ip,rp};
        distribution = new EnumeratedIntegerDistribution(dsIndex, probabilities);
    }

    /**
     * Gets random status.
     *
     * @return the random status
     */
    String getRandomStatus() {
        return diseaseStatus[distribution.sample()];
    }
}