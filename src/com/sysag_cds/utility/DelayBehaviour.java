package com.sysag_cds.utility;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * Similar to WakerBehaviour, but the delay starts when the behaviour runs, rather than when is created
 */
public abstract class DelayBehaviour extends SimpleBehaviour {
    private long timeout, wakeupTime;
    private boolean finished;

    /**
     * Instantiates a new Delay behaviour.
     *
     * @param a       the agent
     * @param timeout the timeout
     */
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
     * Called function after the delay
     */
    abstract protected void onWake();

    /**
     * Reset.
     *
     * @param timeout the timeout
     */
    public void reset(long timeout) {
        wakeupTime = System.currentTimeMillis() + timeout;
        finished = false;
    }

    public boolean done() {
        return finished;
    }
}