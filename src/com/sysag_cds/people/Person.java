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
    static int seirDelta = 7*Simulation.day;   // tempo di incubazione (da EXPOSED a INFECTIOUS)
    static int seirGamma = 14*Simulation.day;   // tempo di guarigione (da INFECTIOUS a RECOVERED)
    static int walkingTime = 1; // tempo per percorrere una strada
    static int maxfood = 2*Simulation.day;    // dimensione riserva beni di prima necessità
    static int deltaFoodTicks = 1; // tempo di riduzione beni di prima necessità
    static int staySupermarketTicks = 10; // tempo di permanenza al supermercato
    static int deltaIllTicks = Simulation.day; // tempo di possibile insorgenza di malattia
    static int stayHospitalTicks = 2*Simulation.day; // tempo di degenza in ospedale
    static int walkingTicks = Simulation.day;    // tempo tra passeggiate
    static int parkTicks = 2*Simulation.day;    // tempo tra visite al parco
    static int stayParkTicks = (int)(.5*Simulation.day);    // tempo di permanenza al parco
    static double eventProbability = 0.2;   // probabilità di accettare un invito ad un evento
    static int stayEventTicks = (int)(.5*Simulation.day);    // tempo di permanenza ad un evento
    static double illProbability = 0.01;     // probabilità giornaliera di necessitare dell'ospedale
    static double diseaseIllProbability = 0.05; // probabilità giornaliera di necessitare dell'ospedale se ammalati
    static double deathProbability = 0.05;  // probabilità giornaliera di morire con la malattia e ricovero
    static double outDeathProbability = 0.8;  // probabilità giornaliera di morire con la malattia e degenza negata in ospedale

    // status
    int food = maxfood;  // riserva beni di prima necessità
    boolean goingSuperMarket = false;
    boolean goingHospital = false;
    boolean noHospital = false; // negata degenza in ospedale
    boolean bedTaken = false; // assegnato un letto in ospedale
    boolean ill = false;
    boolean goingPark = false;
    boolean goingWalk = false;
    boolean goingEvent = false;
    boolean naughty = false; // mancato rispetto dei decreti
    static int walkingDistance = 10; // distanza massima passeggiata (naugthy)
    protected SEIR diseaseStatus = SEIR.SUSCEPTIBLE;    // stato di avanzamento della malattia
    Building home = new Building("testHome");      // residenza
    Location position = home;  // posizione corrente
    List<SubscriptionInitiator> subscriptions = new LinkedList<>(); // lista sottoscrizioni (potenziali contagi)

    // messaggi
    Decree currentDecree = new Decree();
    int beds = 100;

    @Override
    protected void setup() {

        if (Simulation.debug)
            System.out.println(getLocalName() + " started.");

        // inizializzazione agente
        Object[] args = this.getArguments();
        if (args != null) {
            // il primo argomento specifica lo stato della malattia:
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
            // il terzo argomento specifica l'incoscienza
            if (args.length >= 3 && args[2].equals("True")) {
                naughty = true;
            }
        }

        subscribeDecrees();
        subscribeHealthCare();
        subscribeEvents();

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
                            if (Simulation.debug)
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        if (Simulation.debug)
                                            System.out.println(getLocalName()+" is going to the Supermarket.");
                                    }
                                });
                            task.addSubBehaviour(new TravelTask(myAgent, destination));
                            task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * staySupermarketTicks));
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    food = maxfood;
                                    goingSuperMarket = false;
                                    if (Simulation.debug)
                                        System.out.println(getLocalName()+" is coming back home from the Supermarket.");
                                }
                            });
                            task.addSubBehaviour(new TravelTask(myAgent, home));
                        } else {
                            goingSuperMarket = false;
                            if (Simulation.debug)
                                System.out.println(getLocalName()+" cannot go to the Supermarket.");
                        }
                        }
                    });
                    scheduleTask(task);
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" wants to go to the Supermarket.");
                }
            }
        });

        // passeggiata
        addBehaviour(new TickerBehaviour(this, Simulation.tick * walkingTicks) {
            protected void onTick() {
                if (!goingWalk) {
                    goingWalk = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            if (Simulation.debug) {
                                if (currentDecree.getWalkDistance() == 0 && !naughty)
                                    System.out.println(getLocalName() + " cannot go for a walk.");
                                else
                                    System.out.println(getLocalName() + " is going for a walk.");
                            }
                            if (currentDecree.getWalkDistance() > 0 && !naughty)
                                task.addSubBehaviour(new WalkingTask(myAgent, currentDecree.getWalkDistance()));
                            if (naughty)
                                task.addSubBehaviour(new WalkingTask(myAgent, walkingDistance));
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    goingWalk = false;
                                }
                            });
                        }
                    });
                    scheduleTask(task);
                    if (Simulation.debug)
                        System.out.println(getLocalName() + " wants to go for a walk.");
                }
            }
        });

        // ospedale
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaIllTicks) {
            protected void onTick() {
                if (!ill) {
                    ill = randomIllness();
                    if (Simulation.debug && ill)
                        System.out.println(getLocalName() + " is ill and needs to go to the Hospital.");
                }
                if(ill && !goingHospital) {
                    goingHospital = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            Building destination = findNearestBusiness("Hospital");
                            if (destination!=null && beds>0) {
                                noHospital = false;
                                bedTaken = true;
                                updateBeds(true);
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        if (Simulation.debug)
                                            System.out.println(getLocalName()+" is going to the Hospital.");
                                    }
                                });
                                task.addSubBehaviour(new TravelTask(myAgent, destination));
                                task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayHospitalTicks));
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        ill = false;
                                        goingHospital = false;
                                        bedTaken = false;
                                        updateBeds(false);
                                        if (Simulation.debug)
                                            System.out.println(getLocalName()+" is coming back home from the Hospital.");
                                    }
                                });
                                task.addSubBehaviour(new TravelTask(myAgent, home));
                            } else {
                                noHospital = true;
                                goingHospital = false;
                                if (Simulation.debug)
                                    System.out.println(getLocalName()+" cannot go to the Hospital.");
                            }
                        }
                    });
                    scheduleTask(task);
                }
            }
        });

        // morte da covid: si può morire se si è infetti(isInfectious) e con sintomi(ill)
        addBehaviour(new TickerBehaviour(this, Simulation.tick * deltaIllTicks) {
            @Override
            protected void onTick() {
                if (ill && isInfectious() && randomDeath()) {
                    if (bedTaken)
                        updateBeds(false);
                    updateStatistics("Dead"); //Manda messaggio all'agente Statistics
                    myAgent.doDelete();
                }
            }
        });

        // parco
        addBehaviour(new TickerBehaviour(this, Simulation.tick * parkTicks) {
            protected void onTick() {
                if (!goingPark) {
                    goingPark = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                        Building destination = findNearestBusiness("Park");
                        if (destination!=null) {
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    if (Simulation.debug)
                                        System.out.println(getLocalName()+" is going to the Park.");
                                }
                            });
                            task.addSubBehaviour(new TravelTask(myAgent, destination));
                            task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayParkTicks));
                            task.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                goingPark = false;
                                    if (Simulation.debug)
                                        System.out.println(getLocalName()+" is coming back home from the Park.");
                                }
                            });
                            task.addSubBehaviour(new TravelTask(myAgent, home));
                        } else {
                            goingPark = false;
                            if (Simulation.debug)
                                System.out.println(getLocalName()+" cannot go to the Park.");
                        }
                        }
                    });
                    scheduleTask(task);
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" wants to go to the Park.");
                }
            }
        });
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

        updateStatistics("Recovered");
    }

    public boolean isRecovered() {
        return diseaseStatus == SEIR.RECOVERED;
    }

    boolean randomDeath() {
        if (noHospital) {
            return BooleanProbability.getBoolean(outDeathProbability);
        } else {
            return BooleanProbability.getBoolean(deathProbability);
        }
    }

    boolean randomIllness() {
        if (isInfectious())
            return BooleanProbability.getBoolean(diseaseIllProbability);
        else
            return BooleanProbability.getBoolean(illProbability);
    }

    // ------------------------------------
    //  Spostamenti
    // ------------------------------------

    public void setLocation(Location l) {

        //System.out.println("L'agente " + getLocalName() + " si sposta in " + l.toString());

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
        sd.addProperties(new Property("Location", position.getLocation()));   // location
        sd.addProperties(new Property("DPI", Boolean.toString(haveDPI())));   // boolean
        sd.addProperties(new Property("Distancing", Double.toString(distancing()))); // double
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
        sd.addProperties(new Property("Location", position.getLocation()));   // location
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
                                    infectiousDPI = Boolean.parseBoolean((String)p.getValue());
                                if (p.getName().equals("Distancing"))
                                    infectiousDist = Double.parseDouble((String)p.getValue());
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
        //if (Simulation.debug)
            //System.out.println(getLocalName() + " unsubscribed");

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
            super(a, ticks);
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
                                if (Simulation.debug)
                                    System.out.println(this.myAgent.getLocalName()+" comes back home from a walk.");
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
            if (minDist>currentDecree.getMaxTravel())
                closest=null;
        }

        return closest;
    }

    // ------------------------------------
    //  Eventi
    // ------------------------------------

    /**
     * Notifica inviti ad eventi
     */
    public void subscribeEvents() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Event");
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" received a new event invitation.");
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
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
                            if (randomGoEvent() && currentDecree.getEventOpen())
                                goEvent(buil);
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };
        addBehaviour(subscription);
    }

    void goEvent(Building eventSetting) {
        if (!goingEvent) {
            goingEvent = true;
            SequentialBehaviour task = new SequentialBehaviour();
            task.addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    if (World.getInstance().getDistance(home, eventSetting) <= currentDecree.getMaxTravel()) {
                        task.addSubBehaviour(new OneShotBehaviour() {
                            @Override
                            public void action() {
                                if (Simulation.debug)
                                    System.out.println(getLocalName() + " is going to the Event.");
                            }
                        });
                        task.addSubBehaviour(new TravelTask(myAgent, eventSetting));
                        task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * stayEventTicks));
                        task.addSubBehaviour(new OneShotBehaviour() {
                            @Override
                            public void action() {
                                goingEvent = false;
                                if (Simulation.debug)
                                    System.out.println(getLocalName() + " is coming back home from the Event.");
                            }
                        });
                        task.addSubBehaviour(new TravelTask(myAgent, home));
                    } else {
                        goingEvent = false;
                        if (Simulation.debug)
                            System.out.println(getLocalName() + " cannot go to the Event.");
                    }
                }
            });
            scheduleTask(task);
            if (Simulation.debug)
                System.out.println(getLocalName() + " wants to go to the Event.");
        }
    }

    boolean randomGoEvent() {
        return BooleanProbability.getBoolean(eventProbability);
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
        msg.addReceiver(new AID("Statistics", AID.ISLOCALNAME));
        msg.setContent(s);
        send(msg);
    }

    // ------------------------------------
    //  Contagio
    // ------------------------------------

    // True se due persone si avvicinano
    boolean meet(double infectiousDist) {
        double susceptibleDist = distancing();
        boolean m = BooleanProbability.getBoolean(Math.max(infectiousDist,susceptibleDist));
        if (Simulation.debug)
            System.out.println("Meet: "+m+", Prob: "+Math.max(infectiousDist,susceptibleDist));
        return m;
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

        boolean c = BooleanProbability.getBoolean(probability);

        if (Simulation.debug)
            System.out.println("Contagion: "+c+", DPI: "+infectiousDPI+haveDPI()+", home: "+position.equals(home));

        return c;
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
                    if (Simulation.debug)
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
                                    currentDecree.setMaskRequired( currentDecree.parseString((String) p.getValue()));
                                if (p.getName().equals("density"))
                                    currentDecree.setDensity( Double.parseDouble((String)p.getValue()));
                                if (p.getName().equals("eventOpen"))
                                    currentDecree.setEventOpen(Boolean.parseBoolean((String)p.getValue()));
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

    // ------------------------------------
    //  Posti letto
    // ------------------------------------

    /**
     * Notifica aggiornamenti relativi alla disponibilità di posti letto in ospedale
     */
    public void subscribeHealthCare() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("HealthCare");
        template.addServices(sd);

        SubscriptionInitiator subscription = new SubscriptionInitiator(
                this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            protected void handleInform(ACLMessage inform) {
                try {
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" received a new update about Hospital beds");
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (DFAgentDescription dfd : dfds) {
                        Iterator allServices = dfd.getAllServices();
                        while (allServices.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) allServices.next();
                            Iterator allProperties = sd.getAllProperties();
                            while (allProperties.hasNext()) {
                                Property p = (Property) allProperties.next();
                                if (p.getName().equals("beds"))
                                    beds = Integer.parseInt((String)p.getValue());
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

    void updateBeds(boolean b) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("HealthCare", AID.ISLOCALNAME));
        msg.setContent(Boolean.toString(b));
        send(msg);
    }
}
