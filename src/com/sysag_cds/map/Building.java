package com.sysag_cds.map;

public class Building extends Location {

    enum Business{
        Negozio,
        Supermercato,
        Scuola,
        Fabbrica,
        Parco,
        Ospedale
    }

    float distanceDPI= (float) 0.5; //ogni edificio ha un valore di DPI da rispettare o possiede una certa distanza di sicurezza che può essere convertita in DPI
    public Business bus;

    public Building(String l) {
        super(l);
    }
}
