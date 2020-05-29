package com.sysag_cds.people;

import java.util.Random;

public class RandomDeath {

    Double prob;
    Random rand;

    public RandomDeath(Double deathProb) {
        prob = deathProb;
        rand = new Random();
    }

    Boolean getRandomDeath () {
        return rand.nextFloat() < prob;
    }
}
