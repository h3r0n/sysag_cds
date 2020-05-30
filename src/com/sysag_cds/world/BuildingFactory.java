package com.sysag_cds.world;

import com.google.common.base.Supplier;
import java.util.List;

public class BuildingFactory implements Supplier<Building> {

    protected int count = 0;
    protected List<Building> list;

    BuildingFactory() {}

    public BuildingFactory(List<Building> l) {
        list = l;
    }

    @Override
    public Building get() {
        Building b = new Building("b"+count++);
        b.density = .5;
        if (list!=null)
            list.add(b);
        return b;
    }
}
