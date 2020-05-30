package com.sysag_cds.world;

import com.google.common.base.Supplier;

public class RoadFactory implements Supplier<Road> {

    protected int counter = 0;

    @Override
    public Road get() {
        Road r = new Road("r"+counter++);
        r.density = .1;
        return r;
    }
}
