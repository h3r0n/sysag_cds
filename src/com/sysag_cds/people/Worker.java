package com.sysag_cds.people;

import com.sysag_cds.superagents.Simulation;
import com.sysag_cds.utility.BooleanProbability;
import com.sysag_cds.utility.Decree;
import com.sysag_cds.world.Building;
import com.sysag_cds.world.World;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.util.leap.Iterator;

/**
 * The Worker Agent is a more specific kind of Person Agent who has an own work place,with low contagion probability, to which he goes periodically
 *
 */
public class Worker extends Person {

    Building workplace = null;
    int workTicks = 30;
    int workInterval = 60;
    boolean working = false;
    double workContagion = 0.015;

    protected void setup() {
        super.setup();

        Object[] args = this.getArguments();

        // il quarto argomento specifica il luogo di lavoro
        if (args.length >= 4) {
            workplace = World.getInstance().findBuilding(new Building((String) args[3]));
        }

        System.out.println(this.getLocalName()+" works in "+ workplace.toString());

        addBehaviour(new TickerBehaviour(this, Simulation.tick * workInterval) {
            protected void onTick() {
                if (!working) {
                    working = true;
                    SequentialBehaviour task = new SequentialBehaviour();
                    task.addSubBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            if (workplace != null && isOpen(workplace)) {
                                task.addSubBehaviour(new TravelTask(myAgent, workplace));
                                task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * workTicks));
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        working = false;
                                        if (Simulation.debug)
                                            System.out.println(getLocalName()+" is coming back home from work.");
                                    }
                                });
                                task.addSubBehaviour(new TravelTask(myAgent, home));
                            }else{
                                working=false;
                                if (Simulation.debug)
                                    System.out.println(getLocalName()+" cannot go to work.");
                            }
                        }
                    });
                    scheduleTask(task);
                    if (Simulation.debug)
                        System.out.println(getLocalName()+" has to go to work.");
                }
            }
        });
    }

    /**
     * Is the Building building open?
     *
     * @param building the building
     * @return the boolean is true if it is open, false otherwise
     */
    boolean isOpen(Building building) {
        boolean open = false;

        // search template
        DFAgentDescription dfdt = new DFAgentDescription();
        ServiceDescription sdt = new ServiceDescription();
        if (!naughty)
            sdt.addProperties(new Property("Open", true));
        sdt.addProperties(new Property("Location", building.getLocation()));
        dfdt.addServices(sdt);

        // search
        DFAgentDescription[] dfds = new DFAgentDescription[0];
        try {
            dfds = DFService.search(this, dfdt);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // get results
        for (DFAgentDescription dfd : dfds) {
            Iterator allServices = dfd.getAllServices();
            while (allServices.hasNext()) {
                ServiceDescription sd = (ServiceDescription) allServices.next();
                open = true;
            }
        }

        return open;
    }

    @Override
    boolean contagion(boolean infectiousDPI) {
        if (position.equals(workplace) &&
                (currentDecree.getMaskRequired()==Decree.LAW.ALWAYS
                        || currentDecree.getMaskRequired()==Decree.LAW.INDOOR
                )
        )
            return BooleanProbability.getBoolean(workContagion);
        else
            return super.contagion(infectiousDPI);
    }
}
