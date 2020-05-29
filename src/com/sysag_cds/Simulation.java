package com.sysag_cds;

import com.sysag_cds.world.Building;
import com.sysag_cds.world.RandomBuilding;
import com.sysag_cds.world.World;
import com.sysag_cds.people.RandomNaughty;
import com.sysag_cds.people.Person;
import com.sysag_cds.people.PersonFactory;
import com.sysag_cds.people.RandomSEIR;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;

public class Simulation extends Agent {
    public static int tick = 1000;
    private static int decreeTick = 10;
    public static boolean debug = true;
    // public static int buildingInform = 20;
    // public static int statsInform = 21;
    // public static int decreeInform = 22;


    private int nPeople;
    private double[] diseaseDistribution = new double[4];
    private int mapSize;     // la mappa è un quadrato, mapSize è il numero di Building per lato
    private double naughtyProb;
    private World map;

    private List<Building> buildings;
    private List<AID> people = new ArrayList<>();

    protected void setup() {

        startStatistics();

        readArgs(getArguments());
        map = World.getInstance(mapSize);
        createPeople();

        /*
        addBehaviour(new WakerBehaviour(this, Simulation.tick * decreeTick) {
            @Override
            protected void onWake() {
                Decree();
            }
        });
        */
    }

    /*
        Parametri di Simulation:
            [0] numero di Person
            [2-4] probabilità di avere persone SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED
            [5] probabilità di avere persone che non rispettano i decreti (tra 0 e 1)
            [6] numero di Building per lato, considerando che la mappa è un quadrato. Deve essere >= 2

        Esempio: -agents simulation:com.sysag_cds.Simulation(1,.5,0,.5,0,2)
            Crea 1 agente Person, con il 50% di probabilità di essere SUSCEPTIBLE e il 50% di essere INFECTIOUS
            Crea poi una mappa con 4 edifici

     */
    private void readArgs(Object[] args) {
        nPeople = Integer.parseInt((String) args[0]);
        diseaseDistribution[Person.SEIR.SUSCEPTIBLE.ordinal()] = Double.parseDouble((String) args[1]);
        diseaseDistribution[Person.SEIR.EXPOSED.ordinal()] = Double.parseDouble((String) args[2]);
        diseaseDistribution[Person.SEIR.INFECTIOUS.ordinal()] = Double.parseDouble((String) args[3]);
        diseaseDistribution[Person.SEIR.RECOVERED.ordinal()] = Double.parseDouble((String) args[4]);
        naughtyProb = Double.parseDouble((String) args[4]);
        mapSize = Integer.parseInt((String) args[5]);
    }

    private void createPeople() {

        PersonFactory pf = new PersonFactory(
                this,
                new RandomBuilding(map),
                new RandomSEIR(diseaseDistribution[0], diseaseDistribution[1], diseaseDistribution[2], diseaseDistribution[3]),
                new RandomNaughty(naughtyProb)
        );

        for (int i = 0; i < nPeople; i++)
            pf.create();
    }

    private void startStatistics() {
        ContainerController c = getContainerController();
        try {
            AgentController a = c.createNewAgent("Statistics", "com.sysag_cds.Statistics", null);
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
/*
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

    void sendBroadcastMessage(){
        ACLMessage msg= new ACLMessage();
        for(int i=0;i<agents.size();i++){
            msg.addReceiver(agents.get(i));
            msg.setContent("0.7");
        }
        send(msg);
    }

    */
}
