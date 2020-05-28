package com.sysag_cds.map;

import com.google.common.base.Supplier;

public class RoadFactory implements Supplier<Road> {

    protected int counter = 0;

    @Override
    public Road get() {
        return new Road("r"+counter++);
    }
}
