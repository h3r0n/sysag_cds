package com.sysag_cds.superagents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class HealthCare extends Agent {
    int beds;

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        beds = Integer.parseInt((String)args[0]);

        registerService();
        addBehaviour(new ManageBeds());
    }

    class ManageBeds extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                String query = msg.getContent();
                if (Boolean.parseBoolean(query)) {
                    beds--;
                } else {
                    beds++;
                }
                updateService();
            } else
                block();
        }
    }

    private void registerService() {
        try {
            DFService.register(this, createService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void updateService() {
        try {
            DFService.modify(this, createService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private DFAgentDescription createService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("HealthCare");
        sd.setName(getLocalName());
        sd.addProperties(new Property("beds", beds));
        dfd.addServices(sd);

        return dfd;
    }
}
