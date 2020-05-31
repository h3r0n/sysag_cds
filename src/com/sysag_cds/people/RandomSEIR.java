package com.sysag_cds.people;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.stream.IntStream;

/**
 * Restituisce uno stato casuale della malattia in base ad una distribuzione di probabilità specificata.
 * Implementa il modello SEIR.
 */
public class RandomSEIR {

    private String[] diseaseStatus = new String[]{"SUSCEPTIBLE", "EXPOSED", "INFECTIOUS", "RECOVERED"};
    EnumeratedIntegerDistribution distribution;

    public RandomSEIR(double sp, double ep, double ip, double rp) {
        int[] dsIndex = IntStream.range(0,diseaseStatus.length).toArray();
        double[] probabilities = new double[]{sp,ep,ip,rp};
        distribution = new EnumeratedIntegerDistribution(dsIndex, probabilities);
    }

    String getRandomStatus() {
        return diseaseStatus[distribution.sample()];
    }
}