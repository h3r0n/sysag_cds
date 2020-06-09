package com.sysag_cds.superagents;

import com.sysag_cds.world.BusinessFactory;
import com.sysag_cds.world.RandomBuilding;
import com.sysag_cds.world.RandomBusiness;
import com.sysag_cds.world.World;
import com.sysag_cds.people.Person;
import com.sysag_cds.people.PersonFactory;
import com.sysag_cds.people.RandomSEIR;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * The Simulation Agent creates other agents and starts the simulation.
 * Simulation Parameters:
 *     [0] number of Person agents
 *     [1-4] probability that generates  SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED Person agents
 *     [5] probability that generates naughty Person agents (beetwen 0 and 1)
 *     [6] probability that generates workers
 *     [7] number of Building of each side, the map is a square. It must be >= 2
 *     [8] number of Business building
 *     [9] number of hospital beds
 *
 * Example: -agents simulation:com.sysag_cds.superagents.Simulation(100,.9,0,.1,0,.2,.5,15,20,5)
 *     Generates 100 Persons, with the 90% probability of been SUSCEPTIBLE and the 10% probability to be INFECTIOUS
 *     20% probability to be naughty e il 50% probability to be workers
 *     Generates a map composed by 225 building, in which 20 are Business
 *     Among all hospitals there are 5 beds
 */
public class Simulation extends Agent {
    public static int tick = 1000;
    public static int day = 60;
    public static boolean debug = false;

    private int nPeople;
    private int nBusiness;
    private double[] diseaseDistribution = new double[4];
    private int mapSize;
    private double naughtyProb;
    private double workerProb;
    private int beds;


    protected void setup() {

        startAgent("Statistics", "com.sysag_cds.superagents.Statistics", null);  // avvia superagente Statistics
        startAgent("Government", "com.sysag_cds.superagents.Government", null);  // avvia superagente Government

        readArgs(getArguments());   // leggi argomenti
        World.getInstance(mapSize); // inizializza mappa
        RandomBuilding randomBuilding = new RandomBuilding();   // edifici casuali per case e business

        // crea Business
        BusinessFactory bf = new BusinessFactory(
                this,   // agente che crea gli agenti Business
                randomBuilding, // selezionatore casuale di Building in cui posizionare il business
                new RandomBusiness(new double[]{.3, .1, .2, .2, .2})    // selezionatore casuale di categorie di business
        );

        for (int i = 0; i < nBusiness; i++)
            bf.create();

        System.out.println(bf.getList().toString());
        System.out.println(bf.getList().toString());
        RandomBuilding randomWorkplace = new RandomBuilding(bf.getList());   // edifici casuali per posti di lavoro

        // crea Persone
        PersonFactory pf = new PersonFactory(
                this,   // agente che crea gli agenti Person
                randomBuilding, // selezionatore casuale di Building da impostare come casa
                randomWorkplace,    // selezionatore casuale di Building da impostare come posto di lavoro
                new RandomSEIR( // selezionatore casuale di stati della malattia
                        diseaseDistribution[0],
                        diseaseDistribution[1],
                        diseaseDistribution[2],
                        diseaseDistribution[3]
                ),
                naughtyProb,    // probabilità di creare incoscienti
                workerProb  // probabilità di creare lavoratori
        );

        for (int i = 0; i < nPeople; i++)
            pf.create();

        startAgent("HealthCare", "com.sysag_cds.superagents.HealthCare", new Object[]{Integer.toString(beds)});  // avvia superagente HealthCare
        startAgent("EventPlanner", "com.sysag_cds.superagents.EventPlanner",null);  // avvia superagente EventPlanner
    }

    private void readArgs(Object[] args) {
        nPeople = Integer.parseInt((String) args[0]);
        diseaseDistribution[Person.SEIR.SUSCEPTIBLE.ordinal()] = Double.parseDouble((String) args[1]);
        diseaseDistribution[Person.SEIR.EXPOSED.ordinal()] = Double.parseDouble((String) args[2]);
        diseaseDistribution[Person.SEIR.INFECTIOUS.ordinal()] = Double.parseDouble((String) args[3]);
        diseaseDistribution[Person.SEIR.RECOVERED.ordinal()] = Double.parseDouble((String) args[4]);
        naughtyProb = Double.parseDouble((String) args[5]);
        workerProb = Double.parseDouble((String) args[6]);
        mapSize = Integer.parseInt((String) args[7]);
        nBusiness = Integer.parseInt((String) args[8]);
        beds = Integer.parseInt((String) args[9]);
    }

    private void startAgent(String nickname, String classname, Object[] args) {
        ContainerController c = getContainerController();
        try {
            AgentController a = c.createNewAgent(
                    nickname, classname, args
            );
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
