package com.sysag_cds;

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
    public static boolean debug = true;

    protected int nPeople = 1;
    protected double[] diseaseDistribution = new double[4];
    protected int mapSize;     // la mappa è un quadrato, mapSize è il numero di Building per lato

    Graph<Building,Road> map;

    protected void setup()  {

        readArgs(getArguments());
        List<Building> buildingList = new ArrayList<>();
        buildMap(buildingList);
        createPeople(buildingList);

        addBehaviour(new managePathFindingRequest());
        registerPathFindingService();
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
                ACLMessage reply = msg.createReply();
                String[] query = msg.getContent().split(",");
                reply.setPerformative(ACLMessage.INFORM );
                reply.setContent(getPath(query[0],query[1]));
            } else
                block();
        }
    }
}
