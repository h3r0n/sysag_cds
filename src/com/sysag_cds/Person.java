package com.sysag_cds;

import jade.core.Agent;
import jade.core.behaviours.*;

public class Person extends Agent {

    enum SEIR {
        susceptible,
        exposed,
        infectious,
        recovered
    }

    static int beta = 2;
    static int delta = 2;
    static int gamma = 2;

    SEIR disease = SEIR.susceptible;

    Location position;
    Location home;

    public boolean isAtHome() {
        return this.position == this.home;
    }

    protected void setup() {
        position = home;

        this.setExposed();
    }

    void setExposed() {
        disease = SEIR.exposed;

        addBehaviour(new WakerBehaviour(this, Simulation.tick*delta) {
            @Override
            protected void onWake() {
                System.out.println(myAgent.getLocalName()+" infectious");
                setInfectious();
            }
        });
    }

    void setInfectious() {
        disease = SEIR.infectious;

        addBehaviour(new WakerBehaviour(this, Simulation.tick*gamma) {
            @Override
            protected void onWake() {
                System.out.println(myAgent.getLocalName()+" recovered");
                setRecovered();
            }
        });
    }

    void setRecovered() {
        disease = SEIR.recovered;
    }

    void goToLocation(Location l) {
        position = l;

    }

    void sneeze() {
        
    }
}