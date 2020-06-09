package com.sysag_cds.world;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.stream.IntStream;

/**
 * Returns a random Business category given the specified probability distribution.
 */
public class RandomBusiness {
    private final String[] categoryStrings = new String[]{"SuperMarket", "Hospital", "Park", "Essential", "NonEssential"};
    EnumeratedIntegerDistribution distribution;

    /**
     * Instantiates a new Random business.
     *
     * @param p the probability
     */
    public RandomBusiness(double[] p) {
        int[] catIndex = IntStream.range(0,categoryStrings.length).toArray();
        distribution = new EnumeratedIntegerDistribution(catIndex, p);
    }

    /**
     * Gets random category.
     *
     * @return the random category
     */
    String getRandomCategory() {
        return categoryStrings[distribution.sample()];
    }
}
