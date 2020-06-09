package com.sysag_cds.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Returns a random Building from the World Singleton or from a Building List with or without removing it
 */
public class RandomBuilding {
    Random rand;
    List<Building> buildings;

    /**
     * Instantiates a new Random building.
     */
    public RandomBuilding() {
        buildings = new ArrayList<>(World.getInstance().getBuildings());
        rand = new Random();
    }

    /**
     * Instantiates a new Random building.
     *
     * @param list the list of Buildings
     */
    public RandomBuilding(List<Building> list) {
        buildings = new LinkedList<>(list);
        rand = new Random();
    }

    /**
     * Gets random building.
     *
     * @return the random building
     */
    public Building getRandomBuilding() {
        return buildings.get(rand.nextInt(buildings.size()));
    }

    /**
     * Gets random building and remove it from the list.
     *
     * @return the random building
     */
    public Building getRandomBuildingAndRemove() {
        int index = rand.nextInt(buildings.size());
        Building b = buildings.get(index);
        buildings.remove(b);
        return b;
    }
}
