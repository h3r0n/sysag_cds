package com.sysag_cds.people;

import com.sysag_cds.Simulation;
import com.sysag_cds.world.Location;

public class Nurse extends Worker {

    Location hospital;

    protected void setup() {
        super.setup();
    }

    @Override
    void work(){
        float DPIbefore=this.DPI;
        scheduleTask(new WalkingTask(this, workingPlace));
        this.DPI= (float) 0.95;
        scheduleTask(new WaitingTask(this,Simulation.tick*workTicks));
        this.DPI= DPIbefore;
        scheduleTask(new WalkingTask(this,home));
    }

}
