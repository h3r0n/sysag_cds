package com.sysag_cds.people;

import java.util.Random;

public class RandomIllness {

    Double prob;
    Random rand;

    public RandomIllness(Double illnessProb) {
        prob = illnessProb;
        rand = new Random();
    }

    Boolean getRandomIllness () {
        return rand.nextFloat() < prob;
    }
}

