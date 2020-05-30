package com.sysag_cds.business;

import com.sysag_cds.utility.Decree;
import com.sysag_cds.world.Building;
import com.sysag_cds.world.Location;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

public class Business extends Agent {
    /*enum Business{
        Negozio,
        Supermercato,
        Scuola,
        Fabbrica,
        Parco,
        Ospedale
    }*/

    Building position;
    boolean open;
    String category;

    protected void setup() {
        // inizializzazione agente
        Object[] args = this.getArguments();
        if (args != null) {
            // il primo argomento specifica la categoria
            System.out.println((String) args[0]);
            category = (String) args[0];

            // il secondo argomento specifica la posizione
            if (args.length >= 2) {
                position = new Building((String) args[1]);
            }
            // il terzo argomento specifica la densità
            if (args.length >= 3) {
                position.setDensity(Double.parseDouble((String)args[2]));
            }
        }

        registerService();
        subscribeDecrees();
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
        sd.setType(category);
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position));
        if (open)
            sd.addProperties(new Property("Open","True"));
        dfd.addServices(sd);

        return dfd;
    }

    /**
     * Notifica aggiornamenti alle normative
     */
    public void subscribeDecrees() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Government");
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            Iterator allProperties = sd.getAllProperties();
                            while (allProperties.hasNext()) {
                                Property p = (Property) allProperties.next();
                                if (p.getName().equals("Decree"))
                                    manageDecree((Decree) p.getValue());
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

    void manageDecree(Decree d) {
        setOpen(d.getDensity() < position.getDensity());
    }
}
