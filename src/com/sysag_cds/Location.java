package com.sysag_cds;

import jade.core.Agent;

public class Location {
    enum edificio{
        scuole,
        negozi,
        lavoro,
        svago,
        casa
    }


    Agent Person;
    edificio tipo_di_edificio;
    int posizione_nodo;
}
