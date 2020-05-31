package com.sysag_cds.people;

import com.sysag_cds.utility.BooleanProbability;
import com.sysag_cds.superagents.Simulation;
import com.sysag_cds.utility.Decree;
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

/**
 * L'agente Person simula i comportamenti di una persona che si muove nella mappa per diversi
 * scopi. È in grado di contrarre la malattia e di contagiare altri agenti Person.
 */
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
    static int walkingTicks = 10;    // tempo tra passeggiate
    static int parkTicks = 100;
    static int stayParkTicks = 10;
    static double deathProbability = 0.1;
    static double illProbability = 0.1;

    // status
    int food = maxfood;  // riserva beni di prima necessità
    boolean goingSuperMarket = false;
    boolean goingHospital = false;
    boolean ill = false;
    boolean goingPark = false;
    boolean naughty = false; // mancato rispetto dei decreti
    static int walkingDistance = 2; // distanza massima passeggiata
    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;    // stato di avanzamento della malattia
    Building home = new Building("testHome");      // residenza
    Location position = home;  // posizione corrente
    List<SubscriptionInitiator> subscriptions = new LinkedList<>(); // lista sottoscrizioni (potenziali contagi)

    // messaggi
    AID statistics;    // agente fornitore del servizio Statistics
    Decree currentDecree = new Decree();

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
                home = World.getInstance().findBuilding(new Building((String) args[1]));
                home.setDensity(1.0);
                position = home;
            }
            // il terzo argomento specifica il rispetto dei decreti
            if (args.length >= 3 && args[2].equals("True")) {
                naughty = true;
            }
        }

        statistics = findServiceAgent("Statistics");
        subscribeDecrees();

        // supermercato
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaFoodTicks) {
            protected void onTick() {
                food--;
                if (!goingSuperMarket && food < 0) {
                    goingSuperMarket = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                        Building destination = findNearestBusiness("SuperMarket");
                        if (destination!=null) {
                            task.addSubBehaviour(new TravelTask(myAgent, destination));
                            task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * staySupermarketTicks));
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    food = maxfood;
                                    goingSuperMarket = false;
                                }
                            });
                            task.addSubBehaviour(new TravelTask(myAgent, home));
                        } else {
                            goingSuperMarket = false;
                        }
                        }
                    });
                    scheduleTask(task);
                }
            }
        });

        // passeggiata
        addBehaviour(new TickerBehaviour(this, Simulation.tick * walkingTicks) {
            protected void onTick() {
               SequentialBehaviour task = new SequentialBehaviour();
               task.addSubBehaviour(new OneShotBehaviour() {
                   @Override
                   public void action() {
                       if (currentDecree.getWalkDistance()>0)
                           task.addSubBehaviour(new WalkingTask(myAgent, currentDecree.getWalkDistance()));
                   }
               });
               scheduleTask(task);
            }
        });

        // ospedale
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaIllTicks) {
            protected void onTick() {
                if (!ill)
                    ill = randomIllness();
                if(ill && !goingHospital) {
                    goingHospital = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            Building destination = findNearestBusiness("Hospital");
                            if (destination!=null) {
                                task.addSubBehaviour(new TravelTask(myAgent, destination));
                                task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayHospitalTicks));
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        ill = false;
                                        goingHospital = false;
                                    }
                                });
                                task.addSubBehaviour(new TravelTask(myAgent, home));
                            } else {
                                goingHospital = false;
                            }
                        }
                    });
                    scheduleTask(task);
                }
            }
        });

        // parco
        addBehaviour(new TickerBehaviour(this, Simulation.tick * parkTicks) {
            protected void onTick() {
                food--;
                if (!goingSuperMarket && food < 0) {
                    goingSuperMarket = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                        Building destination = findNearestBusiness("Park");
                        if (destination!=null) {
                            task.addSubBehaviour(new TravelTask(myAgent, destination));
                            task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayParkTicks));
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                goingPark = false;
                                }
                            });
                            task.addSubBehaviour(new TravelTask(myAgent, home));
                        } else {
                            goingPark = false;
                        }
                        }
                    });
                    scheduleTask(task);
                }
            }
        });

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

    protected DFAgentDescription createContagionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.setName(getLocalName());
        sd.addProperties(new Property("Location", position));   // location
        sd.addProperties(new Property("DPI", haveDPI()));   // boolean
        sd.addProperties(new Property("Distancing", distancing())); // double
        dfd.addServices(sd);

        return dfd;
    }

    /**
     * Notifica se esistono possibilità di contagio nel luogo corrente
     */
    public void subscribeContagionService() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Contagion");
        sd.addProperties(new Property("Location", position));
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            boolean infectiousDPI = true;
                            double infectiousDist = 0;

                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            Iterator allProperties = sd.getAllProperties();
                            while (allProperties.hasNext()) {
                                Property p = (Property) allProperties.next();
                                if (p.getName().equals("DPI"))
                                    infectiousDPI = (boolean) p.getValue();
                                if (p.getName().equals("Distancing"))
                                    infectiousDist = (double) p.getValue();
                            }

                            if (meet(infectiousDist) && contagion(infectiousDPI))
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
    class TravelTask extends SequentialBehaviour {

        public TravelTask(Agent a, Building destination) {
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

    /**
     * Task per effettuare una passeggiata.
     */
    class WalkingTask extends SequentialBehaviour {
        public WalkingTask(Agent a, int maxDistance) {
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
                        results.add((Building) p.getValue());
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

    protected AID findServiceAgent(String type) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
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

    // ------------------------------------
    //  Contagio
    // ------------------------------------

    // True se due persone si avvicinano
    boolean meet(double infectiousDist) {
        double susceptibleDist = distancing();
        return BooleanProbability.getBoolean(Math.max(infectiousDist,susceptibleDist));
    }

    // True se avviene il contagio
    boolean contagion(boolean infectiousDPI) {
        double probability = 1;
        boolean susceptibleDPI = haveDPI();

        if (!infectiousDPI && susceptibleDPI)
            probability = .7;

        if (infectiousDPI && !susceptibleDPI)
            probability = .05;

        if (infectiousDPI && susceptibleDPI)
            probability = .015;

        return BooleanProbability.getBoolean(probability);
    }

    // Probabilità di avvicinarsi a qualcuno
    double distancing() {
        Double minDist = position.getDensity();
        Double maxDist = naughty ? 1 : currentDecree.getDensity();
        return minDist + Math.random() * (maxDist-minDist);
    }

    // Possesso DPI come mascherine e guanti
    boolean haveDPI() {
        boolean have = false;
        boolean indoor = position instanceof Building;

        if (!naughty && currentDecree.getMaskRequired()==Decree.LAW.ALWAYS && !position.equals(home))
            have = true;

        if (!naughty && currentDecree.getMaskRequired()==Decree.LAW.INDOOR && indoor && !position.equals(home))
            have = true;

        return have;
    }

    // ------------------------------------
    //  Decreti
    // ------------------------------------

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
                                    currentDecree = (Decree) p.getValue();
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
}
