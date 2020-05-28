package com.sysag_cds.business;

import com.sysag_cds.map.Location;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public abstract class Business extends Agent {
    /*enum Business{
        Negozio,
        Supermercato,
        Scuola,
        Fabbrica,
        Parco,
        Ospedale
    }*/

    Location position;
    double density;
    boolean open;


    protected void setup() {
        registerService();
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(category());
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position.toString()));
        sd.addProperties(new Property("Density", String.valueOf(density)));
        sd.addProperties(new Property("Open","True"));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    abstract String category();
}
