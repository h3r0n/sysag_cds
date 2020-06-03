package com.sysag_cds.world;

import java.util.Objects;

/**
 * Un indirizzo generico.
 */
public class Location {
    protected String location;
    protected Double density = .0;

    public Location(String l) {
        location = l;
    }

    public String getLocation(){ return location; }

    public void setLocation(String loc){ location=loc;}

    public Double getDensity() {
        return density;
    }

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
