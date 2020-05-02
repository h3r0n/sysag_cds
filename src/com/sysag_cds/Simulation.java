package com.sysag_cds;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.graph.*;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Simulation extends Agent {
    public static int tick = 1000;
    public static boolean debug = true;

    protected void setup()  {
        ContainerController c = getContainerController();
        AgentController a;

        Object [] args = new Object[1];
        args[0] = "infectious";

        try {

            a = c.createNewAgent( "contagioso", "com.sysag_cds.Person", args);
            a.start();

            Thread.sleep(1000);

            a = c.createNewAgent( "sano", "com.sysag_cds.Person", null);
            a.start();

        } catch (StaleProxyException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Graph<Building,Road> map = (
                new Lattice2DGenerator<Building, Road>(UndirectedSparseGraph.<Building, Road>getFactory(),
                        new BuildingFactory(),
                        new RoadFactory(),
                        10, false
                )
        ).get();

        System.out.println(map.toString());
    }
}
