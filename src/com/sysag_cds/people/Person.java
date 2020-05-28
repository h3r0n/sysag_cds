package com.sysag_cds.people;

import com.google.common.collect.Iterators;
import com.sysag_cds.Simulation;
import com.sysag_cds.behaviour.DelayBehaviour;
import com.sysag_cds.behaviour.TaskAgent;
import com.sysag_cds.map.Building;
import com.sysag_cds.map.Location;
import com.sysag_cds.map.Road;
import com.sysag_cds.map.World;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class Person extends TaskAgent {

    public enum SEIR {
        SUSCEPTIBLE,
        EXPOSED,
        INFECTIOUS,
        RECOVERED
    }

    // costanti
    static int seirDelta = 2;   // tempo di incubazione (da EXPOSED a INFECTIOUS)
    static int seirGamma = 100;   // tempo di guarigione (da INFECTIOUS a RECOVERED)
    static int walkingTime = 1; // tempo per percorrere una strada
    static int deltaResources = 10;   // tempo di aggiornamento risorse
    static int maxfood = 10;    // dimensione riserva beni di prima necessità
    static int supermarketTicks = 10;
    static int hospitalTicks = 10;
    static int randomWalkTicks = 10;
    static int businessTicks = 10;
    static int parkTicks = 10;

    // status
    int food = maxfood;  // riserva beni di prima necessità
    boolean dead = false;
    boolean ill = false;
    boolean naughty = false; // mancato rispetto dei decreti
    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;    // stato di avanzamento della malattia
    float DPI = 0; //valore di DPI impostato inizialmente a zero
    Location home = new Building("testHome");      // residenza
    Location position = home;  // posizione corrente
    List<SubscriptionInitiator> subscriptions = new LinkedList<>(); // lista sottoscrizioni (potenziali contagi)
    //AID pathFinding;    // agente fornitore del servizio pathfinding

    protected void setup() {

        // inizializzazione agente
        Object[] args = this.getArguments();
        if (args != null) {
            // il primo argomento specifica lo stato della malattia:
            System.out.println((String) args[0]);
            switch ((String) args[0]) {
                case "SUSCEPTIBLE":
                    setSusceptible();
                    break;
                case "EXPOSED":
                    setExposed();
                    break;
                case "INFECTIOUS":
                    setInfectious();
                    break;
                case "RECOVERED":
                    setRecovered();
                    break;
            }
            // il secondo argomento specifica la casa
            if (args.length >= 2) {
                home = new Location((String) args[1]);
                position = home;
            }
            // il terzo argomento specifica il rispetto dei decreti
            if (args.length >= 3 && args[2].equals("True")) {
                naughty = true;
            }
        }

        /*

        // ricerca agente fornitore del servizio pathfinding
        try {
            updatePathFinding();
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // aggiornamento risorse
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaResources) {
            protected void onTick() {
                food--;
                if (food < 0)
                    goToSupermarket();
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * randomWalkTicks) {
            protected void onTick() {
               System.out.println("Sto aggiungendo un nuovo percorso per l'agente :" + this.myAgent.getLocalName());
               scheduleRandomWalk();
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * hospitalTicks) {
            protected void onTick() {
                if(IllProbability()) {
                     goToHospital();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * businessTicks) {
            protected void onTick() {
                goToBusiness();
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * parkTicks) {
            protected void onTick() {
                goToPark();
            }
        });

        addBehaviour(new manageDecreeDisposition());

         */

        if (Simulation.debug)
            System.out.println("Agent " + getLocalName() + " started");
    }

    // ------------------------------------
    //  Modello SEIR
    // ------------------------------------

    void setSusceptible() {
        diseaseStatus = SEIR.SUSCEPTIBLE;

        if (Simulation.debug)
            System.out.println(getLocalName() + " is susceptible");
    }

    boolean isSusceptible() {
        return diseaseStatus == SEIR.SUSCEPTIBLE;
    }

    void setExposed() {

        if (diseaseStatus == SEIR.SUSCEPTIBLE) {

            unsubscribeAll();
            diseaseStatus = SEIR.EXPOSED;

            if (Simulation.debug)
                System.out.println(getLocalName() + " has been exposed");

            addBehaviour(new WakerBehaviour(this, Simulation.tick * seirDelta) {
                @Override
                protected void onWake() {
                    setInfectious();
                }
            });
        }
    }

    boolean isExposed() {
        return diseaseStatus == SEIR.EXPOSED;
    }

    void setInfectious() {
        diseaseStatus = SEIR.INFECTIOUS;
        //sendStatsDeclaration("Infected"); //Manda messaggio ad Agente Statista
        registerContagionService();

        if (Simulation.debug)
            System.out.println(getLocalName() + " is infectious");

        addBehaviour(new WakerBehaviour(this, Simulation.tick * seirGamma) {
            @Override
            protected void onWake() {
                setRecovered();
            }
        });
    }

    boolean isInfectious() {
        return diseaseStatus == SEIR.INFECTIOUS;
    }

    void setRecovered() {

        if (diseaseStatus == SEIR.INFECTIOUS)
            deregisterContagionService();

        diseaseStatus = SEIR.RECOVERED;

        if (Simulation.debug)
            System.out.println(getLocalName() + " has recovered");

        /*
        if(DeathProbability()){
            sendStatsDeclaration("Death"); //Manda messaggio ad Agente Statista
            this.doDelete();
        }else{
            sendStatsDeclaration("Recovered"); //Manda messaggio ad Agente Statista
        }
        */
    }
/*
    boolean DeathProbability() {
        return ThreadLocalRandom.current().nextFloat()<0.1;
    }

    boolean IllProbability() {
        return ThreadLocalRandom.current().nextFloat()<0.1;
    }

    boolean isRecovered() {
        return diseaseStatus == SEIR.RECOVERED;
    }
*/
    // ------------------------------------
    //  Spostamenti
    // ------------------------------------

    void setLocation(Location l) {

        System.out.println("L'agente " + getLocalName() + " si sposta in " + l.toString());

        if (isSusceptible())
            unsubscribeAll();

        position = l;

        if (isInfectious())
            updateContagionService();

        if (isSusceptible())
            subscribeContagionService();
    }

    // crea un servizio contagion relativo a una location
    void registerContagionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
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

    void updateContagionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.modify(this, getDefaultDF(), dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void deregisterContagionService() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // notifica se esistono possibilità di contagio nel luogo corrente
    void subscribeContagionService() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.addProperties(new Property("Location", position.toString()));
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            if (Simulation.debug) {
                                System.out.println("dfd name: " + dfd.getName());
                                System.out.println("services: " + Iterators.size(allServices));
                                System.out.println("sd type: " + sd.getType());
                                System.out.println("sd name:" + sd.getName());
                                System.out.println("Location: " + ((Property) sd.getAllProperties().next()).getValue());
                                System.out.println("Received by: " + getLocalName());
                            }
                            //chiamare la funzione di calcolo del contagio
                            setExposed();
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };

        addBehaviour(subscription);
        subscriptions.add(subscription);

    }

    void unsubscribeAll() {
        if (Simulation.debug)
            System.out.println(getLocalName() + " unsubscribed");

        for (SubscriptionInitiator s : subscriptions) {
            s.cancel(getDefaultDF(), true);
        }

        subscriptions.clear();
    }
