package com.sysag_cds;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

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
