package com.sysag_cds.world;

import com.sysag_cds.Simulation;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe, la cui unica instanza (singleton) rappresenta la mappa geografica in cui si svolge la simulazione
 */
public class World {

    private Graph<Building, Road> map;
    private List<Building> buildingList = new LinkedList<>();
    private static World instance = null;
    private DijkstraShortestPath<Building,Road> pathFinder;

    private World(int mapSize) {
        buildMap(mapSize);
        pathFinder = new DijkstraShortestPath<>(map);
        pathFinder.enableCaching(true);
    }

    public static World getInstance(int mapSize) {
        if (instance == null) {
            return new World(mapSize);
        } else {
            return instance;
        }
    }

    public static World getInstance() {
        return instance;
    }

    private void buildMap(int mapSize) {

        map = (
                new Lattice2DGenerator<>(UndirectedSparseGraph.getFactory(),
                        new BuildingFactory(buildingList),
                        new RoadFactory(),
                        mapSize, false
                )
        ).get();

        if (Simulation.debug) {
            System.out.println(map.toString());
            System.out.println("Vertices=Building, Edges=Roads");
        }
    }

    public List<Building> getBuildings() {
        return buildingList;
    }

    public List<Road> getPath(Building begin, Building end) {
        return pathFinder.getPath(begin, end);
    }

    public int getDistance(Building begin, Building end) {
        return pathFinder.getDistance(begin, end).intValue();
    }
}
