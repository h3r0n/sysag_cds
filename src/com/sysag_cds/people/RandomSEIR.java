package com.sysag_cds.people;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class RandomSEIR {

    private String[] diseaseStatus = new String[]{"SUSCEPTIBLE", "EXPOSED", "INFECTIOUS", "RECOVERED"};
    EnumeratedIntegerDistribution distribution;

    public RandomSEIR(double sp, double ep, double ip, double rp) {
        int[] dsIndex = new int[]{0, 1, 2, 3};
        double[] probabilities = new double[]{sp,ep,ip,rp};
        distribution = new EnumeratedIntegerDistribution(dsIndex, probabilities);
    }

    String getRandomStatus() {
        return diseaseStatus[distribution.sample()];
    }
}