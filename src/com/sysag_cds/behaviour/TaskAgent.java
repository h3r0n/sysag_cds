package com.sysag_cds.behaviour;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

abstract public class TaskAgent extends Agent {
    SequentialBehaviour todo = null;

    public void scheduleTask(Behaviour t) {
        if (todo == null) {
            todo = new SequentialBehaviour();
            this.addBehaviour(todo);
        }
        if (todo.done()) {
            todo = new SequentialBehaviour();
            this.addBehaviour(todo);
        }
        todo.addSubBehaviour(t);
    }
}
