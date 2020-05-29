package com.sysag_cds.people;

import com.sysag_cds.world.RandomBuilding;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class PersonFactory {

    protected int count = 0;

    RandomSEIR seirProb;
    RandomNaughty naughtyProb;
    RandomBuilding buildingProb;

    ContainerController c;
    Object[] personArgs = new Object[3];

    public PersonFactory(Agent creator, RandomBuilding bp, RandomSEIR sp, RandomNaughty np) {
        c = creator.getContainerController();
        buildingProb = bp;
        seirProb = sp;
        naughtyProb = np;
    }

    public void create() {
        personArgs[0] = seirProb.getRandomStatus();    // assegna uno stato di salute casuale
        personArgs[1] = buildingProb.getRandomBuilding().toString();   // assegna una casa casuale
        personArgs[2] = naughtyProb.getRandomNaughty();     // assegna una coscienziosità casuale

        try {
            AgentController a = c.createNewAgent("p" + count++, "com.sysag_cds.people.Person", personArgs);
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
