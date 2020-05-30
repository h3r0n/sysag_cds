package com.sysag_cds.utility;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Agente con una coda di Task sempre attiva
 */
abstract public class TaskAgent extends Agent {
    SequentialBehaviour todo = null;

    /**
     * Aggiunge un Task alla coda
     *
     * @param t Task da aggiungere alla coda
     */
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
