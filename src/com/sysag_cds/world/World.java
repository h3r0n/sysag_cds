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
 * A Class with a single instance (singleton) that represents a map in which the simulation takes place.<br>
 * Has several path finding methods.
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

    /**
     * Gets instance.
     *
     * @param mapSize the map size
     * @return the World instance
     */
    public static World getInstance(int mapSize) {
        if (instance == null) {
            instance= new World(mapSize);
        }
        return instance;
    }

    /**
     * Gets instance.
     *
     * @return the World instance
     */
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

    /**
     * Gets buildings list.
     *
     * @return the buildings list
     */
    public List<Building> getBuildings() {
        return buildingList;
    }

    /**
     * Gets the path between two buildings.
     *
     * @param begin the begin
     * @param end   the end
     * @return the path
     */
    public synchronized List<Road> getPath(Building begin, Building end) {
        if (begin.equals(end))
            return null;
        return pathFinder.getPath(begin, end);
    }

    /**
     * Gets the distance between two buildings.
     *
     * @param begin the begin
     * @param end   the end
     * @return the distance
     */
    public synchronized int getDistance(Building begin, Building end) {
        return pathFinder.getDistance(begin, end).intValue();
    }

    /**
     * Returns a random building given the map
     *
     * @param m the map
     * @return the building
     */
    public static Building randomBuilding(Graph<Building,Road> m) {
        Collection<Building> buildings =  m.getVertices();
        return buildings.stream()
                .skip((int) (buildings.size() * Math.random()))
                .findFirst().orElse(null);
    }

    /**
     * Random building.
     *
     * @return the building
     */
    public Building randomBuilding() {
        return buildingList.stream()
                .skip((int) (buildingList.size() * Math.random()))
                .findFirst().orElse(null);
    }

    /**
     * Returns a random walk.
     *
     * @param begin       the begin
     * @param maxDistance the max distance
     * @return the random walk
     */
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

    /**
     * Finds the building instance inside the map.
     *
     * @param b the building searched
     * @return the building found
     */
    public Building findBuilding(Building b) {
        return buildingList.stream()
                .filter(building -> building.equals(b))
                .findAny().orElse(null);
    }
}
