package com.sysag_cds;

import com.google.common.collect.Iterators;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class Person extends Agent {

    enum SEIR {
        SUSCEPTIBLE,
        EXPOSED,
        INFECTIOUS,
        RECOVERED
    }

    static int delta = 2;
    static int gamma = 2;

    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;

    Location position;
    Location home;

    protected void setup() {

        if (Simulation.debug)
            System.out.println("Agent "+getLocalName()+" started");

        Object[] args=this.getArguments();

        if(args!=null && args[0].equals("infectious"))
            setInfectious();
        else
            setSusceptible();

        goToLocation(new Location("b"));
    }

    void setSusceptible() {
        diseaseStatus = SEIR.SUSCEPTIBLE;
        if (Simulation.debug)
            System.out.println(getLocalName() + " is susceptible");
    }

    boolean isSusceptible() {
        return diseaseStatus == SEIR.SUSCEPTIBLE;
    }

    void setExposed() {
        diseaseStatus = SEIR.EXPOSED;

        if (Simulation.debug)
            System.out.println(getLocalName() + " has been exposed");

        addBehaviour(new WakerBehaviour(this, Simulation.tick * delta) {
            @Override
            protected void onWake() {
                setInfectious();
            }
        });
    }

    boolean isExposed() {
        return diseaseStatus == SEIR.EXPOSED;
    }

    void setInfectious() {
        diseaseStatus = SEIR.INFECTIOUS;

        if (Simulation.debug)
            System.out.println(getLocalName() + " is infectious");

        addBehaviour(new WakerBehaviour(this, Simulation.tick * gamma) {
            @Override
            protected void onWake() {
                setRecovered();
            }
        });
    }

    boolean isInfectious() {
        return diseaseStatus == SEIR.INFECTIOUS;
    }

    void setRecovered() {
        diseaseStatus = SEIR.RECOVERED;

        if (Simulation.debug)
            System.out.println(getLocalName() + " has recovered");
    }

    boolean isRecovered() {
        return diseaseStatus == SEIR.RECOVERED;
    }

    void goToLocation(Location l) {
        position = l;

        if (isInfectious())
            registerContagionService();

        if (isSusceptible())
            subscribeContagionService();
    }

    void registerContagionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void subscribeContagionService() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.addProperties(new Property("Location", position.toString()));
        template.addServices(sd);
        addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        ServiceDescription sd = (ServiceDescription) dfd.getAllServices().next();
                        if (Simulation.debug) {
                            System.out.println("dfd name: "+dfd.getName());
                            System.out.println("services: "+ Iterators.size(dfd.getAllServices()));
                            System.out.println("sd type: "+sd.getType());
                            System.out.println("sd name:"+sd.getName());
                            System.out.println("Location: "+((Property)sd.getAllProperties().next()).getValue());
                        }
                        //chiamare la funzione di calcolo del contagio
                        setExposed();
                    }
                }
                catch (FIPAException fe) {fe.printStackTrace(); }
            }
        });
    }

}
