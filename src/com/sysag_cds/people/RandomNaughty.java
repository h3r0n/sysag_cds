package com.sysag_cds.people;

import java.util.Random;

public class RandomNaughty {

    Double prob;
    Random rand;

    public RandomNaughty(Double naughtyProb) {
        prob = naughtyProb;
        rand = new Random();
    }

    String getRandomNaughty () {
        return (rand.nextFloat() < prob) ? "True" : "False";
    }
}
