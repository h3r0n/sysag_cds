package com.sysag_cds.superagents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * L'agente Statistics raccoglie i numeri relativi al contagio e li stampa periodicamente.
 */
public class Statistics extends GuiAgent {

    private int dead = 0;
    private int recovered = 0;
    private int infected = 0;
    private int currentInfected = 0;
    private static int updateTicks = 5;
    int time = 0;

    @Override
    protected void setup() {
        StatGui gui = new StatGui();
        addBehaviour(new manageStatsCounts());
        registerStatisticsService();
        addBehaviour(new TickerBehaviour(this, Simulation.tick * updateTicks) {
            @Override
            protected void onTick() {
                printStatistics();
                gui.addData(time+=updateTicks,infected);
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

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {}

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
