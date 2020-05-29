package com.sysag_cds.business;

import com.sysag_cds.world.Location;
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

    public void setOpen(boolean open) {
        this.open = open;
        updateService();
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
        sd.setType(category());
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position.toString()));
        sd.addProperties(new Property("Density", density));
        if (open)
            sd.addProperties(new Property("Open","True"));
        dfd.addServices(sd);

        return dfd;
    }

    protected String category() {
        return "Business";
    }
}
