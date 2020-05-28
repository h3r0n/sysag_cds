package com.sysag_cds.people;

import com.sysag_cds.Simulation;
import com.sysag_cds.map.Location;
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
        scheduleTask(new WalkingTask(this,workingPlace.toString()));
        scheduleTask(new WaitingTask(this,Simulation.tick*workTicks));
        scheduleTask(new WalkingTask(this,home.toString()));
    }

}
