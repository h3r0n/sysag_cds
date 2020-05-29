package com.sysag_cds;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class Statistics extends Agent {

    private int dead = 0;
    private int recovered = 0;
    private int infected = 0;
    private int currentInfected = 0;
    private static int updateTicks = 10;

    @Override
    protected void setup() {
        addBehaviour(new manageStatsCounts());
        registerStatisticsService();
        addBehaviour(new TickerBehaviour(this, Simulation.tick * updateTicks) {
            @Override
            protected void onTick() {
                printStatistics();
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
        System.out.println(recovered+" recovered");
        System.out.println(dead+" deaths");
    }
}
