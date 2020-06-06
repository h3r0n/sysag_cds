package com.sysag_cds.world;

import com.sysag_cds.utility.Decree;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

/**
 * Agente Business in cui si recano gli agenti Person per svariati motivi. Gestisce la propria apertura/chiusura
 * in base ai decreti dell'agente Government.
 */
public class Business extends Agent {

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
                position = World.getInstance().findBuilding(new Building((String) args[1]));
            }
            // il terzo argomento specifica la densità
            if (args.length >= 3) {
                position.setDensity(Double.parseDouble((String)args[2]));
            }
        }
        System.out.println("business "+position.toString());
        open=true;
        registerService();
        subscribeDecrees();
    }

    public void setOpen(boolean open) {
        this.open = open;
        updateService();
        //System.out.println("Open= "+open+" "+"Park= "+category+park+" "+"Open2= "+this.open+" ");
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
        sd.addProperties(new Property("Open",Boolean.toString(open)));
        sd.addProperties(new Property("Location", position.getLocation()));
        sd.addProperties(new Property("Density",position.density.toString()));
        dfd.addServices(sd);

        return dfd;
    }

    /**
     * Notifica aggiornamenti alle normative
     */
    public void subscribeDecrees() {
        Decree currentDecree= new Decree();
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Government");
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    System.out.println(getLocalName()+" received a new decree");
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            Iterator allProperties = sd.getAllProperties();
                            while (allProperties.hasNext()) {
                                Property p = (Property) allProperties.next();
                                if (p.getName().equals("decreeNumber"))
                                    currentDecree.setDecreeNumber( Integer.parseInt((String)p.getValue()));
                                if (p.getName().equals("walkDistance"))
                                    currentDecree.setWalkDistance( Integer.parseInt((String)p.getValue()));
                                if (p.getName().equals("maxTravel"))
                                    currentDecree.setMaxTravel( Integer.parseInt((String)p.getValue()));
                                if (p.getName().equals("maskRequired"))
                                    currentDecree.setMaskRequired( currentDecree.parseString((String)p.getValue()));
                                if (p.getName().equals("density"))
                                    currentDecree.setDensity( Double.parseDouble((String)p.getValue()));
                                if (p.getName().equals("parkOpen"))
                                    currentDecree.setParkOpen( Boolean.parseBoolean((String)p.getValue()));
                                if (p.getName().equals("nonEssentialOpen"))
                                    currentDecree.setNonEssentialOpen(Boolean.parseBoolean((String)p.getValue()));

                            }
                            manageDecree(currentDecree);
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
        boolean open = d.getDensity() > position.getDensity();
        if (category.equals("Park") && !d.getParkOpen())
            open = false;
        if (category.equals("nonEssential") && !d.getNonEssentialOpen())
            open = false;
        setOpen(open);
    }
}
