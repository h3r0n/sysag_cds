package com.sysag_cds.behaviour;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

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

    protected void onWake() {}

    public void reset(long timeout) {
        wakeupTime = System.currentTimeMillis() + timeout;
        finished = false;
    }

    public boolean done() {
        return finished;
    }
}