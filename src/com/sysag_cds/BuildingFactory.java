package com.sysag_cds;

import com.google.common.base.Supplier;
import java.util.List;

public class BuildingFactory implements Supplier<Building> {

    protected int count = 0;
    protected List<Building> list;

    BuildingFactory() {}

    BuildingFactory(List<Building> l) {
        list = l;
    }

    @Override
    public Building get() {
        Building b = new Building("b"+count++);
        if (list!=null)
            list.add(b);
        return b;
    }
}
