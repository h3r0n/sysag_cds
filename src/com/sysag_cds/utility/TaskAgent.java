package com.sysag_cds.utility;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Agent with a queue of tasks.
 */
abstract public class TaskAgent extends Agent {
    SequentialBehaviour todo = null;

    /**
     * Adds a task to the queue
     *
     * @param t Task that must be added
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
