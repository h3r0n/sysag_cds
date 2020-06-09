package com.sysag_cds.world;

import java.util.Objects;

/**
 * A place in the map
 */
public class Location {
    protected String location;
    protected Double density = .0;

    /**
     * Instantiates a new Location.
     *
     * @param l the location string
     */
    public Location(String l) {
        location = l;
    }

    /**
     * Get location string.
     *
     * @return the location string
     */
    public String getLocation(){ return location; }

    /**
     * Set location.
     *
     * @param loc the location
     */
    public void setLocation(String loc){ location=loc;}

    /**
     * Gets density.
     *
     * @return the density
     */
    public Double getDensity() {
        return density;
    }

    /**
     * Sets density.
     *
     * @param d the density
     */
    public void setDensity(Double d) {
        density=d;
    }

    @Override
    public String toString() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location1 = (Location) o;
        return location.equals(location1.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
