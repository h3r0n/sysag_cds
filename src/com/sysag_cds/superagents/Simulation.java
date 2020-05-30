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

public class Simulation extends Agent {
    public static int tick = 1000;
    public static boolean debug = true;

    private int nPeople;
    private int nBusiness;
    private double[] diseaseDistribution = new double[4];
    private int mapSize;
    private double naughtyProb;

    RandomBuilding randomBuilding = new RandomBuilding();

    protected void setup() {
        startGovernment();
        startStatistics();

        readArgs(getArguments());
        World.getInstance(mapSize);
        createBusinesses();
        createPeople();
    }

    /*
        Parametri di Simulation:
            [0] numero di Person
            [2-4] probabilità di creare persone SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED
            [5] probabilità di creare persone non coscienziose (tra 0 e 1)
            [6] numero di Building per lato, considerando che la mappa è un quadrato. Deve essere >= 2
            [7] numero di Business

        Esempio: -agents simulation:com.sysag_cds.superagents.Simulation(1,.5,0,.5,0,0.1,2)
            Crea 1 agente Person, con il 50% di probabilità di essere SUSCEPTIBLE e il 50% di essere INFECTIOUS
            Crea poi una mappa con 4 edifici

     */
    private void readArgs(Object[] args) {
        nPeople = Integer.parseInt((String) args[0]);
        diseaseDistribution[Person.SEIR.SUSCEPTIBLE.ordinal()] = Double.parseDouble((String) args[1]);
        diseaseDistribution[Person.SEIR.EXPOSED.ordinal()] = Double.parseDouble((String) args[2]);
        diseaseDistribution[Person.SEIR.INFECTIOUS.ordinal()] = Double.parseDouble((String) args[3]);
        diseaseDistribution[Person.SEIR.RECOVERED.ordinal()] = Double.parseDouble((String) args[4]);
        naughtyProb = Double.parseDouble((String) args[5]);
        mapSize = Integer.parseInt((String) args[6]);
        nBusiness = Integer.parseInt((String) args[7]);
    }

    private void createPeople() {

        PersonFactory pf = new PersonFactory(
                this,
                randomBuilding,
                new RandomSEIR(
                        diseaseDistribution[0],
                        diseaseDistribution[1],
                        diseaseDistribution[2],
                        diseaseDistribution[3]
                ),
                naughtyProb
        );

        for (int i = 0; i < nPeople; i++)
            pf.create();
    }

    private void createBusinesses() {
        BusinessFactory bf = new BusinessFactory(
                this,
                randomBuilding,
                new RandomBusiness(new double[]{.1, .2, .3})    // todo
        );

        for (int i = 0; i < nBusiness; i++)
            bf.create();
    }

    private void startStatistics() {
        ContainerController c = getContainerController();
        try {
            AgentController a = c.createNewAgent(
                    "Statistics", "com.sysag_cds.superagents.Statistics", null
            );
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void startGovernment() {
        ContainerController c = getContainerController();
        try {
            AgentController a = c.createNewAgent(
                    "Government", "com.sysag_cds.superagents.Government", null
            );
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
