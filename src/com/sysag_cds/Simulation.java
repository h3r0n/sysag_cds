package com.sysag_cds;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.graph.*;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.LinkedList;
import java.util.List;

public class Simulation extends Agent {
    public static int tick = 1000;
    public static boolean debug = true;

    protected void setup()  {
        ContainerController c = getContainerController();
        AgentController a;

        Object [] args = new Object[1];


        try {
            args[0] = "infectious";
            a = c.createNewAgent( "contagioso", "com.sysag_cds.Person", args);
            a.start();

            Thread.sleep(1000);

            args[0] = "susceptible";
            a = c.createNewAgent( "sano", "com.sysag_cds.Person", args);
            a.start();

        } catch (StaleProxyException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        List<Building> list = new LinkedList<>();

        Graph<Building,Road> map = (
                new Lattice2DGenerator<Building, Road>(UndirectedSparseGraph.<Building, Road>getFactory(),
                        new BuildingFactory(list),
                        new RoadFactory(),
                        10, false
                )
        ).get();

        System.out.println(map.toString());
        System.out.println(list.toString());
    }
}
