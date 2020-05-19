package com.sysag_cds;

import com.google.common.collect.Iterators;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
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
import java.util.concurrent.ThreadLocalRandom;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Person extends TaskAgent {

    enum SEIR {
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

    // status
    int food = maxfood;  // riserva beni di prima necessità
    boolean dead = false;
    boolean ill = false;
    boolean naughty= false; // va bene settarlo a false e poi cambiarlo durante la creazione degli agenti?
    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;    // stato di avanzamento della malattia
    float DPI = 0; //valore di DPI impostato inizialmente a zero
    Location home = new Building("testHome");      // residenza
    Location position = home;  // posizione corrente
    List<SubscriptionInitiator> subscriptions = new LinkedList<>(); // lista sottoscrizioni (potenziali contagi)
    AID pathFinding;    // agente fornitore del servizio pathfinding

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
            // il secondo argomento specifica la posizione
            if (args.length > 1) {
                home = new Location((String) args[1]);
                position = home;
            }
        }

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
                    food = 0;
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaResources) {
            protected void onTick() {
               System.out.println("Sto aggiungendo un nuovo percorso per l'agente :" + this.myAgent.getLocalName());
               scheduleRandomWalk();
            }
        });

        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaResources) {
            protected void onTick() {
                if(IllProbability()==1) {
                     GoToHospital();
                }
            }
        });

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

        if(DeathProbability()==1){
            this.dead=true;
            //Manda messaggio ad Agente Statista
        }
    }

    int DeathProbability() {
        return ThreadLocalRandom.current().nextInt(0, 1);
    }

    int IllProbability() {
        return ThreadLocalRandom.current().nextInt(0, 1);
    }

    boolean isRecovered() {
        return diseaseStatus == SEIR.RECOVERED;
    }

    // ------------------------------------
    //  Spostamenti
    // ------------------------------------

    void setLocation(Location l) {

        System.out.println("L'agente " + getLocalName() + " si sposta in " + l.location);

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

    // ------------------------------------
    //  Task
    // ------------------------------------

    class WalkingTask extends Task {

        Queue<Road> stages = new LinkedList<>();
        String destination;
        int state = 0;

        public WalkingTask(String destination) {
            super();
            this.destination = destination;
        }

        public WalkingTask(Agent a, String destination) {
            super(a);
            this.destination = destination;
        }

        @Override
        public void onStart() {
            //assert pathFinding != null;
            ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
            msg.addReceiver(pathFinding);
            msg.setContent(position.toString() + "," + destination);
            send(msg);
        }

        @Override
        public void action() {
            switch (state) {
                // attende il messaggio contenente il path
                case 0:
                    MessageTemplate MT1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    MessageTemplate MT2 = MessageTemplate.MatchSender(pathFinding);
                    ACLMessage msg = myAgent.receive(MessageTemplate.and(MT1, MT2));

                    if (msg != null) {
                        String[] roads = msg.getContent().split(",");
                        for (String s : roads) {
                            stages.add(new Road(s));
                        }
                        state++;
                    } else {
                        System.out.println("Non ricevo nulla dal GPS");
                        block();
                    }
                    break;

                // in cammino
                case 1:
                    state++;
                    System.out.println("In attesa sulla strada");
                    block(Simulation.tick * walkingTime);
                    break;

                // raggiunta nuova location
                case 2:
                    if (stages.size() != 0) {
                        state--;
                        System.out.println("In transito da :"+stages.peek().location);
                        setLocation(stages.poll());
                    } else {
                        state = 3;
                        System.out.println("Arrivato a destinazione: "+destination);
                        setLocation(new Building(destination));
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return state == 3;
        }
    }

    class WaitingTask extends Task {

        boolean wait = true;
        int ticks;

        public WaitingTask(int ticks) {
            super();
            this.ticks = ticks;
        }

        public WaitingTask(Agent a, int ticks) {
            super(a);
            this.ticks = ticks;
        }

        @Override
        public void action() {
            if (wait) {
                wait = false;
                block(Simulation.tick);
            }
        }

        @Override
        public boolean done() {
            return !wait;
        }
    }

    public void scheduleWalkHome() {
        scheduleTask(new WalkingTask(home.toString()));
    }

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

    void GoToHospital() {

        Location hospital = new Location();
        //chiedere alle pagine gialle un ospedale disponibile
        scheduleTask(new WalkingTask(hospital.location));

    }
}
