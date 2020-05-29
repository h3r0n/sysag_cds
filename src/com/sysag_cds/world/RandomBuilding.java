package com.sysag_cds.world;

import java.util.List;
import java.util.Random;

public class RandomBuilding {
    Double prob;
    Random rand;
    List<Building> buildings;

    public RandomBuilding(World w) {
        buildings = w.getBuildings();
        rand = new Random();
    }

    public Building getRandomBuilding () {
        return buildings.get(rand.nextInt(buildings.size()));
    }
}
