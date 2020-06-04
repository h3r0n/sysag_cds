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
 * Classe, la cui unica instanza (singleton) rappresenta la mappa geografica in cui si svolge la simulazione.
 * Offre metodi per il pathfinding.
 */
public class World {

    private Graph<Building, Road> map;
    private List<Building> buildingList;
    private static World instance = null;
    private DijkstraShortestPath<Building,Road> pathFinder;

    private World(int mapSize) {
        buildMap(mapSize);
        pathFinder = new DijkstraShortestPath<>(map);
        pathFinder.enableCaching(true);
    }

    public static World getInstance(int mapSize) {
        if (instance == null) {
            instance= new World(mapSize);
        }
        return instance;
    }

    public static World getInstance() {
        return instance;
    }

    private void buildMap(int mapSize) {

        BuildingFactory bf = new BuildingFactory();

        map = (
                new Lattice2DGenerator<>(UndirectedSparseGraph.getFactory(),
                        bf,
                        new RoadFactory(),
                        mapSize, false
                )
        ).get();

        buildingList = bf.getList();

        if (Simulation.debug) {
            System.out.println(map.toString());
            System.out.println("Vertices=Building, Edges=Roads");
        }
    }

    public List<Building> getBuildings() {
        return buildingList;
    }

    public synchronized List<Road> getPath(Building begin, Building end) {
        if (begin.equals(end))
            return null;
        return pathFinder.getPath(begin, end);
    }

    public synchronized int getDistance(Building begin, Building end) {
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
        if (kNeighbor != null) {
            kNeighbor.removeVertex(begin);
        } else
            return null;

        int tries = 1;

        Building destination = randomBuilding(kNeighbor);
        while ((destination == null || destination.equals(begin)) && tries <10) {
            destination = randomBuilding(kNeighbor);
            tries++;
        }

        if (destination == null || destination.equals(begin))
            return null;

        List<Road> path = getPath(begin,destination);   // andata
        if (path == null)
            return null;
        List<Road> rPath = new ArrayList<>(path);
        Collections.reverse(rPath);     // ritorno

        Stream<Road> stream = Stream.of();
        stream = Stream.concat(stream, path.stream());
        stream = Stream.concat(stream, rPath.stream());

        return stream.collect(Collectors.toList());
    }

    public Building findBuilding(Building b) {
        return buildingList.stream()
                .filter(building -> building.equals(b))
                .findAny().orElse(null);
    }
}
