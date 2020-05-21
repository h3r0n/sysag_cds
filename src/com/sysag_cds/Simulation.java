package com.sysag_cds;

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import jade.core.AID;
import jade.core.Agent;
import jade.core.BackEndContainer;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Simulation extends Agent {
    public static int tick = 1000;
    public static int decreeTick=10;
    public static boolean debug = true;
    public int deathCounts = 0;
    public int recoveredCounts = 0;
    public int infectedCounts = 0;
    public static int buildingInform=20;
    public static int statsInform=21;
    public static int decreeInform=22;

    protected int nPeople = 1;
    protected double[] diseaseDistribution = new double[4];
    protected int mapSize;     // la mappa è un quadrato, mapSize è il numero di Building per lato

    Graph<Building,Road> map;

    List<Building> buildings = new ArrayList<>();
    List<AID> agents= new ArrayList<>();

    protected void setup()  {

        registerPathFindingService();
        addBehaviour(new managePathFindingRequest());
        /*
        registerFactoryBuildingFindingService();
        registerParkBuildingFindingService();
        registerMarketBuildingFindingService();
        registerShopBuildingFindingService();
        registerSchoolBuildingFindingService();
        registerHospitalBuildingFindingService();
         */
        registerBuildingFindingService();
        addBehaviour(new manageBuildingFindingRequest());

        registerGrimReaperService();
        addBehaviour(new manageStatsCounts());

        readArgs(getArguments());
        List<Building> buildingList = new ArrayList<>();
        buildMap(buildingList);
        createPeople(buildingList);

        addBehaviour(new WakerBehaviour(this, Simulation.tick * decreeTick) {
            @Override
            protected void onWake() {
                Decree();
            }
        });


        //Siccome vengono create prima le persone del servizio gps, talvolta le persone lo chiamano prima che sia registrato il servizio, invertire l'ordine?
    }

    /*
        Parametri di Simulation:
            [0] numero di Person
            [2-4] probabilità di avere persone SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED
            [5] numero di Building per lato, considerando che la mappa è un quadrato. Deve essere >= 2

        Esempio: -agents simulation:com.sysag_cds.Simulation(1,.5,0,.5,0,2)
            Crea 1 agente Person, con il 50% di probabilità di essere SUSCEPTIBLE e il 50% di essere INFECTIOUS
            Crea poi una mappa con 4 edifici

     */
    void readArgs(Object[] args) {
        nPeople = Integer.parseInt((String) args[0]);
        diseaseDistribution[Person.SEIR.SUSCEPTIBLE.ordinal()] = Double.parseDouble((String) args[1]);
        diseaseDistribution[Person.SEIR.EXPOSED.ordinal()] = Double.parseDouble((String) args[2]);
        diseaseDistribution[Person.SEIR.INFECTIOUS.ordinal()] = Double.parseDouble((String) args[3]);
        diseaseDistribution[Person.SEIR.RECOVERED.ordinal()] = Double.parseDouble((String) args[4]);
        mapSize = Integer.parseInt((String) args[5]);
    }

    void buildMap(List<Building> buildingList) {

        map = (
                new Lattice2DGenerator<>(UndirectedSparseGraph.getFactory(),
                        new BuildingFactory(buildingList),
                        new RoadFactory(),
                        mapSize, false
                )
        ).get();

        if (Simulation.debug) {
            System.out.println(map.toString());
            System.out.println("Vertices=Building, Edges=Roads");
        }
    }

    void createPeople(List<Building> buildingList) {

        ContainerController c = getContainerController();
        Object [] personArgs = new Object[2];
        Random rand = new Random();
        String[] diseaseStatus = new String[]{"SUSCEPTIBLE","EXPOSED","INFECTIOUS","RECOVERED"};
        int[] dsIndex = new int[]{0,1,2,3};
        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(dsIndex,diseaseDistribution);



        for (int i = 0; i<nPeople; i++) {
            personArgs[0] = diseaseStatus[distribution.sample()];    // assegna uno stato di salute casuale
            personArgs[1] = buildingList.get(rand.nextInt(buildingList.size())).toString();   // assegna una casa casuale
            try {
                AgentController a = c.createNewAgent( "p"+i, "com.sysag_cds.Person", personArgs);
                a.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    String getPath(String begin, String end) {
        List<Road> list = (new DijkstraShortestPath<>(map)).getPath(new Building(begin),new Building(end));
        Iterator<Road> iterator = list.iterator();
        StringBuilder path = new StringBuilder();

        if (iterator.hasNext())
            path.append(iterator.next());
        while (iterator.hasNext()) {
            path.append(",");
            path.append(iterator.next().toString());
        }

        return path.toString();
    }

    // trova il percorso minimo data la posizione dell'agente e il tipo di negozio che vuole raggiungere
    //Funzione da modificare
    String getBuildingPath(String begin, String end) {
        List<Road> list = (new DijkstraShortestPath<>(map)).getPath(new Building(begin),new Building(end));
        Iterator<Road> iterator = list.iterator();
        StringBuilder path = new StringBuilder();

        if (iterator.hasNext())
            path.append(iterator.next());
        while (iterator.hasNext()) {
            path.append(",");
            path.append(iterator.next().toString());
        }

        return path.toString();
    }

    void registerPathFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("PathFinding");
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    class managePathFindingRequest extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate MT1=MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
            ACLMessage msg = myAgent.receive(MT1);

            if (msg != null) {
                System.out.println("E' arrivato un messaggio per il GPS");
                ACLMessage reply = msg.createReply();
                String[] query = msg.getContent().split(",");
                reply.setPerformative(ACLMessage.INFORM );
                reply.setContent(getPath(query[0],query[1]));
                //reply.addReceiver(msg.getSender());
                send(reply);
            } else
                block();
        }
    }

    void registerBuildingFindingService() {
        for(int i=0;i<buildings.size();i++) {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType(buildings.get(i).bus.toString());
            sd.addProperties(new Property("Stato", "Aperto"));
            sd.addProperties(new Property("Posizione", buildings.get(i).location));
            sd.addProperties(new Property("Distanza", buildings.get(i).distanceDPI));
            sd.setName(getLocalName());
            //sd.addProperties(new Property("Location", position.toString()));
            //sd.addProperties(new Property("DPI", 0.5));
            dfd.addServices(sd);

            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    /*
    void registerMarketBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Supermercato");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void registerShopBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Negozio");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void registerParkBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Parco");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void registerFactoryBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Fabbrica");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void registerSchoolBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Scuola");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void registerHospitalBuildingFindingService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Ospedale");
        sd.addProperties(new Property("Stato","Aperto"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
*/
    class manageBuildingFindingRequest extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate MT1=MessageTemplate.MatchPerformative(buildingInform);
            ACLMessage msg = myAgent.receive(MT1);

            if (msg != null) {
                System.out.println("E' arrivato un messaggio per il Gestore Edifici");
                ACLMessage reply = msg.createReply();
                String[] query = msg.getContent().split(",");
                reply.setPerformative(ACLMessage.INFORM );
                reply.setContent(getBuildingPath(query[0],query[1]));
                //reply.addReceiver(msg.getSender());
                send(reply);
            } else
                block();
        }
    }

    void registerGrimReaperService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("GrimReaper");
        sd.setName(getLocalName());
        //sd.addProperties(new Property("Location", position.toString()));
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    class manageStatsCounts extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate MT1=MessageTemplate.MatchPerformative(statsInform);
            ACLMessage msg = myAgent.receive(MT1);

            if (msg != null) {
                System.out.println("E' arrivato un messaggio per il Tristo Mietitore");
                String query = msg.getContent();
                switch (query){
                    case "Death":
                        deathCounts++;
                        break;
                    case "Infected":
                        infectedCounts++;
                        break;
                    case "Recovered":
                        recoveredCounts++;
                        break;
                }
                System.out.println("Sono deceduti "+deathCounts+" agenti nella simulazione, "+(nPeople-deathCounts)+" agenti rimanenti, di cui "+infectedCounts+ " infetti, "+recoveredCounts+" guariti e "+(nPeople-deathCounts-recoveredCounts-infectedCounts)+" esposti.");
            } else
                block();
        }
    }

    class Decree extends Behaviour{

        @Override
        public void action() {
           updateShopService();
           updateSchoolService();
           sendBroadcastMessage();
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    void updateMarketService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Supermercato");
        sd.addProperties(new Property("Stato","Chiuso"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.modify(this, getDefaultDF(), dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void updateShopService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Negozio");
        sd.addProperties(new Property("Stato","Chiuso"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.modify(this, getDefaultDF(), dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void updateFactoryService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Fabbrica");
        sd.addProperties(new Property("Stato","Chiuso"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.modify(this, getDefaultDF(), dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void updateSchoolService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Scuola");
        sd.addProperties(new Property("Stato","Chiuso"));
        sd.setName(getLocalName());
        //sd.addProperties(new Property("DPI", 0.5));
        dfd.addServices(sd);

        try {
            DFService.modify(this, getDefaultDF(), dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    void sendBroadcastMessage(){
        ACLMessage msg= new ACLMessage();
        for(int i=0;i<agents.size();i++){
            msg.addReceiver(agents.get(i));
            msg.setContent("0.7");
        }
        send(msg);
    }
}
