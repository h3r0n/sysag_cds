package com.sysag_cds.utility;

/**
 * The Decree class defines rules concerning social distancing, Business closing status, and DPI/PPE status
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

    /**
     * Gets if the events are open.
     *
     * @return true if they are, false otherwise
     */
    public boolean getEventOpen() {
        return eventOpen;
    }

    /**
     * Sets if the events are open.
     *
     * @param eventOpen true if they are, false otherwise
     */
    public void setEventOpen(boolean eventOpen) {
        this.eventOpen = eventOpen;
    }

    /**
     * Gets decree number.
     *
     * @return the decree number
     */
    public int getDecreeNumber() {
        return decreeNumber;
    }

    /**
     * Sets decree number.
     *
     * @param DecreeNumber the decree number
     */
    public void setDecreeNumber(int DecreeNumber) {
         decreeNumber=DecreeNumber;
    }

    /**
     * Gets walk distance.
     *
     * @return the walk distance
     */
    public int getWalkDistance() {
        return WalkDistance;
    }

    /**
     * Sets walk distance.
     *
     * @param walkDistance the walk distance
     */
    public void setWalkDistance(int walkDistance) {
        WalkDistance = walkDistance;
    }

    /**
     * Gets max travel distance.
     *
     * @return the max travel distance
     */
    public int getMaxTravel() {
        return maxTravel;
    }

    /**
     * Sets max travel distance.
     *
     * @param maxTravel the max travel distance
     */
    public void setMaxTravel(int maxTravel) {
        this.maxTravel = maxTravel;
    }

    /**
     * Gets park opening.
     *
     * @return the park opening
     */
    public boolean getParkOpen() {
        return parkOpen;
    }

    /**
     * Sets park opening.
     *
     * @param parkOpen the park opening
     */
    public void setParkOpen(boolean parkOpen) {
        this.parkOpen = parkOpen;
    }

    /**
     * Gets non essential opening.
     *
     * @return the non essential opening
     */
    public boolean getNonEssentialOpen() {
        return nonEssentialOpen;
    }

    /**
     * Sets non essential opening.
     *
     * @param nonEssentialOpen the non essential opening
     */
    public void setNonEssentialOpen(boolean nonEssentialOpen) {
        this.nonEssentialOpen = nonEssentialOpen;
    }

    /**
     * Gets mask required.
     *
     * @return the mask required
     */
    public LAW getMaskRequired() {
        return maskRequired;
    }

    /**
     * Sets mask required.
     *
     * @param maskRequired the mask required
     */
    public void setMaskRequired(LAW maskRequired) {
        this.maskRequired = maskRequired;
    }

    /**
     * Gets density.
     *
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * Sets density.
     *
     * @param density the density
     */
    public void setDensity(double density) {
        this.density = density;
    }

    /**
     * Parse string law.
     *
     * @param s the string
     * @return the law enum
     */
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
