package com.sysag_cds;

import jade.core.behaviours.TickerBehaviour;

public class Worker extends Person {

    Location workingPlace;
    int workTicks=10;
    int workInterval=100;

    protected void setup() {
        super.setup();
        addBehaviour(new TickerBehaviour(this, Simulation.tick *workInterval ) {
            protected void onTick() {
                work();
            }
        } );

    }

    void work(){
        scheduleTask(new WalkingTask(workingPlace.toString()));
        scheduleTask(new WaitingTask(Simulation.tick*workTicks));
        scheduleTask(new WalkingTask(home.toString()));
    }

}
