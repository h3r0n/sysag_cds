package com.sysag_cds.people;

import com.google.common.collect.Iterators;
import com.sysag_cds.utility.BooleanProbability;
import com.sysag_cds.Simulation;
import com.sysag_cds.utility.DelayBehaviour;
import com.sysag_cds.utility.TaskAgent;
import com.sysag_cds.world.Building;
import com.sysag_cds.world.Location;
import com.sysag_cds.world.Road;
import com.sysag_cds.world.World;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
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
    static int maxfood = 10;    // dimensione riserva beni di prima necessità
    static int deltaFoodTicks = 10; // tempo di riduzione beni di prima necessità
    static int staySupermarketTicks = 10; // tempo di permanenza al supermercato
    static int deltaIllTicks = 10; // tempo di insorgenza di malattia
    static int stayHospitalTicks = 10; // tempo di degenza in ospedale
    static int randomWalkTicks = 10;    // tempo tra passeggiate
    static int businessTicks = 10;
    static int parkTicks = 10;
    static int leisureTicks = 10;
    static double deathProbability = 0.1;
    static double illProbability = 0.1;

    // status
    int food = maxfood;  // riserva beni di prima necessità
    boolean goingSuperMarket = false;
    boolean ill = false;
    boolean naughty = false; // mancato rispetto dei decreti
    static int randomWalkDistance = 2; // distanza massima passeggiata
    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;    // stato di avanzamento della malattia
    float DPI = 0; //valore di DPI impostato inizialmente a zero
    Building home = new Building("testHome");      // residenza
    Location position = home;  // posizione corrente
    List<SubscriptionInitiator> subscriptions = new LinkedList<>(); // lista sottoscrizioni (potenziali contagi)

    // messaggi
    AID statistics;    // agente fornitore del servizio Statistics

    @Override
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
                home = new Building((String) args[1]);
                position = home;
            }
            // il terzo argomento specifica il rispetto dei decreti
            if (args.length >= 3 && args[2].equals("True")) {
                naughty = true;
            }
        }

        statistics = findStatisticsAgent();

        // supermercato
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaFoodTicks) {
            protected void onTick() {
                food--;
                if (!goingSuperMarket && food < 0) {
                    goingSuperMarket = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new WalkBusinessTask(myAgent, "SuperMarket"));
                    task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * staySupermarketTicks));
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            food = maxfood;
                            goingSuperMarket = false;
                        }
                    });
                    task.addSubBehaviour(new WalkingTask(myAgent, home));
                    scheduleTask(task);
                }
            }
        });

        // passeggiata
        addBehaviour(new TickerBehaviour(this, Simulation.tick * randomWalkTicks) {
            protected void onTick() {
               System.out.println("Sto aggiungendo un nuovo percorso per l'agente :" + this.myAgent.getLocalName());
               scheduleTask(new RandomWalkTask(myAgent,randomWalkDistance));
            }
        });

        // ospedale
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaIllTicks) {
            protected void onTick() {
                if(!ill && randomIllness()) {
                    ill = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new WalkBusinessTask(myAgent,"Hospital"));
                    task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayHospitalTicks));
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            ill = false;
                        }
                    });
                    task.addSubBehaviour(new WalkingTask(myAgent,home));
                    scheduleTask(task);
                }
            }
        });

        /*
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

    public void setSusceptible() {
        diseaseStatus = SEIR.SUSCEPTIBLE;

        if (Simulation.debug)
            System.out.println(getLocalName() + " is susceptible");
    }

    public boolean isSusceptible() {
        return diseaseStatus == SEIR.SUSCEPTIBLE;
    }

    public void setExposed() {

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

    public boolean isExposed() {
        return diseaseStatus == SEIR.EXPOSED;
    }

    public void setInfectious() {
        diseaseStatus = SEIR.INFECTIOUS;
        updateStatistics("Infected");  //Manda messaggio all'agente Statistics
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

    public boolean isInfectious() {
        return diseaseStatus == SEIR.INFECTIOUS;
    }

    public void setRecovered() {

        if (diseaseStatus == SEIR.INFECTIOUS)
            deregisterContagionService();

        diseaseStatus = SEIR.RECOVERED;

        if (Simulation.debug)
            System.out.println(getLocalName() + " has recovered");

        if (randomDeath()) {
            updateStatistics("Dead"); //Manda messaggio all'agente Statistics
            this.doDelete();
        } else {
            updateStatistics("Recovered"); //Manda messaggio all'agente Statistics
        }
    }

    public boolean isRecovered() {
        return diseaseStatus == SEIR.RECOVERED;
    }

    boolean randomDeath() {
        return BooleanProbability.getBoolean(deathProbability);
    }

    boolean randomIllness() {
        return BooleanProbability.getBoolean(illProbability);
    }

    // ------------------------------------
    //  Spostamenti
    // ------------------------------------

    public void setLocation(Location l) {

        System.out.println("L'agente " + getLocalName() + " si sposta in " + l.toString());

        if (isSusceptible())
            unsubscribeAll();

        position = l;

        if (isInfectious())
            updateContagionService();

        if (isSusceptible())
            subscribeContagionService();
    }

    protected DFAgentDescription createContagionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));   // todo DPI
        dfd.addServices(sd);

        return dfd;
    }

    /**
     * Registra un servizio contagion relativo a una location
     */
    public void registerContagionService() {
        try {
            DFService.register(this, createContagionService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void updateContagionService() {
        try {
            DFService.modify(this, createContagionService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void deregisterContagionService() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifica se esistono possibilità di contagio nel luogo corrente
     */
    public void subscribeContagionService() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.addProperties(new Property("Location", position.toString()));
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
                            if (Simulation.debug) {
                                System.out.println("dfd name: " + dfd.getName());
                                System.out.println("services: " + Iterators.size(allServices));
                                System.out.println("sd type: " + sd.getType());
                                System.out.println("sd name:" + sd.getName());
                                System.out.println("Location: " + ((Property) sd.getAllProperties().next()).getValue());
                                System.out.println("Received by: " + getLocalName());
                            }
                            //todo funzione di calcolo del contagio
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

    public void unsubscribeAll() {
        if (Simulation.debug)
            System.out.println(getLocalName() + " unsubscribed");

        for (SubscriptionInitiator s : subscriptions) {
            s.cancel(getDefaultDF(), true);
        }

        subscriptions.clear();
    }

    // ------------------------------------
    //  Task
    // ------------------------------------

    /**
     * Task per raggiungere una destinazione passando per le strade intermedie.
     */
    class WalkingTask extends SequentialBehaviour {

        public WalkingTask(Agent a, Building destination) {
            super(a);

            addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    // in questo modo il calcolo del percorso verrà effettuato all'attivazione
                    List<Road> path = World.getInstance().getPath((Building) position, destination);

                    if (path!=null) {
                        for (Road r : path) {
                            addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                                @Override
                                protected void onWake() {
                                    setLocation(r);
                                }
                            });
                        }
                        addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                            @Override
                            protected void onWake() {
                                setLocation(destination);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Task per attendere un certo numero di ticks.
     */
    class WaitingTask extends DelayBehaviour {

        boolean wait = true;
        int ticks;

        public WaitingTask(Agent a, int ticks) {
            super(a, Simulation.tick * ticks);
        }

        @Override
        protected void onWake() {}
    }

    class WalkBusinessTask extends SequentialBehaviour {

        public WalkBusinessTask(Agent a, String category) {
            super(a);

            addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    // in questo modo la ricerca della destinazione verrà effettuata all'attivazione
                    Building destination = findNearestBusiness(category);
                    if (destination != null)
                        addSubBehaviour(new WalkingTask(myAgent,destination));
                }
            });
        }
    }

    /**
     * Task per effettuare una passeggiata.
     */
    class RandomWalkTask extends SequentialBehaviour {
        public RandomWalkTask(Agent a, int maxDistance) {
            super(a);

            addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    // in questo modo il calcolo del percorso verrà effettuato all'attivazione
                    Building start = (Building) position;
                    List<Road> path = World.getInstance().getRandomWalk(start, maxDistance);

                    if (path!=null) {
                        for (Road r : path) {
                            addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                                @Override
                                protected void onWake() {
                                    setLocation(r);
                                }
                            });
                        }
                        addSubBehaviour(new DelayBehaviour(myAgent, Simulation.tick * walkingTime) {
                            @Override
                            protected void onWake() {
                                setLocation(start);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Trova l'indirizzo del Business più vicino di un categoria desiderata.
     *
     * @param category categoria del Business
     * @return indirizzo del Business più vicino. null se non esiste
     */
    protected Building findNearestBusiness(String category) {

        List<Building> results = new LinkedList<>();
        Building closest = null;
        int minDist;
        World map = World.getInstance();

        // search template
        DFAgentDescription dfdt = new DFAgentDescription();
        ServiceDescription sdt = new ServiceDescription();
        sdt.setType(category);
        if (!naughty)
            sdt.addProperties(new Property("Open","True"));
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
                while (allProperties.hasNext()) {
                    Property p = (Property) allProperties.next();
                    if (p.getName().equals("Location"))
                        results.add(new Building((String) p.getValue()));
                }
            }
        }

        // pick the closest
        if (results.size()>0) {
            closest = results.get(0);
            minDist = map.getDistance((Building) position, closest);

            for (Building b : results) {
                int dist = map.getDistance((Building) position, b);
                if (dist < minDist) {
                    closest = b;
                    minDist = dist;
                }
            }
        }

        return closest;
    }

    // ------------------------------------
    //  Statistiche
    // ------------------------------------

    protected AID findStatisticsAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("PathFinding");
        dfd.addServices(sd);

        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        if(result.length>0) {
            return result[0].getName();
        } else
            return null;
    }

    protected void updateStatistics(String s) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(statistics);
        msg.setContent(s);
        send(msg);
    }

    /*

    // ------------------------------------
    //  Decreti
    // ------------------------------------

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
