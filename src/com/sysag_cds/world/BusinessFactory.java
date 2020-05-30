package com.sysag_cds.world;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class BusinessFactory {

    protected int count = 0;

    RandomBuilding bp;
    RandomBusiness cp;

    ContainerController c;
    Object[] businessArgs = new Object[3];

    BusinessFactory(Agent creator, RandomBuilding bp, RandomBusiness cp) {
        c = creator.getContainerController();
        this.bp = bp;
        this.cp = cp;
    }

    public void create() {
        businessArgs[0] = cp.getRandomCategory();
        businessArgs[1] = bp.getRandomBuildingAndRemove();
        businessArgs[2] = Double.toString(0.5 + Math.random() * .5);

        try {
            AgentController a = c.createNewAgent(
                    "bus" + count++, "com.sysag_cds.world.Business", businessArgs
            );
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
