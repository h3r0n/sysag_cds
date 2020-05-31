package com.sysag_cds.superagents;

import com.sysag_cds.utility.Decree;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 * L'agente Government può emanare decreti riguardo il distanziamento sociale, la chiusura di Business
 * e l'obbligo di indossare DPI.
 */
public class Government extends Agent {

    Decree currentDecree = new Decree();

    @Override
    protected void setup() {
        registerService();
    }

    public void updateDecree(Decree newDecree) {
        currentDecree = newDecree;
        updateService();
    }

    private DFAgentDescription createService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Government");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Decree", currentDecree));
        dfd.addServices(sd);

        return dfd;
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
}
