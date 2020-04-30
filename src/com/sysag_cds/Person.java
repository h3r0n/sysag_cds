package com.sysag_cds;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Service;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class Person extends Agent {

    enum SEIR {
        susceptible,
        exposed,
        infectious,
        recovered
    }

    float DPI= (float) Math.random();
    float primary= (float) Math.random();

    static int beta = 10;
    static int delta = 2;
    static int gamma = 2;

    SEIR disease = SEIR.susceptible;

    Location position;
    Location home;

    public boolean isAtHome() {
        return this.position == this.home;
    }

    protected void setup() {
        position = home;
        Object[] args=this.getArguments();
        if(args[0].equals("infetto")){
            this.disease=SEIR.infectious;
            //addBehaviour(new waitSusceptible());
        }else{
            addBehaviour(new waitInfectious());
        }
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                primary= (float) (primary-0.1);
            }
        } );
        Location lavoro=new Location();
        this.goToLocation(lavoro);
        System.out.println(this.DPI+" "+this.primary);

    }

    void setExposed() {
        disease = SEIR.exposed;

        addBehaviour(new WakerBehaviour(this, Simulation.tick*delta) {
            @Override
            protected void onWake() {
                System.out.println(myAgent.getLocalName()+" infectious");
                setInfectious();
            }
        });
    }

    void setInfectious() {
        disease = SEIR.infectious;

        addBehaviour(new WakerBehaviour(this, Simulation.tick*gamma) {
            @Override
            protected void onWake() {
                System.out.println(myAgent.getLocalName()+" recovered");
                setRecovered();
            }
        });
    }

    void setRecovered() {
        disease = SEIR.recovered;
    }

    void goToLocation(Location l) {
        position = l;
        if(this.disease==SEIR.infectious){
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( getAID() );
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( "Contagio" );
            sd.addProperties(new Property("DPI",0.5)); //proprietà id nodo
            sd.setName(String.valueOf(l.posizione_nodo));
            dfd.addServices(sd);

            try {
                DFService.register(this, dfd );
            }
            catch (FIPAException fe) { fe.printStackTrace(); }

            lavora();

        }else{
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sds  = new ServiceDescription();
            sds.setType( "Contagio" );
            sds.setName(String.valueOf(l.posizione_nodo));
            template.addServices(sds);
            addBehaviour( new SubscriptionInitiator( this,
                            DFService.createSubscriptionMessage( this, getDefaultDF(),
                                    template, null))
                    {
                        protected void handleInform(ACLMessage inform) {
                            /*
                            try {
                                                             //chiamare la funzione di calcolo del contagio
                            }
                            catch (FIPAException fe) {fe.printStackTrace(); }
                        */
                        }
                    });
        }

    }

    private void lavora() {
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

    /*
        class waitSusceptible extends Behaviour{

            @Override
            public void action() {
                ACLMessage msg= myAgent.receive();
                if(msg!=null){
                    AID contagiabile=msg.getSender();
                    //String contenuto=msg.getContent();
                    ACLMessage messaggio_contagio= new ACLMessage(ACLMessage.PROPOSE);
                    messaggio_contagio.addReceiver(contagiabile);
                    messaggio_contagio.setContent("DPI");
                    send(messaggio_contagio);
                }
                block();
            }

            @Override
            public boolean done() {
                return false;
            }
        }
    */
    class waitInfectious extends Behaviour{
        boolean infettato=false;

        @Override
        public void action() {
            ACLMessage msg= myAgent.receive();
            if(msg!=null){
                //calcola contagio
                try {
                    DFAgentDescription[] dfds =
                            DFService.decodeNotification(msg.getContent());
                    String str= String.valueOf(msg.getPerformative());
                    System.out.println(str);
                    if(dfds[0].getAllServices().hasNext()) {
                        ServiceDescription ss = (ServiceDescription) dfds[0].getAllServices().next();
                        Property pp = (Property) ss.getAllProperties().next();
                        AID contagiato = dfds[0].getName();
                        System.out.println(pp.getName() + pp.getValue() + " " + contagiato);
                        //infettato=true;
                        System.out.println("Sono contagiato "+ getName());
                        setExposed();
                    }
                } catch (FIPAException e) {
                e.printStackTrace();
                }

            }
            block();
        }

        @Override
        public boolean done() {
            return infettato;
        }
    }
/*
    class consumePrimary extends CyclicBehaviour{

        @Override
        public void action() {

        }
    }
*/
    void sneeze() {
        
    }
}