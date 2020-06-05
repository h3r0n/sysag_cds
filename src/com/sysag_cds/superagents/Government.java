package com.sysag_cds.superagents;

import com.sysag_cds.utility.Decree;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;

/**
 * L'agente Government può emanare decreti riguardo il distanziamento sociale, la chiusura di Business
 * e l'obbligo di indossare DPI.
 */
public class Government extends GuiAgent {

    Decree currentDecree = new Decree();

    @Override
    protected void setup() {
        registerService();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new GovGui(this);
    }

    public void updateDecree(Decree newDecree) {
        currentDecree = newDecree;
        updateService();
    }

    private DFAgentDescription createService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Government");
        sd.setName(getLocalName());
        sd.addProperties(new Property("decreeNumber", currentDecree.getDecreeNumber()));
        sd.addProperties(new Property("walkDistance",currentDecree.getWalkDistance()));
        sd.addProperties(new Property("maxTravel", currentDecree.getMaxTravel()));
        sd.addProperties(new Property("parkOpen",currentDecree.getParkOpen()));
        sd.addProperties(new Property("maskRequired",currentDecree.getMaskRequired()));
        sd.addProperties(new Property("density",currentDecree.getDensity()));
        dfd.addServices(sd);

        return dfd;
    }

    private void registerService() {
        try {
            DFService.register(this, createService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void updateService() {
        try {
            DFService.modify(this, createService());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        Decree d = new Decree();
        switch ((int) ev.getParameter(0)) {
            case 0:
                d.setMaskRequired(Decree.LAW.NEVER);
                break;
            case 1:
                d.setMaskRequired(Decree.LAW.INDOOR);
                break;
            case 2:
                d.setMaskRequired(Decree.LAW.ALWAYS);
                break;
        }
        switch ((int) ev.getParameter(1)) {
            case 0:
                d.setParkOpen(true);
                break;
            case 1:
                d.setParkOpen(false);
                break;
        }
        d.setDensity((double) ev.getParameter(2));
        d.setWalkDistance((int) ev.getParameter(3));
        //System.out.println("Obbligo Mascherina= "+d.getMaskRequired().toString()+" Parchi Aperti= "+d.getParkOpen()+" Distanziamento= "+d.getDensity()+" Distanza Passeggiata="+d.getWalkDistance()+" ");
        updateDecree(d);
    }
}