/*
    void updatePathFinding() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("PathFinding");
        dfd.addServices(sd);

        DFAgentDescription[] result = DFService.search(this, dfd);
        if(result.length!=0) {
            pathFinding = result[0].getName();
        }else{
            //inserire azioni o messaggi alternativi
        }
    }
*/
    // ------------------------------------
    //  Task
    // ------------------------------------

    class WalkingTask extends SequentialBehaviour {

        public WalkingTask(Agent a, String destination) {
            super(a);

            addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    String path = World.getInstance().getPath(position.toString(), destination);
                    String[] roads = path.split(",");

                    for (String r : roads) {
                        addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                            @Override
                            protected void onWake() {
                                setLocation(new Location(r));
                            }
                        });
                    }
                    addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                        @Override
                        protected void onWake() {
                            setLocation(new Location(destination));
                        }
                    });
                }
            });
        }
    }

    class WaitingTask extends DelayBehaviour {

        boolean wait = true;
        int ticks;

        public WaitingTask(Agent a, int ticks) {
            super(a, Simulation.tick * ticks);
        }
    }

    public void scheduleWalkHome() {
        scheduleTask(new WalkingTask(this,home.toString()));
    }
    /*
    public void scheduleRandomWalk() {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 5);
        System.out.println(randomNum);
        switch (randomNum){
            case 1:
                scheduleTask(new WalkingTask("b0"));
                break;
            case 2:
                scheduleTask(new WalkingTask("b1"));
                break;
            case 3:
                scheduleTask(new WalkingTask("b2"));
                break;
            case 4:
                scheduleTask(new WalkingTask("b3"));
                break;
            default:
                scheduleTask(new WalkingTask(home.toString()));
                break;
        }
    }

    void goToHospital() {

        Location hospital=new Location("test");

        DFAgentDescription template= new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Ospedale");
        template.addServices(sd);
        List<Location> hospitals = new ArrayList<>();
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i=0; i<hospitals.size(); i++){
                Iterator iter=result[i].getAllServices();
                Property p=(Property) iter.next();
                if(p.getName().equals("Stato")){
                    p=(Property) iter.next();
                }
                hospitals.add(new Location((String) p.getValue()));
            }

        }
        catch(FIPAException fe){
            fe.printStackTrace();
        }
        hospital=findNearestLocation(position,hospitals);
        scheduleTask(new WalkingTask(hospital.toString()));
        scheduleTask(new WaitingTask(hospitalTicks*Simulation.tick));
    }

    Location findNearestLocation(Location position, List<Location> hospitals) {
        //da definire
        return position;
    }

    void goToSupermarket(){
        Location supermarket = new Location("test");

        DFAgentDescription template= new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Supermercato");
        template.addServices(sd);
        List<Location> supermarkets = new ArrayList<>();
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i=0; i<supermarkets.size(); i++){
                Iterator iter=result[i].getAllServices();
                Property p=(Property) iter.next();
                if(p.getName().equals("Stato")){
                    p=(Property) iter.next();
                }
                supermarkets.add(new Location((String) p.getValue()));
            }

        }
        catch(FIPAException fe){
            fe.printStackTrace();
        }
        supermarket=findNearestLocation(position,supermarkets);
        scheduleTask(new WalkingTask(supermarket.toString()));
        scheduleTask(new WaitingTask(supermarketTicks*Simulation.tick));
    }

    void goToBusiness(){
        Location business= new Location("test");
        boolean closed=false;

        DFAgentDescription template= new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Negozio");
        template.addServices(sd);
        List<Location> businesses = new ArrayList<>();
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i=0; i<businesses.size(); i++){
                Iterator iter=result[i].getAllServices();
                Property p=(Property) iter.next();
                if(p.getName().equals("Stato")){
                    if(p.getValue().equals("Chiuso")){
                        if(naughty){
                            businesses.add(new Location((String) p.getValue()));
                        }
                    }else{
                        businesses.add(new Location((String) p.getValue()));
                    }
                    p=(Property) iter.next();
                }
            }

        }
        catch(FIPAException fe){
            fe.printStackTrace();
        }
        business=findNearestLocation(position,businesses);

        scheduleTask(new WalkingTask(business.toString()));
        scheduleTask(new WaitingTask(businessTicks*Simulation.tick));


    }

    void goToPark(){
        Location park= new Location("test");

        DFAgentDescription template= new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Parco");
        template.addServices(sd);
        List<Location> parks = new ArrayList<>();
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i=0; i<parks.size(); i++){
                Iterator iter=result[i].getAllServices();
                Property p=(Property) iter.next();
                if(p.getName().equals("Stato")){
                    if(p.getValue().equals("Chiuso")){
                        if(naughty){
                            parks.add(new Location((String) p.getValue()));
                        }
                    }else{
                        parks.add(new Location((String) p.getValue()));
                    }
                    p=(Property) iter.next();
                }
            }

        }
        catch(FIPAException fe){
            fe.printStackTrace();
        }
        park=findNearestLocation(position,parks);

        scheduleTask(new WalkingTask(park.toString()));
        scheduleTask(new WaitingTask(parkTicks*Simulation.tick));

    }

    void sendStatsDeclaration(String s){
        ACLMessage msg = new ACLMessage(statsInform);
        msg.addReceiver(pathFinding);
        msg.setContent(s);
        send(msg);
    }

    class manageDecreeDisposition extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate MT1=MessageTemplate.MatchPerformative(decreeInform);
            ACLMessage msg = myAgent.receive(MT1);

            if (msg != null) {
                String query = msg.getContent();
                DPI= Float.parseFloat(query);
            } else
                block();
        }
    }

     */
}
