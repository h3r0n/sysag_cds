package com.sysag_cds.people;

import com.sysag_cds.Simulation;
import com.sysag_cds.map.Location;

public class Nurse extends Worker {

    Location hospital;

    protected void setup() {
        super.setup();
    }

    @Override
    void work(){
        float DPIbefore=this.DPI;
        scheduleTask(new WalkingTask(workingPlace.toString()));
        this.DPI= (float) 0.95;
        scheduleTask(new WaitingTask(Simulation.tick*workTicks));
        this.DPI= DPIbefore;
        scheduleTask(new WalkingTask(home.toString()));
    }

}
