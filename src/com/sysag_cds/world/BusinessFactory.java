package com.sysag_cds.world;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.LinkedList;
import java.util.List;

/**
 * Factory Class that creates Business Agents in the current container in different buildings.<br>
 * It keeps a list of used locations.
 */
public class BusinessFactory {

    protected int count = 0;
    List<Building> list = new LinkedList<>();

    RandomBuilding bp;
    RandomBusiness cp;

    ContainerController c;
    Object[] businessArgs = new Object[3];

    /**
     * Instantiates a new Business factory.
     *
     * @param creator the creator
     * @param bp      the Random Building Agent bp
     * @param cp      the Random Business Agent cp
     */
    public BusinessFactory(Agent creator, RandomBuilding bp, RandomBusiness cp) {
        c = creator.getContainerController();
        this.bp = bp;
        this.cp = cp;
    }

    /**
     * Creates a Business.
     */
    public void create() {
        Building b = bp.getRandomBuildingAndRemove();
        list.add(b);

        businessArgs[0] = cp.getRandomCategory();
        businessArgs[1] = b.toString();
        if (businessArgs[0].equals("Park")) {
            businessArgs[2] = Double.toString(0.1 + Math.random() * .9);
        } else {
            businessArgs[2] = Double.toString(0.5 + Math.random() * .5);
        }

        try {
            AgentController a = c.createNewAgent(
                    "bus" + count++, "com.sysag_cds.world.Business", businessArgs.clone()
            );
            a.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets Business list.
     *
     * @return the list
     */
    public List<Building> getList() {
        return list;
    }
}
