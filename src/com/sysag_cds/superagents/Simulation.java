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
 * L'agente Simulation crea gli altri agenti e avvia la simulazione.
 *
 * Parametri di Simulation:
 *     [0] numero di Person
 *     [2-4] probabilità di creare persone SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED
 *     [5] probabilità di creare persone non coscienziose (tra 0 e 1)
 *     [6] probabilità di creare lavoratori
 *     [7] numero di Building per lato, considerando che la mappa è un quadrato. Deve essere >= 2
 *     [8] numero di Business
 *     [9] numero di posti letto
 *
 * Esempio: -agents simulation:com.sysag_cds.superagents.Simulation(100,.9,0,.1,0,.2,.5,15,20,5)
 *     Crea 100 Person, con il 90% di probabilità di essere SUSCEPTIBLE e il 10% di essere INFECTIOUS
 *     hanno il 20% di probabilità di essere incoscienti e il 50% di essere lavoratori
 *     Crea poi una mappa con 225 edifici, in cui risiedono 20 Business
 *     Tra tutti gli ospedali i posti letto sono 5
 */
public class Simulation extends Agent {
    public static int tick = 100;
    public static int day = 60;
    public static boolean debug = true;

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
