package com.sysag_cds.world;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.stream.IntStream;

/**
 * Restituisce una categoria casuale di Business in base ad una distribuzione di probabilità specificata.
 */
public class RandomBusiness {
    private final String[] categoryStrings = new String[]{"SuperMarket", "Hospital", "Park"};
    EnumeratedIntegerDistribution distribution;

    public RandomBusiness(double[] p) {
        int[] catIndex = IntStream.range(0,categoryStrings.length).toArray();
        distribution = new EnumeratedIntegerDistribution(catIndex, p);
    }

    String getRandomCategory() {
        return categoryStrings[distribution.sample()];
    }
}
