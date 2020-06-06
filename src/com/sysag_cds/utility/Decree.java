package com.sysag_cds.utility;

/**
 * Specifica le norme sul distanziamento sociale, la chiusura di Business e l'obbligo di indossare DPI.
 */
public class Decree {

    public enum LAW {
        ALWAYS,
        INDOOR,
        NEVER
    }

    private int decreeNumber = 1;
    private int WalkDistance = 10;
    private int maxTravel = 100;
    private boolean parkOpen = true;
    private boolean nonEssentialOpen = true;
    private boolean eventOpen = true;
    private LAW maskRequired = LAW.NEVER;
    private double density = 1;

    public boolean getEventOpen() {
        return eventOpen;
    }

    public void setEventOpen(boolean eventOpen) {
        this.eventOpen = eventOpen;
    }

    public int getDecreeNumber() {
        return decreeNumber;
    }

    public void setDecreeNumber(int DecreeNumber) {
         decreeNumber=DecreeNumber;
    }

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

    public boolean getParkOpen() {
        return parkOpen;
    }

    public void setParkOpen(boolean parkOpen) {
        this.parkOpen = parkOpen;
    }

    public boolean getNonEssentialOpen() {
        return nonEssentialOpen;
    }

    public void setNonEssentialOpen(boolean nonEssentialOpen) {
        this.nonEssentialOpen = nonEssentialOpen;
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

    public LAW parseString(String s){
        LAW result = null;
        if(s.equals("ALWAYS"))
            result=LAW.ALWAYS;
        if(s.equals("INDOOR"))
            result=LAW.INDOOR;
        if(s.equals("NEVER"))
            result=LAW.NEVER;
        return result;
    }
}
