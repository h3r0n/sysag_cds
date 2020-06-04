# Benvenuti nella wiki del progetto sysag_cds !
Tale progetto ha lo scopo di realizzare una simulazione della diffusione del COVID-19 mediante un sistema multi-agente.\
La simulazione riproduce tramite agenti le principali azioni svolte dalle loro controparti umane, e ha lo scopo di mostrare dinamicamente come e quanto le precauzioni e le norme igieniche posso contrastare la diffusione del virus.\
La simulazione è caratterizzata da diverse entità e dinamiche di interazioni, ognuna descritta nella apposita pagina relativa.
## Simulazione
La simulazione virtuale del contagio da COVID-19 viene avviata da un tipo particolare di _agente super partes_ denominato appunto **Simulazion**.
Tale agente elabora innanzitutto gli argomenti passati da linea di comando volti a caratterizzare i parametri della simulazione:
1. Numero di PERSON
2. Probabilità di creare persone SUSCEPTIBLE (tra 0 e 1)
3. Probabilità di creare persone EXPOSED (tra 0 e 1)
4. Probabilità di creare persone INFECTIOUS (tra 0 e 1)
5. Probabilità di creare persone RECOVERED (tra 0 e 1)
6. Probabilità di creare persone NON COSCIENZIOSE (tra 0 e 1)
7. Probabilità di creare LAVORATORI (tra 0 e 1)
8. Numero di BUILDING per lato, considerando che la mappa è un quadrato. Deve essere >= 2
9. Numero di BUSINESS
***
Successivamente,viene istanziata la classe **Statistics** che ha il compito di compilare le statistiche del sistema durante la sua esecuzione,mediante messaggi inviati da ciascun agente PERSON, quali:
* Numero di agenti PERSON correntemente infetti
* Numero totale di agenti PERSON infettati
* Numero di agenti PERSON guariti
* Numero di agenti PERSON deceduti
***
Inoltre, viene istanziata la classe **Government** che ha il compito di emanare _DECRETI_ atti a contrastare la diffusione del virus agendo sui seguenti parametri:
* Distanza massima percorribile durante una passeggiata
* Distanza massima percorribile per recarsi in un edificio
* Distanza massima tra individui concessa all'interno di un edificio
* Obbligatorietà della mascherina rispetto ad ambienti chiusi ed aperti
Tali decreti dunque sono in grado di determinare la chiusura di luoghi di lavoro e di svago.
***
Infine l'agente **Simulation** pone le basi per la creazione delle istanze volte a definire le caratteristiche della simulazione stessa, trattane nella sezione relativa alla classe **World**
## Mondo
Gli agenti _Person_ si muovono in una rappresentazione schematizzata a grafo di una città, secondo una struttura a lattice connessa e completa dove i nodi sono gli edifici **Building** e gli archi sono strade **Road**.\
Tale struttura è generata da un istanza della classe **World** mediante la libreria _Jung_ e offre funzionalità necessarie alla navigazione del grafo come:
* Trovare il percorso minimo tra due nodi del grafo
* Trovare la distanza minima tra due nodi del grafo
* Trovare particolari nodi nel grafo
* Generare percorsi verso nodi casuali
* Selezionare nodi casuali nel grafo
***
Sia **Road**,**Building** e **Business** sono sottoclassi della classe **Location** atta ad individuare un generico luogo nella mappa, mediante identificativo _location_ e distanza media di distanziamento sociale _density_ .\
I **Business**, ovvero i luoghi di lavoro e di svago, possono essere di tipo _Ospedale_, _Supermarket_ e _Parco_ mentre tutti gli altri **Building** sulla mappa sono adibiti ad abitazioni per famiglie di agenti Person; Ciascuno di essi è generato dall'apposito agente **Factory** associato. 
## Persone
Gli agenti **Person**,che divengono **Worker** se lavorano presso un _Business_ , sono caratterizzati da:
* Una propria abitazione
* Stato delle scorte alimentari
* Stato della malattia COVID-19
* Tempo necessario a consumare le scorte alimentari
* Tempo necessario per percorrere una strada
* Tempo necessario per andare a fare la spesa
* Tempo necessario per ricevere cure in ospedale per altre patologie
* Tempo necessario per svagarsi al parco
* Temperamento dell'agente (coscienzioso o non)
* Probabilità di decedere a causa del virus
* Probabilità di ammalarsi di altre patalogie\
Per ogni agente **Person** vengono quindi calcolate distribuzioni di probabilità atte a determinare l'esito delle interazioni dell'agente; l'ordine e la tipologia di tali interazioni vengono organizzate in una struttura dati assimilabile ad una coda ed eseguiti in sequenza.
***
Il contagio è stato modellato secondo il modello SEIR che ammette per ogni individuo 4 stati quali _SUSCEPTIBLE,EXPOSED,INFECTIOUS,RECOVERED_ .
* _SUSCEPTIBLE_ indica un individuo che non ha ancora contratto il virus
* _EXPOSED_ indica un individuo che si trova in una condizione tale da poter contrarre il virus
* _INFECTIOUS_ indica un individuo che ha contratto il virus e che è contagioso
* _RECOVERED_ indica un individuo che è guarito dall' infezione e non può essere nuovamente contagiato
***
### Contagio
Il contagio può avvenire quando due o più individui,di cui almeno uno contagioso,si trovano nello stesso istante sulla stessa **Road**,**Building** o **Business** e avviene calcolando due eventi conseguenti, quali la probabilità che l'individuo sano e il contagiato si incontrino e la probabilità che l'infezione avvenga.\
La probabilità associata al verificarsi di questi eventi è dipendente da parametri quali il distanziamento sociale associato alla Location in questione e dal valore dei DPI, Dispositivi di Protezione Individuale, indossati dagli individui.
***

