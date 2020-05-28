package com.sysag_cds.map;

import jade.core.Agent;

public class BuildingAgent extends Agent {
    enum Business{
        Negozio,
        Supermercato,
        Scuola,
        Fabbrica,
        Parco,
        Ospedale
    }

    float distanceDPI= (float) 0.5; //ogni edificio ha un valore di DPI da rispettare o possiede una certa distanza di sicurezza che può essere convertita in DPI
    Location position;
    Building.Business bus;

    protected void setup(){

        //bus=
        //position=

    }
/*
    void subscribeBusinessService(Business business) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(String.valueOf(business));
        sd.addProperties(new Property("DPI", String.valueOf(DPI));
        //sd.addProperties(new Property("DPI", String.valueOf(DPI));
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            if (Simulation.debug) {
                                System.out.println("dfd name: " + dfd.getName());
                                System.out.println("services: " + Iterators.size(allServices));
                                System.out.println("sd type: " + sd.getType());
                                System.out.println("sd name:" + sd.getName());
                                System.out.println("Location: " + ((Property) sd.getAllProperties().next()).getValue());
                                System.out.println("Received by: " + getLocalName());
                            }
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };

        addBehaviour(subscription);
        subscriptions.add(subscription);

    }

    void unsubscribeAll() {
        if (Simulation.debug)
            System.out.println(getLocalName() + " unsubscribed");

        for (SubscriptionInitiator s : subscriptions) {
            s.cancel(getDefaultDF(), true);
        }

        subscriptions.clear();
    }
 */
}
