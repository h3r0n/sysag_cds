package com.sysag_cds.people;

import com.sysag_cds.superagents.Simulation;
import com.sysag_cds.utility.BooleanProbability;
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

public class Worker extends Person {

    Building workplace = null;
    int workTicks = 10;
    int workInterval = 100;
    boolean working = false;
    double workContagion = 0.015;

    protected void setup() {
        super.setup();

        Object[] args = this.getArguments();

        // il quarto argomento specifica il luogo di lavoro
        if (args.length >= 4) {
            workplace = World.getInstance().findBuilding(new Building((String) args[3]));
        }

        addBehaviour(new TickerBehaviour(this, Simulation.tick * workInterval) {
            protected void onTick() {
                work();
            }
        });

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
                                task.addSubBehaviour(new WaitingTask(myAgent, Simulation.tick * workInterval));
                                task.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        working = false;
                                    }
                                });
                                task.addSubBehaviour(new TravelTask(myAgent, home));
                            }
                        }
                    });
                    scheduleTask(task);
                }
            }
        });
    }

    void work() {
        scheduleTask(new TravelTask(this, workplace));
        scheduleTask(new WaitingTask(this, Simulation.tick * workTicks));
        scheduleTask(new TravelTask(this, home));
    }

    boolean isOpen(Building building) {
        boolean open = false;

        // search template
        DFAgentDescription dfdt = new DFAgentDescription();
        ServiceDescription sdt = new ServiceDescription();
        if (!naughty)
            sdt.addProperties(new Property("Open", "True"));
        sdt.addProperties(new Property("Location", building));
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
        if (position.equals(workplace))
            return BooleanProbability.getBoolean(workContagion);
        else
            return super.contagion(infectiousDPI);
    }
}
