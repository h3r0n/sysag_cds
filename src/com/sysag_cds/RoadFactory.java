package com.sysag_cds;

import com.google.common.base.Supplier;

public class RoadFactory implements Supplier {

    protected int counter = 0;

    @Override
    public Object get() {
        return new Road("r"+counter++);
    }
}
