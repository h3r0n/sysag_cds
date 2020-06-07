package com.sysag_cds.superagents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

/**
 * L'agente Statistics raccoglie i numeri relativi al contagio e li stampa periodicamente.
 */
public class Statistics extends Agent {

    private int beds = 0;
    private int dead = 0;
    private int recovered = 0;
    private int infected = 0;
    private int currentInfected = 0;
    private int exposed = 0;
    private static int updateTicks = 5;
    int time = 0;
    StatGui gui = new StatGui();

    @Override
    protected void setup() {
        subscribeHealthCare();
        addBehaviour(new manageStatsCounts());
        registerStatisticsService();
        addBehaviour(new TickerBehaviour(this, Simulation.tick * updateTicks) {
            @Override
            protected void onTick() {
                time+=updateTicks;
                if (Simulation.debug)
                    printStatistics();
                gui.addData((double) time/Simulation.day,infected,currentInfected,exposed,recovered,dead,beds);
            }
        });
    }

    void registerStatisticsService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Statistics");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    class manageStatsCounts extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                String query = msg.getContent();
                switch (query) {
                    case "Dead":
                        dead++;
                        break;
                    case "Infected":
                        infected++;
                        break;
                    case "Exposed":
                        exposed++;
                        break;
                    case "Recovered":
                        recovered++;
                        break;
                }
                currentInfected = infected - (recovered + dead);
            } else
                block();
        }
    }

    public void printStatistics() {
        System.out.println(infected+" total infected");
        System.out.println(currentInfected+" currently infected");
        System.out.println(exposed+" total exposed");
        System.out.println(recovered+" recovered");
        System.out.println(dead+" deaths");
    }

    public void subscribeHealthCare() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("HealthCare");
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" received a new update about Hospital beds");
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            Iterator allProperties = sd.getAllProperties();
                            while (allProperties.hasNext()) {
                                Property p = (Property) allProperties.next();
                                if (p.getName().equals("beds"))
                                    beds = Integer.parseInt((String)p.getValue());
                            }
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };
        addBehaviour(subscription);
    }
}
