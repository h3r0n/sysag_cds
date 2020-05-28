package com.sysag_cds.people;

import java.util.Random;

public class NaughtyProbability {

    Double prob;
    Random rand;

    public NaughtyProbability(Double naughtyProb) {
        prob = naughtyProb;
        rand = new Random();
    }

    String getRandomNaughty () {
        return (rand.nextFloat() < prob) ? "True" : "False";
    }
}
