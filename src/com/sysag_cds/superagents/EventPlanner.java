package com.sysag_cds.superagents;

import com.sysag_cds.utility.Decree;
import com.sysag_cds.world.Building;
import com.sysag_cds.world.RandomBuilding;
import com.sysag_cds.world.World;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import java.util.LinkedList;
import java.util.List;

public class EventPlanner extends Agent {

    static int eventTick = 3*Simulation.day;
    boolean firstEvent = true;
    boolean allowed = true;

    @Override
    protected void setup() {

        subscribeDecrees();
        addBehaviour(new TickerBehaviour(this, eventTick * Simulation.tick) {
            @Override
            protected void onTick() {
                Building setting = findSetting();
                if (setting!=null && allowed) {
                    if (firstEvent) {
                        firstEvent = false;
                        registerService(setting);
                    } else {
                        updateService(setting);
                    }
                }
            }
        });
    }

    private DFAgentDescription createService(Building setting) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Event");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", setting.getLocation()));
        sd.addProperties(new Property("Density", setting.getDensity().toString()));
        dfd.addServices(sd);

        return dfd;
    }

    private void registerService(Building setting) {
        try {
            DFService.register(this, createService(setting));
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void updateService(Building setting) {
        try {
            DFService.modify(this, createService(setting));
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected Building findSetting() {

        List<Building> results = new LinkedList<>();
        Building setting = null;

        // search template
        DFAgentDescription dfdt = new DFAgentDescription();
        ServiceDescription sdt = new ServiceDescription();
        sdt.setType("Park");
        sdt.addProperties(new Property("Open",true));
        dfdt.addServices(sdt);

        // search
        DFAgentDescription[] dfds = new DFAgentDescription[0];
        try {
            dfds = DFService.search(this, dfdt);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // get results
        for (DFAgentDescription dfd : dfds) {
            Iterator allServices = dfd.getAllServices();
            while (allServices.hasNext()) {
                ServiceDescription sd = (ServiceDescription) allServices.next();
                Iterator allProperties = sd.getAllProperties();
                Building buil=new Building("test");
                while (allProperties.hasNext()) {
                    Property p = (Property) allProperties.next();
                    if (p.getName().equals("Location"))
                        buil.setLocation((String) p.getValue());
                    if (p.getName().equals("Density"))
                        buil.setDensity(Double.parseDouble((String) p.getValue()));
                }
                results.add(buil);
            }
        }
        // pick a random one
        if (results.size()>0)
            setting = new RandomBuilding(results).getRandomBuilding();

        return setting;
    }

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
                                if (p.getName().equals("eventOpen"))
                                    currentDecree.setEventOpen(Boolean.parseBoolean((String)p.getValue()));

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
        allowed = d.getEventOpen();
    }
}
