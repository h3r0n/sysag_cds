package com.sysag_cds;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class Student extends TaskAgent {

    Location school;

    static int beta= 10000;     //intervallo di tempo per andare a scuola

    protected void setup() {
        super.setup();
        addBehaviour(new TickerBehaviour(this, beta ) {
            protected void onTick() {
                Student.super.todo.add(new goToSchool(school)); //?
            }
        } );
    }

    public class goToSchool extends Task{

        Location l;

        goToSchool(Location s){
            this.l.location=s.location;
        }

        @Override
        public void action() {

        }

        @Override
        public boolean done() {
            return true;
        }
    }

}
