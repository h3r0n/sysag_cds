package com.sysag_cds.people;

import com.sysag_cds.utility.BooleanProbability;
import com.sysag_cds.world.RandomBuilding;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Factory class that generates Person agents in the current container, giving them an home,(if worker) a job, a naughty status and a SEIR status.
 *
 */
public class PersonFactory {

    protected int count = 0;

    RandomSEIR seirProb;
    double naughtyProb;
    double workingProb;
    RandomBuilding buildingProb;
    RandomBuilding wpProb;

    ContainerController c;
    Object[] personArgs = new Object[3];
    Object[] workerArgs = new Object[4];

    /**
     * Instantiates a new Person factory.
     *
     * @param creator the creator which creates the person factory instance
     * @param bp      the Random Building bp instance for houses
     * @param wpp     the Random Building wpp instance for working places
     * @param sp      the Random SEIR sp instance for SEIR probabilities
     * @param np      the naughty probability np
     * @param wp      the working probability wp
     */
    public PersonFactory(Agent creator, RandomBuilding bp, RandomBuilding wpp, RandomSEIR sp, double np, double wp) {
        c = creator.getContainerController();
        buildingProb = bp;
        seirProb = sp;
        naughtyProb = np;
        workingProb = wp;
        wpProb = wpp;
    }

    /**
     * Creates a Person agent or a Worker agent.
     */
    public void create() {
        if (BooleanProbability.getBoolean(workingProb)) {

            workerArgs[0] = seirProb.getRandomStatus();    // assegna uno stato di salute casuale
            workerArgs[1] = buildingProb.getRandomBuilding().toString();   // assegna una casa casuale
            workerArgs[2] = BooleanProbability.getTrueFalse(naughtyProb);    // assegna una coscienziosità casuale
            workerArgs[3] = wpProb.getRandomBuilding().toString(); // assegna un posto di lavoro casuale

            try {
                AgentController a = c.createNewAgent("p" + count++, "com.sysag_cds.people.Worker", workerArgs.clone());
                a.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        } else {

            personArgs[0] = seirProb.getRandomStatus();    // assegna uno stato di salute casuale
            personArgs[1] = buildingProb.getRandomBuilding().toString();   // assegna una casa casuale
            personArgs[2] = BooleanProbability.getTrueFalse(naughtyProb);    // assegna una coscienziosità casuale

            try {
                AgentController a = c.createNewAgent("p" + count++, "com.sysag_cds.people.Person", personArgs.clone());
                a.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
