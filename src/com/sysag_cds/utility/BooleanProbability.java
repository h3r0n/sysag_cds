package com.sysag_cds.utility;

import java.util.stream.Stream;

/**
 * Bernoulli random variable.
 */
public class BooleanProbability {
    /**
     * Given a probability returns true or false
     *
     * @param probability the probability
     * @return the true or false string
     */
    public static String getTrueFalse(Double probability) {
        return Boolean.toString(getBoolean(probability));
    }

    /**
     * Given a Bernoulli probability distribution, it samples a random boolean value
     *
     * @param probability the probability
     * @return the boolean
     */
    public static boolean getBoolean(Double probability) {
        return Math.random() < probability;
    }
}
