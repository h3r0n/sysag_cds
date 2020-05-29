package com.sysag_cds.scheduling;

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
    public void scheduleTask(Task t) {
        if (todo == null) {
            todo = new SequentialBehaviour();
            this.addBehaviour(todo);
        }
        if (todo.done()) {
            todo = new SequentialBehaviour();
            this.addBehaviour(todo);
        }
        todo.addSubBehaviour((Behaviour) t);
    }

    /**
     * Behaviour da eseguire in sequenza.
     */
    public interface Task {
        void action();
        boolean done();
    }
}
