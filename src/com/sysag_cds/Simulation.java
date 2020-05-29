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

import java.util.ArrayList;
import java.util.List;

public class Simulation extends Agent {
    public static int tick = 1000;
    public static int decreeTick = 10;
    public static boolean debug = true;
    public static int buildingInform = 20;
    public static int statsInform = 21;
    public static int decreeInform = 22;
    public int deathCounts = 0;
    public int recoveredCounts = 0;
    public int infectedCounts = 0;

    protected int nPeople;
    protected double[] diseaseDistribution = new double[4];
    protected int mapSize;     // la mappa è un quadrato, mapSize è il numero di Building per lato
    protected double naughtyProb;

    World map;

    List<Building> buildings;
    List<AID> people = new ArrayList<>();

    protected void setup() {

        //registerGrimReaperService();
        //addBehaviour(new manageStatsCounts());

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
    void readArgs(Object[] args) {
        nPeople = Integer.parseInt((String) args[0]);
        diseaseDistribution[Person.SEIR.SUSCEPTIBLE.ordinal()] = Double.parseDouble((String) args[1]);
        diseaseDistribution[Person.SEIR.EXPOSED.ordinal()] = Double.parseDouble((String) args[2]);
        diseaseDistribution[Person.SEIR.INFECTIOUS.ordinal()] = Double.parseDouble((String) args[3]);
        diseaseDistribution[Person.SEIR.RECOVERED.ordinal()] = Double.parseDouble((String) args[4]);
        naughtyProb = Double.parseDouble((String) args[4]);
        mapSize = Integer.parseInt((String) args[5]);
    }

    void createPeople() {

        PersonFactory pf = new PersonFactory(
                this,
                new RandomBuilding(map),
                new RandomSEIR(diseaseDistribution[0], diseaseDistribution[1], diseaseDistribution[2], diseaseDistribution[3]),
                new RandomNaughty(naughtyProb)
        );

        for (int i = 0; i < nPeople; i++)
            pf.create();
    }


    /*

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
