package com.sysag_cds.world;

import com.google.common.base.Supplier;

import java.util.LinkedList;
import java.util.List;

/**
 * Classe factory che restituisce istanze di classe Building. Mantiene una lista delle istanze create.
 */
public class BuildingFactory implements Supplier<Building> {

    protected int count = 0;
    protected List<Building> list = new LinkedList<>();

    public BuildingFactory() {}

    @Override
    public Building get() {
        Building b = new Building("b"+count++);
        b.density = .5;
        list.add(b);
        return b;
    }

    List<Building> getList() {
        return list;
    }
}
