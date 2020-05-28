package com.sysag_cds.map;

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

    public String getRandomBuilding () {
        return buildings.get(rand.nextInt(buildings.size())).toString();
    }
}
