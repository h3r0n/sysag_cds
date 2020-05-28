package com.sysag_cds.map;

import java.util.Objects;

public class Location {
    protected String location;

    public Location(String l) {
        location = l;
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
