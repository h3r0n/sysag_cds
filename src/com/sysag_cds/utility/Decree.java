package com.sysag_cds.utility;

public class Decree {

    public enum LAW {
        ALWAYS,
        INDOOR,
        NEVER
    }

    private int WalkDistance = 10;
    private int maxTravel = 10;
    private LAW maskRequired = LAW.NEVER;
    private double density = 1;

    public int getWalkDistance() {
        return WalkDistance;
    }

    public void setWalkDistance(int walkDistance) {
        WalkDistance = walkDistance;
    }

    public int getMaxTravel() {
        return maxTravel;
    }

    public void setMaxTravel(int maxTravel) {
        this.maxTravel = maxTravel;
    }

    public LAW getMaskRequired() {
        return maskRequired;
    }

    public void setMaskRequired(LAW maskRequired) {
        this.maskRequired = maskRequired;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }
}
