package com.sysag_cds;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class BuildingAgent extends Agent {
    enum Business{
        Negozio,
        Supermercato,
        Scuola,
        Fabbrica
    }

    Location position;
    Business bus;

    protected void setup(){

        //bus=
        //position=

    }

    void registerBusinessService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(String.valueOf(bus));
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

    void deregisterBusinessService() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
