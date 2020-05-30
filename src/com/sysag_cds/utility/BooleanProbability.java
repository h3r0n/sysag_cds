package com.sysag_cds.utility;

public class BooleanProbability {
    public static String getTrueFalse(Double probability) {
        return (Math.random() < probability) ? "True" : "False";
    }

    public static boolean getBoolean(Double probability) {
        return Math.random() < probability;
    }
}
