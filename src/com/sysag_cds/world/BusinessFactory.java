package com.sysag_cds.world;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.LinkedList;
import java.util.List;

public class BusinessFactory {

    protected int count = 0;
    List<Building> list = new LinkedList<>();

    RandomBuilding bp;
    RandomBusiness cp;

    ContainerController c;
    Object[] businessArgs = new Object[3];

    public BusinessFactory(Agent creator, RandomBuilding bp, RandomBusiness cp) {
        c = creator.getContainerController();
        this.bp = bp;
        this.cp = cp;
    }

    public void create() {
        Building b = bp.getRandomBuildingAndRemove();
        list.add(b);

        businessArgs[0] = cp.getRandomCategory();
        businessArgs[1] = b.toString();
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

    public List<Building> getList() {
        return list;
    }
}
