package com.sysag_cds.world;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.stream.IntStream;

public class RandomBusiness {
    private String[] categoryStrings = new String[]{"SuperMarket", "Park", "Business"}; // todo
    EnumeratedIntegerDistribution distribution;

    public RandomBusiness(double[] p) {
        int[] catIndex = IntStream.range(0,categoryStrings.length).toArray();
        distribution = new EnumeratedIntegerDistribution(catIndex, p);
    }

    String getRandomCategory() {
        return categoryStrings[distribution.sample()];
    }
}
