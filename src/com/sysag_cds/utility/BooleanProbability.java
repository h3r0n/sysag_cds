package com.sysag_cds.utility;

import java.util.stream.Stream;

/**
 * Bernoulli random variable.
 */
public class BooleanProbability {
    public static String getTrueFalse(Double probability) {
        return Boolean.toString(getBoolean(probability));
    }

    public static boolean getBoolean(Double probability) {
        return Math.random() < probability;
    }
}
