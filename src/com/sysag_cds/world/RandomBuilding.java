package com.sysag_cds.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBuilding {
    Double prob;
    Random rand;
    List<Building> buildings;

    public RandomBuilding() {
        buildings = new ArrayList<>(World.getInstance().getBuildings());
        rand = new Random();
    }

    public Building getRandomBuilding() {
        return buildings.get(rand.nextInt(buildings.size()));
    }

    public Building getRandomBuildingAndRemove() {
        int index = rand.nextInt(buildings.size());
        Building b = buildings.get(index);
        buildings.remove(b);
        return b;
    }
}
