package com.sysag_cds.map;

import com.sysag_cds.Simulation;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class World {

    private Graph<Building, Road> map;
    List<Building> buildingList = new LinkedList<>();
    private static World instance = null;

    private World(int mapSize) {
        buildMap(mapSize);
    }

    public static World getInstance(int mapSize) {
        if (instance == null) {
            return new World(mapSize);
        } else {
            return instance;
        }
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

    String getPath(String begin, String end) {
        List<Road> list = (new DijkstraShortestPath<>(map)).getPath(new Building(begin), new Building(end));
        Iterator<Road> iterator = list.iterator();
        StringBuilder path = new StringBuilder();

        if (iterator.hasNext())
            path.append(iterator.next());
        while (iterator.hasNext()) {
            path.append(",");
            path.append(iterator.next().toString());
        }
        return path.toString();
    }
}
