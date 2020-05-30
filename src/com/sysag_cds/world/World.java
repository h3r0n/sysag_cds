package com.sysag_cds.world;

import com.sysag_cds.superagents.Simulation;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (begin.equals(end))
            return null;
        return pathFinder.getPath(begin, end);
    }

    public int getDistance(Building begin, Building end) {
        return pathFinder.getDistance(begin, end).intValue();
    }

    public static Building randomBuilding(Graph<Building,Road> m) {
        Collection<Building> buildings =  m.getVertices();
        return buildings.stream()
                .skip((int) (buildings.size() * Math.random()))
                .findFirst().orElse(null);
    }

    public Building randomBuilding() {
        return buildingList.stream()
                .skip((int) (buildingList.size() * Math.random()))
                .findFirst().orElse(null);
    }

    public List<Road> getRandomWalk(Building begin, int maxDistance) {

        Graph<Building,Road> kNeighbor = new KNeighborhoodFilter<Building,Road>(
                begin,maxDistance,KNeighborhoodFilter.EdgeType.IN_OUT
        ).apply(map);

        if (kNeighbor==null)
            return null;

        int tries = 1;
        Building destination = randomBuilding(kNeighbor);
        while ((destination == null || destination.equals(begin)) && tries <10) {
            destination = randomBuilding(kNeighbor);
            tries++;
        }

        if (destination == null || destination.equals(begin))
            return null;

        Stream<Road> stream = Stream.of();
        stream = Stream.concat(stream, getPath(begin,destination).stream());
        stream = Stream.concat(stream, getPath(destination,begin).stream());

        return stream.collect(Collectors.toList());
    }
}
