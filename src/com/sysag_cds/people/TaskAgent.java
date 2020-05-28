package com.sysag_cds.people;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import java.util.LinkedList;
import java.util.Queue;

abstract public class TaskAgent extends Agent {
    Queue<Task> todo = new LinkedList<>(); // coda task non ancora eseguiti

    abstract class Task extends Behaviour {

        public Task() {
            super();
        }

        public Task(Agent a) {
            super(a);
        }

        @Override
        public int onEnd() {
            // il task in esecuzione è il primo della lista: rimuovilo perché è terminato
            if (this == todo.peek())
                todo.remove();

            // esegui il prossimo task, senza rimuoverlo dalla lista (verrà rimosso quando terminerà)
            if (todo.peek() != null)
                addBehaviour(todo.peek());
            return super.onEnd();
        }
    }

    public void scheduleTask(Task t) {
        // se la coda è vuota, non ci sono task in esecuzione, quindi il nuovo task dovrà essere eseguito manualmente
        if (todo.peek() == null) {
            System.out.println("La coda è vuota per l'agente :" + this.getLocalName());
            todo.add(t);
            addBehaviour(todo.peek());
        // se la coda è piena, c'è un task in esecuzione, il quale eseguirà automaticamente il successivo alla terminazione
        } else {
            todo.add(t);
        }
    }
}
