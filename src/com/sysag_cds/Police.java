package com.sysag_cds;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Police extends Agent {

    static double costo = 0.5;

    Location position;
    Location stazione;

    protected void setup() {
        position = stazione;
        //Object[] args=this.getArguments();
        /*
        addBehaviour(new TickerBehaviour(this, 20000) {
            protected void onTick() {
                position=selectPosition();
            }
        } );
         */
        Location lavoro=new Location();
        this.goToLocation(lavoro);
    }

    void goToLocation(Location l) {
        position = l;
        position.posizione_nodo=0;
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( getAID() );
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( "Multe" );
            sd.addProperties(new Property("COSTO",costo)); //proprietà id nodo
            sd.setName(String.valueOf(position.posizione_nodo));
            dfd.addServices(sd);

            try {
                DFService.register(this, dfd );
            }
            catch (FIPAException fe) { fe.printStackTrace(); }

            //controlli();

    }

   /* private void controlli() {
        addBehaviour(new WakerBehaviour(this, Simulation.tick*beta) {
            @Override
            protected void onWake() {
                System.out.println(myAgent.getLocalName()+" esce di lavoro");
                try {
                    DFService.deregister(this.myAgent);
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                DPI= (float) (DPI-0.1);
            }
        });
    }
*/
   Location selectPosition(){
       return stazione;
   }
}
