package com.sysag_cds.people;

import com.sysag_cds.map.RandomBuilding;
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
        personArgs[0] = naughtyProb.getRandomNaughty();    // assegna uno stato di salute casuale
        personArgs[1] = buildingProb.getRandomBuilding();   // assegna una casa casuale
        personArgs[2] = naughtyProb.getRandomNaughty();     // assegna una coscienziosità casuale

        try {
            AgentController a = c.createNewAgent("p" + count++, "com.sysag_cds.people.Person", personArgs);
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
