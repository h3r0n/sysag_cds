package com.sysag_cds.utility;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * Simile a WakerBehaviour, ma il delay parte dal momento in cui il behaviour si attiva, e non nel momento in cui
 * viene creato.
 */
public abstract class DelayBehaviour extends SimpleBehaviour {
    private long timeout, wakeupTime;
    private boolean finished;

    public DelayBehaviour(Agent a, long timeout) {
        super(a);
        this.timeout = timeout;
        finished = false;
    }

    public void onStart() {
        wakeupTime = System.currentTimeMillis() + timeout;
    }

    public void action() {
        long dt = wakeupTime - System.currentTimeMillis();
        if (dt <= 0) {
            finished = true;
            onWake();
        } else
            block(dt);
    }

    /**
     * Funzione chiamata in seguito al delay
     */
    abstract protected void onWake();

    public void reset(long timeout) {
        wakeupTime = System.currentTimeMillis() + timeout;
        finished = false;
    }

    public boolean done() {
        return finished;
    }
}