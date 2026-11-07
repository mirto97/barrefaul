# BarreFaul dump 07-11-2026

- **Data dump**: sabato 07 novembre 2026
- **Ora dump**: non disponibile (l'ora non è nota a Claude, da aggiungere a mano se serve)
- **Riferimenti temporali nella sessione**: nessun log datato incollato. Compaiono solo diagnostici dell'IDE (senza data) e uno screenshot di VS Code. La data del dump è quella corrente nota a Claude
- **Ambito**: solo il delta di questa conversazione. Il dump precedente è `BarreFaul_dump_29-10-2026.md` e le sue cose sono date per scontate. L'unione la fa l'utente a parte
- **Natura del file**: archivio esteso, non riassunto. Include decisioni, motivazioni, codice, errori, fraintendimenti chiariti e punti aperti
- **Formato data dei dump (deciso in questa sessione)**: `dd-mm-yyyy`. Vale per nome file (`BarreFaul_dump_dd-mm-yyyy.md`), titolo interno e campo **Data dump**. Per stabilire quale dump è il più recente si confrontano le date vere, non l'ordine alfabetico dei nomi (con questo formato non coincide con quello cronologico)
- **Nota sul codice**: il codice qui sotto è ricostruito dalla conversazione. L'utente ha applicato in locale modifiche comunicate solo a parole (lambda al posto dei method reference, `injectionStrategy` sui mapper, ecc.). Dove una modifica non è stata confermata esplicitamente è segnato. In caso di differenze **prevalgono i file locali dell'utente** (ad esempio i suoi commenti e TODO non vanno sovrascritti)

---

## 0. Cosa sostituisce o aggiorna nel dump 29-10-2026

| Sezione del dump 29-10-2026 | Effetto in questo dump |
|---|---|
| 2.5 Eventi, "Da confermare" (regola ricorrente) | Risolto: regola ricorrente come **entità a parte**, chiamata `BarreFaulEventOG` (sezione 4.9) |
| 4.3 `Crew` | `socials` ora con `@OrderColumn` (4.1) |
| 4.5 `Freestyler` | **Sostituita**: `score` diventa `BigDecimal`, aggiunti i sette contatori (4.6) |
| 4.6 `Event` | **Sostituita** per `judges` e `hosts`: da `@ManyToMany` verso `Crew` a `@ElementCollection` di `String` (4.7) |
| 5 Repository | `FreestylerRepository` ed `EventRepository` hanno metodi nuovi (5) |
| 6.1 DTO, `CrewSummary` nei DTO di `Event` | `Event` non usa più `CrewSummary` né gli id; `CrewSummary` resta comunque nel progetto (6) |
| 6.2 DTO | Sostituiti: `SocialRequest` (+`id`), `EventRequest`, `EventResponse`, `FreestylerRequest`, `FreestylerResponse`. Nuovi: `PlacementEntry`, `TournamentResultsRequest` (6) |
| 6.3 Mapper | Tutti sostituiti (`injectionStrategy`, metodi `updateEntity`) (7) |
| 7 Ranking | Ora implementato nel service (8) |
| 8.1 "Da fare nel prossimo lavoro" | Service e controller **fatti**. Resta aperto il resto (sezione 12) |
| 8.2 "Statistiche piazzamenti di `Freestyler`" | Non più rimandata: implementata come sette contatori |
| 8.2 "Ordine di visualizzazione dei `Social`" | Fatto con `@OrderColumn` |
| 8.2 "`link`/`image` e gestione upload" | Upload deciso, è il **primo TODO** (sezione 11) |

---

## 1. Regole di lavoro emerse in questa sessione

- **Formato data dei dump**: `dd-mm-yyyy` (vedi intestazione).
- Quando l'utente chiede "dove devo modificare", Claude indica **file, metodo e import da aggiungere**, e riscrive il metodo intero se la modifica tocca più punti.
- Quando si cambia un file che contiene commenti o TODO dell'utente, Claude scrive **solo i campi da cambiare** invece di rigenerare il file (caso `Freestyler`), per non toccare i commenti.
- Claude **non può verificare nell'IDE**: dopo ogni correzione di warning l'utente ricontrolla il pannello Problems.
- L'utente chiede spesso **pro e contro** o una spiegazione con esempio prima di scegliere ("dimmi pro e contro delle 3 scelte", "spiega meglio"). Si risponde con confronto e, quando serve, un esempio concreto con dati reali (es. Lofo).
- Messaggi ambigui ("ok proseguiamo", "proseguiamo"): Claude chiede quale strada scegliere elencando le opzioni (regola già presente nel dump precedente, applicata più volte).
- Quando l'utente dice "li ho applicati io" o "ok apposto", Claude non ripete il codice.
- Una domanda con più opzioni: l'utente risponde spesso con la scelta più una condizione ("ma in futuro vorrei..."). Le condizioni vanno annotate come TODO futuri, senza anticiparle nel codice.

---

## 2. Fraintendimenti e correzioni chiariti durante la sessione

- **Nome del file dump**: Claude aveva detto che il file allegato si chiamava `BarreFaul_dump_2026-10-29.md`. Il nome reale era già `BarreFaul_dump_29-10-2026.md`. Cambiava solo il titolo interno ("BarreFaul dump 2026-10-29"), che nei nuovi dump è in `dd-mm-yyyy`.
- **"Quattro import"**: Claude ha scritto "quattro import nuovi" in `FreestylerService` ma ne ha elencati cinque (`HashSet`, `Set`, `PlacementEntry`, `TournamentResultsRequest`, `InvalidRequestException`). Sono cinque.
- **Event e ranking**: Claude aveva ipotizzato che i risultati dei freestyler fossero legati a un `Event` della serata. L'utente ha chiarito che **`Event` è completamente staccato dal ranking**: si aggiorna circa ogni settimana, non importa se e quali eventi ci siano stati, non serve un `Event` a cui agganciare i risultati, e non serve nessuno storico.
- **"La regola intendi l'evento?"**: l'utente ha confuso la "regola ricorrente di Barre Faul" con `Event`. Chiarito: `Event` è la serata singola, la regola ricorrente è l'altra cosa (ogni mercoledì 21:30, location fissa e location pioggia).
- **Package `api`**: l'utente ha risposto "si ma il package si chiama api" alla proposta di gestire i 400. Claude l'ha interpretato come: il package dei controller si chiama `com.viterba.barrefaul.api` (non `controller`). **Interpretazione di Claude, non contestata dall'utente.** Il prefisso degli URL `/api` resta.
- **Troncamenti del documento di ranking**: l'utente ha chiarito che i valori "troncati" del documento erano **arrotondamenti fatti per praticità** perché i calcoli si facevano a mano. Ora i calcoli sono esatti e il troncamento è solo visivo.
- **Contatori, opzione B (mappa)**: l'utente non aveva capito la differenza tra sette campi e una mappa. Claude l'ha rispiegata con l'esempio di Lofo.
- **`Sort.sort(...)` in `EventService`**: la prima correzione di Claude del warning sulla stringa `"date"` (`Sort.sort(Event.class).by(Event::getDate).descending()`) ha introdotto altri due hint (metodo deprecato da Spring Data 4.1 e null type safety). Sostituita con un metodo derivato nel repository.
- **Limite dei 255 caratteri**: Claude aveva detto che l'upload risolve il limite di `link` e `image`. In realtà riguarda solo `image` (che diventa un riferimento al file). `link` è l'URL del social e resta a 255 caratteri, da rivedere.
- **"Ripetimi la domanda di prima"**: Claude ha riformulato la domanda su `score`/`participations` (Integer con null = invariato, oppure endpoint separato).

---

## 3. Decisioni sui service, sull'update e sugli errori

### 3.1 `CrewService`
- CRUD completo: lista, dettaglio per id, create, update, delete.
- **Eccezione custom** per id inesistente: `ResourceNotFoundException(resource, id)`.
- **`@RestControllerAdvice` globale** (`GlobalExceptionHandler`), scelto rispetto a `@ResponseStatus` sulla classe. Motivo: corpo dell'errore proprio, e gestione nello stesso posto di validazione (400) e altri errori.
- `findAll` di `Crew` restituisce anche i `Freestyler` (query polimorfica) mappati come `CrewResponse`, quindi senza `score` e `participations`. **Accettato dall'utente**: per quei dati userà il find di `Freestyler`.
- Update con `@MappingTarget` (`updateEntity`) in `CrewMapper`.
- **Semantica dell'update**: `roles` null nella request = invariati (`NullValuePropertyMappingStrategy.IGNORE`). I campi scalari (`firstName`, `bio`, ecc.) seguono la logica PUT: un `null` li azzera. `alias` è obbligatorio.
- **`Social` in update: merge per id** (scelto dall'utente rispetto agli endpoint dedicati). Motivo: i social si modificano spesso anche solo per aggiungerne o rimuoverne uno, e serve un ordine di visualizzazione.
  - `SocialRequest` ha un `id` opzionale: con id aggiorna, senza id crea, assente dalla lista cancella.
  - `socials` null nella request = invariati, lista vuota = tutti rimossi.
  - Ordine = posizione nella lista, persistito con `@OrderColumn(name = "display_order")` su `Crew.socials`.
  - Un `id` che non appartiene a quel membro dà 404. Un `id` ripetuto nella stessa lista **non è controllato**.
  - Il merge è scritto a mano nel componente `SocialMerger` (MapStruct da solo sostituirebbe gli elementi). `SocialMerger` è condiviso da `CrewService` e `FreestylerService` (un `Freestyler` è un `Crew`).
- `CrewService.update` usa `flush()` (non `saveAndFlush`): l'entità è già gestita, e il flush valorizza gli id dei nuovi `Social` prima di costruire la response.
- **`CrewSummary` e `CrewMapper.toSummary` restano nel progetto**, anche se ora non li usa nessuno: l'utente pensa che gli servirà prendere solo `alias` o `id` di un `Crew`.

### 3.2 `judges` e `hosts` di `Event` come alias testuali
- Percorso: l'utente ha proposto di semplificare mettendo `judges` e `hosts` come liste di `String` con gli alias. Claude ha elencato pro e contro. Scelta: **alias come testo**.
  - Guadagna: storico degli eventi intatto anche se un membro viene cancellato, giudici o conduttori esterni senza creare un `Crew`, nessun vincolo di chiave esterna.
  - Perde: nessun legame tra evento e scheda del membro (niente link al profilo, niente "eventi in cui ha giudicato X"), un alias cambiato lascia il vecchio nome negli eventi vecchi, un refuso diventa un giudice diverso.
- **Delete di un `Crew`**: con gli alias testuali non esistono più vincoli, quindi si può cancellare liberamente e il service non tocca `EventRepository`. L'utente voleva comunque poter cancellare un `Crew` legato a un evento. Il codice di delete non è cambiato.
- **Futuro "parecchio lontano"**: l'utente vorrà legare l'evento alla scheda del membro. Per ora trascurato, si farà con una modifica a parte.
- Tipo usato: `Set<String>` (nessun ordine salvato, niente duplicati) tramite `@ElementCollection`, tabelle `event_judges` ed `event_hosts` con colonna `alias`.

### 3.3 `EventService`
- CRUD completo con la stessa semantica di `Crew`: `judges` o `hosts` null nella request = collezioni invariate. I campi scalari seguono la logica PUT (per esempio `selectionsStartTime` mancante viene azzerato).
- `findAll` ordinato per data **dal più avanti nel tempo al più indietro**. Storia della scelta tecnica:
  1. `Sort.by(Sort.Direction.DESC, "date")`, che dà il warning `SPRING_DATA_STRING_PROPERTY_REFERENCE` ("Non type-safe property reference").
  2. `Sort.sort(Event.class).by(Event::getDate).descending()`, che dà un hint di deprecazione (da Spring Data 4.1) e un hint di null type safety.
  3. **Stato finale**: metodo derivato `findAllByOrderByDateDesc()` in `EventRepository`, come già fatto per `Freestyler`. Applicazione non confermata esplicitamente dall'utente, che ha poi riferito che il progetto compila.

### 3.4 Correzioni dei warning dell'IDE
- **Null type safety** su due method reference con ricevitore non legato (`FieldError::getField` in `GlobalExceptionHandler`, `Social::getId` nel merge dei social): sostituiti con lambda (`error -> error.getField()`, `social -> social.getId()`). Applicate dall'utente.
- **`@Autowired` field → constructor parameter** nel codice generato `CrewMapperImpl` e `FreestylerMapperImpl`: risolto con `injectionStrategy = InjectionStrategy.CONSTRUCTOR` in `@Mapper` su **tutti** i mapper (`SocialMapper`, `CrewMapper`, `EventMapper`, `FreestylerMapper`), con import `org.mapstruct.InjectionStrategy`. Confermato dall'utente ("ok apposto").
- **Hint residuo, non risolto**: `JAVA_AUTOWIRED_CONSTRUCTOR` ("Unnecessary `@Autowired` annotation", severity 4) nel `FreestylerMapperImpl` generato. Il file sta in `target/generated-sources`, quindi non si corregge a mano. Con `InjectionStrategy.CONSTRUCTOR` MapStruct mette `@Autowired` sul costruttore generato. Non cambia nulla a runtime. Claude ha suggerito il filtro `!**/target/**` nel campo Filter del pannello Problems (vale solo per la vista). **Non verificato** se MapStruct abbia un'opzione per ometterlo. Claude ha chiesto se lasciare così o cercare l'opzione, e l'utente ha risposto "ok proseguiamo" senza sceglierlo: **lasciato aperto**.
- Dallo screenshot: il pannello Problems mostrava anche un info `TODO inserire media ponderata ecc…` in `Freestyler.java` (riga 15, testo troncato), che è un TODO **dell'utente** nel suo file locale. Il git del progetto era sul branch `dev` con modifiche non committate (badge 12 nel controllo del codice sorgente).

---

## 4. Entity (package `com.viterba.barrefaul.entity`)

### 4.1 `Crew` (modifica: `@OrderColumn` su `socials`)
Importare anche `jakarta.persistence.OrderColumn`. Il resto è come nel dump 29-10-2026.

```java
package com.viterba.barrefaul.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nome d'arte, i futuro potremmo fare una lista e metterli vari come su genius
    @NotNull
    private String alias;

    private String firstName;

    private String lastName;

    private String bio = "";

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "crew_roles", joinColumns = @JoinColumn(name = "crew_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "crew_id", nullable = false)
    @OrderColumn(name = "display_order")
    private List<Social> socials = new ArrayList<>();

}
```

### 4.2 `Social`
Nessuna modifica all'entity in questa sessione. **Decisione futura**: `image` smetterà di essere un URL e diventerà un riferimento a un file caricato (vedi sezione 11).

### 4.3 `Role`
Nessuna modifica.

### 4.4 (vedi 4.6) `Placement`
Vedi 4.8.

### 4.5 (vedi 4.7)
Vedi sotto.

### 4.6 `Freestyler` (sostituisce 4.5 del dump precedente)
- `score`: **`BigDecimal`** con default `BigDecimal.ZERO`. Motivo: selezione vale 0,5 e giuria 2,5, quindi i punteggi sono a mezzi punti (es. 122,5). Alternative valutate e scartate: `double` (rischia errori nei confronti dei pari merito), `int` in mezzi punti (esatto, ma con conversione a ogni lettura e scrittura). BigDecimal comodo anche per le medie.
- `participations`: `int`, totale. Il commento dell'utente "iniziamo totale, ma vorrei farla annuale" resta.
- **Sette contatori `int`** (opzione A scelta dall'utente, vedi 9.4): `firstPlaces`, `secondPlaces`, `semifinals`, `quarterfinals`, `roundOf16`, `selections`, `jury`, corrispondenti a p.p., s.p., sm, qf, o, sel, g.
- Il blocco commentato dei vecchi piazzamenti (`primoPosto`, ecc.) è sostituito dai contatori: Claude ha detto all'utente di toglierlo a mano.
- Nel file locale dell'utente c'è anche un suo TODO (`TODO inserire media ponderata ecc…`) che qui non è ricostruito.
- Scala della colonna `score`: quella di default di Hibernate per `BigDecimal`. Per fissarla c'è `@Column(precision, scale)`, **non deciso**.

```java
package com.viterba.barrefaul.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Freestyler extends Crew {

    // punteggio attuale
    private BigDecimal score = BigDecimal.ZERO;

    // partecipazioni (iniziamo totale, ma vorrei farla annuale)
    private int participations = 0;

    // Contatori dei piazzamenti
    private int firstPlaces = 0;
    private int secondPlaces = 0;
    private int semifinals = 0;
    private int quarterfinals = 0;
    private int roundOf16 = 0;
    private int selections = 0;
    private int jury = 0;

    // aggiungo il ruolo di freestyler nel costruttore
    public Freestyler() {
        getRoles().add(Role.FREESTYLER);
    }

}
```

### 4.7 `Event` (sostituisce 4.6 del dump precedente)
`judges` e `hosts` diventano `@ElementCollection` di `String`. Via gli import `JoinTable` e `ManyToMany`, dentro `CollectionTable`, `Column`, `ElementCollection`. Tutto il resto (campi, validazioni, commento sui campi futuri) è invariato.

```java
package com.viterba.barrefaul.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Event {

    // classe che poi verrà estesa da classi più specifiche, se serve

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate date;

    // Ora di inizio serata (da locandina)
    @NotNull
    private LocalTime advertisedStartTime;

    // Ora di inizio selezioni
    private LocalTime selectionsStartTime;

    @ElementCollection
    @CollectionTable(name = "event_judges", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "alias")
    private Set<String> judges = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "event_hosts", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "alias")
    private Set<String> hosts = new HashSet<>();

    @NotBlank
    private String location;

    /* da aggiungere poi
    private List<?> sponsors;
    private List<Freestyler> tds;
    private List<Crew> dj;
    */

}
```

### 4.8 `Placement` (nuovo enum)
- Rappresenta il piazzamento ottenuto da un freestyler in una serata. Nella stessa classe: **contatore da incrementare** e **punti**, così nel service non ci sono `switch` sparsi da aggiornare.
- Valori: `FIRST_PLACE`, `SECOND_PLACE`, `THIRD_PLACE`, `SEMIFINAL`, `QUARTERFINAL`, `ROUND_OF_16`, `SELECTION`, `JURY`.
- **`THIRD_PLACE`** (proposto da Claude, approvato: "molto comodo"): terzo posto della finale a tre. Incrementa il contatore `secondPlaces` (nel documento è "segnato come s.p.") e vale 3 punti. Serve a distinguere nell'invio secondo e terzo, che prendono 4 e 3 punti.
- **Punti in `Placement`** (scelta dell'utente fra enum, configurazione, tabella modificabile dall'admin; vedi 9.5).
- Il metodo `points(roundOf16Night, threeWayFinalNight)` riceve i due flag della serata. **I due flag sono indipendenti e possono coincidere** (ottavi e finale a tre nella stessa serata). L'utente ha confermato che quando coincidono non ci sono conflitti di valore.
- Il metodo `isAllowed(...)`: `THIRD_PLACE` valido solo con il flag della finale a tre, `ROUND_OF_16` solo con quello degli ottavi. Direzione indicata dall'utente ("anche se per ora non è importante, ma la direzione è quella"): chi li manda senza il flag corrispondente riceve un 400.

Tabella punti implementata:

| Piazzamento | Standard | Ottavi | Finale a tre | Ottavi + finale a tre |
|---|---|---|---|---|
| `FIRST_PLACE` | 5 | 5 | 5 | 5 |
| `SECOND_PLACE` | 3 | 4 | 4 | 4 |
| `THIRD_PLACE` | n/a | n/a | 3 | 3 |
| `SEMIFINAL` | 2 | 3 | 2 | 3 |
| `QUARTERFINAL` | 1 | 2 | 1 | 2 |
| `ROUND_OF_16` | n/a | 1 | n/a | 1 |
| `SELECTION` | 0,5 | 0,5 | 0,5 | 0,5 |
| `JURY` | 2,5 | 2,5 | 2,5 | 2,5 |

Con la sola finale a tre semifinale resta a 2 e quarti a 1, perché la legenda non li cambia.

```java
package com.viterba.barrefaul.entity;

import java.math.BigDecimal;
import java.util.function.Consumer;

// Piazzamento ottenuto in una serata, abbinato al contatore di Freestyler da incrementare e ai punti assegnati
public enum Placement {

    FIRST_PLACE(freestyler -> freestyler.setFirstPlaces(freestyler.getFirstPlaces() + 1)),
    SECOND_PLACE(freestyler -> freestyler.setSecondPlaces(freestyler.getSecondPlaces() + 1)),
    // Terzo posto della finale a tre: viene contato come secondo posto
    THIRD_PLACE(freestyler -> freestyler.setSecondPlaces(freestyler.getSecondPlaces() + 1)),
    SEMIFINAL(freestyler -> freestyler.setSemifinals(freestyler.getSemifinals() + 1)),
    QUARTERFINAL(freestyler -> freestyler.setQuarterfinals(freestyler.getQuarterfinals() + 1)),
    ROUND_OF_16(freestyler -> freestyler.setRoundOf16(freestyler.getRoundOf16() + 1)),
    SELECTION(freestyler -> freestyler.setSelections(freestyler.getSelections() + 1)),
    JURY(freestyler -> freestyler.setJury(freestyler.getJury() + 1));

    private final Consumer<Freestyler> counterIncrement;

    Placement(Consumer<Freestyler> counterIncrement) {
        this.counterIncrement = counterIncrement;
    }

    public void incrementCounter(Freestyler freestyler) {
        counterIncrement.accept(freestyler);
    }

    // Punti in base al formato della serata: ottavi e finale a tre sono indipendenti e possono coincidere
    public BigDecimal points(boolean roundOf16Night, boolean threeWayFinalNight) {
        return switch (this) {
            case FIRST_PLACE -> BigDecimal.valueOf(5);
            case SECOND_PLACE -> BigDecimal.valueOf(roundOf16Night || threeWayFinalNight ? 4 : 3);
            case THIRD_PLACE -> BigDecimal.valueOf(3);
            case SEMIFINAL -> BigDecimal.valueOf(roundOf16Night ? 3 : 2);
            case QUARTERFINAL -> BigDecimal.valueOf(roundOf16Night ? 2 : 1);
            case ROUND_OF_16 -> BigDecimal.ONE;
            case SELECTION -> new BigDecimal("0.5");
            case JURY -> new BigDecimal("2.5");
        };
    }

    // Piazzamenti validi solo con il formato di serata corrispondente
    public boolean isAllowed(boolean roundOf16Night, boolean threeWayFinalNight) {
        return switch (this) {
            case THIRD_PLACE -> threeWayFinalNight;
            case ROUND_OF_16 -> roundOf16Night;
            default -> true;
        };
    }

}
```

### 4.9 `BarreFaulEventOG` (nuova entity: regola ricorrente di Barre Faul)
- **Scelta dell'utente**: regola ricorrente come **entità a parte** (non eventi normali inseriti a mano ogni mercoledì), con `judges` e `hosts` come "liste di string nullabili con solo gli alias".
- Nome: nato come `RecurringEvent`, poi **rinominato dall'utente `BarreFaulEventOG`**.
- **Per ora solo l'entity**: l'utente ha detto "ora non mi serve tutto, facciamolo solo come entità e il resto lo faremo quando saprò esattamente come gestirlo". Quindi **niente** repository, DTO, mapper, service, controller. Se erano già stati aggiunti quelli del nome precedente vanno cancellati.
- Campi: `name`, `dayOfWeek` (`java.time.DayOfWeek`, salvato come stringa), `advertisedStartTime`, `location` (fissa), `rainLocation` (alternativa in caso di pioggia, sempre la stessa), `judges`, `hosts`.
- `rainLocation` è `@NotBlank` (obbligatoria, perché l'utente ha detto che esiste sempre). Claude ha detto di togliere l'annotation se può mancare, senza obiezioni.
- `judges` e `hosts`: Claude ha usato `Set<String>` come in `Event`. L'utente aveva detto "liste": **non è stato chiesto esplicitamente se serve una `List` con ordine** (in quel caso si aggiunge `@OrderColumn`). In database "nullable" e "lista vuota" coincidono (nessuna riga).
- Tabelle: `barre_faul_event_og_judges` e `barre_faul_event_og_hosts`, colonna `alias`.

```java
package com.viterba.barrefaul.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BarreFaulEventOG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Giorno della settimana in cui si ripete
    @NotNull
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    // Ora di inizio serata (da locandina)
    @NotNull
    private LocalTime advertisedStartTime;

    @NotBlank
    private String location;

    // Location alternativa in caso di pioggia, sempre la stessa
    @NotBlank
    private String rainLocation;

    // Alias come testo, nessun legame con Crew
    @ElementCollection
    @CollectionTable(name = "barre_faul_event_og_judges", joinColumns = @JoinColumn(name = "barre_faul_event_og_id"))
    @Column(name = "alias")
    private Set<String> judges = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "barre_faul_event_og_hosts", joinColumns = @JoinColumn(name = "barre_faul_event_og_id"))
    @Column(name = "alias")
    private Set<String> hosts = new HashSet<>();

}
```

---

## 5. Repository (package `com.viterba.barrefaul.repository`)

### 5.1 `FreestylerRepository` (sostituisce il precedente)
- `findAllByOrderByScoreDescAliasAsc()`: ordine su cui si calcola il ranking. A pari punteggio secondo criterio **alfabetico per alias** (scelta dell'utente). Prima era `findAllByOrderByScoreDesc()`.
- L'ordine alfabetico dipende dalla **collation** del database: con quella di default di MySQL maiuscole e minuscole non fanno differenza.
- `countDistinctScoresGreaterThan(BigDecimal)`: posizione = risultato + 1, usata da `findById` per non caricare tutti i freestyler (scelta approvata).

```java
package com.viterba.barrefaul.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.viterba.barrefaul.entity.Freestyler;

public interface FreestylerRepository extends JpaRepository<Freestyler, Long> {

    // Dal punteggio più alto al più basso, a pari punteggio in ordine alfabetico per alias.
    // È l'ordine su cui si calcola il ranking
    List<Freestyler> findAllByOrderByScoreDescAliasAsc();

    // Numero di punteggi distinti più alti di quello dato: la posizione è il risultato + 1
    @Query("select count(distinct f.score) from Freestyler f where f.score > :score")
    long countDistinctScoresGreaterThan(@Param("score") BigDecimal score);

}
```

### 5.2 `EventRepository` (aggiunta del metodo)
```java
package com.viterba.barrefaul.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Dal più avanti nel tempo al più indietro
    List<Event> findAllByOrderByDateDesc();

}
```

`CrewRepository` invariato. Nessun repository per `BarreFaulEventOG` e per `Social` (come deciso).

---

## 6. DTO (package `com.viterba.barrefaul.dto`)

`CrewRequest`, `CrewResponse`, `CrewSummary`, `SocialResponse` invariati.

### 6.1 `SocialRequest` (aggiunto `id`)
```java
package com.viterba.barrefaul.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialRequest(

    // Null per un nuovo social, valorizzato per aggiornarne uno esistente
    Long id,

    @NotBlank
    String name,

    @NotBlank
    String link,

    String image

) {

}
```

### 6.2 `EventRequest` e `EventResponse`
Alias come testo: spariscono `judgeIds`, `hostIds` e `CrewSummary` dai DTO di `Event`.

```java
package com.viterba.barrefaul.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(

    @NotBlank
    String name,

    @NotNull
    LocalDate date,

    @NotNull
    LocalTime advertisedStartTime,

    LocalTime selectionsStartTime,

    @NotBlank
    String location,

    // Alias come testo, nessun legame con Crew
    Set<@NotBlank String> judges,

    Set<@NotBlank String> hosts

) {

}
```

```java
package com.viterba.barrefaul.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record EventResponse(

    Long id,
    String name,
    LocalDate date,
    LocalTime advertisedStartTime,
    LocalTime selectionsStartTime,
    String location,
    Set<String> judges,
    Set<String> hosts

) {

}
```

### 6.3 `FreestylerRequest` e `FreestylerResponse`
- **`score` e `participations` escono da `FreestylerRequest`** (scelta dell'utente: endpoint separato, vedi 9.3). L'anagrafica non può più azzerarli per sbaglio nell'update.
- `FreestylerResponse` ha `score` (`BigDecimal`), `participations`, i sette contatori e `ranking` (`Integer`, calcolato dal service alla lettura).

```java
package com.viterba.barrefaul.dto;

import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FreestylerRequest(

    @NotNull
    String alias,

    String firstName,

    String lastName,

    String bio,

    Set<Role> roles,

    List<@Valid SocialRequest> socials

) {

}
```

```java
package com.viterba.barrefaul.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

public record FreestylerResponse(

    Long id,
    String alias,
    String firstName,
    String lastName,
    String bio,
    Set<Role> roles,
    List<SocialResponse> socials,
    BigDecimal score,
    int participations,
    int firstPlaces,
    int secondPlaces,
    int semifinals,
    int quarterfinals,
    int roundOf16,
    int selections,
    int jury,

    // Calcolato dal service alla lettura, non è nell'entity
    Integer ranking

) {

}
```

### 6.4 `PlacementEntry` e `TournamentResultsRequest` (nuovi)
- L'invio dell'admin è **una sola chiamata** con l'elenco `freestyler → piazzamento` più i due flag del formato della serata.
- I freestyler arrivano **per id** (la ricerca per nome la fa il frontend).

```java
package com.viterba.barrefaul.dto;

import com.viterba.barrefaul.entity.Placement;

import jakarta.validation.constraints.NotNull;

public record PlacementEntry(

    @NotNull
    Long freestylerId,

    @NotNull
    Placement placement

) {

}
```

```java
package com.viterba.barrefaul.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record TournamentResultsRequest(

    // Formato della serata: i due flag sono indipendenti e possono coincidere
    boolean roundOf16Night,

    boolean threeWayFinalNight,

    @NotEmpty
    List<@Valid PlacementEntry> entries

) {

}
```

---

## 7. Mapper (package `com.viterba.barrefaul.mapper`)

Tutti i mapper hanno `injectionStrategy = InjectionStrategy.CONSTRUCTOR` (import `org.mapstruct.InjectionStrategy`).

### 7.1 `SocialMapper`
Aggiunto `updateEntity` (usato dal merge).

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.dto.SocialResponse;
import com.viterba.barrefaul.entity.Social;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SocialMapper {

    @Mapping(target = "id", ignore = true)
    Social toEntity(SocialRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(SocialRequest request, @MappingTarget Social social);

    SocialResponse toResponse(Social social);

}
```

### 7.2 `CrewMapper`
`socials` ignorati in `updateEntity` (merge nel service), `roles` con null = invariati.

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.CrewSummary;
import com.viterba.barrefaul.entity.Crew;

@Mapper(componentModel = "spring", uses = SocialMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CrewMapper {

    @Mapping(target = "id", ignore = true)
    Crew toEntity(CrewRequest request);

    // I socials non si mappano qui: il merge per id è nel service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "socials", ignore = true)
    @Mapping(target = "roles", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CrewRequest request, @MappingTarget Crew crew);

    CrewResponse toResponse(Crew crew);

    CrewSummary toSummary(Crew crew);

}
```

### 7.3 `EventMapper`
Via gli `ignore` su `judges` e `hosts` (non sono più relazioni). Aggiunto `updateEntity`.

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    Event toEntity(EventRequest request);

    // Se judges o hosts sono null nella request, le collezioni esistenti restano invariate
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "judges", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "hosts", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EventRequest request, @MappingTarget Event event);

    EventResponse toResponse(Event event);

}
```

### 7.4 `FreestylerMapper`
`score`, `participations` e i sette contatori **ignorati** in `toEntity` e `updateEntity` (altrimenti MapStruct segnala i target non mappati): li aggiorna solo la registrazione dei piazzamenti.

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.entity.Freestyler;

@Mapper(componentModel = "spring", uses = SocialMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FreestylerMapper {

    // Punteggio, partecipazioni e contatori non arrivano dalla request: li aggiorna solo la registrazione dei piazzamenti
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "firstPlaces", ignore = true)
    @Mapping(target = "secondPlaces", ignore = true)
    @Mapping(target = "semifinals", ignore = true)
    @Mapping(target = "quarterfinals", ignore = true)
    @Mapping(target = "roundOf16", ignore = true)
    @Mapping(target = "selections", ignore = true)
    @Mapping(target = "jury", ignore = true)
    Freestyler toEntity(FreestylerRequest request);

    // I socials non si mappano qui: il merge per id è nel SocialMerger
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "socials", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "firstPlaces", ignore = true)
    @Mapping(target = "secondPlaces", ignore = true)
    @Mapping(target = "semifinals", ignore = true)
    @Mapping(target = "quarterfinals", ignore = true)
    @Mapping(target = "roundOf16", ignore = true)
    @Mapping(target = "selections", ignore = true)
    @Mapping(target = "jury", ignore = true)
    @Mapping(target = "roles", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FreestylerRequest request, @MappingTarget Freestyler freestyler);

    // Il ranking non è nell'entity: è calcolato dal service e passato qui
    @Mapping(target = "ranking", source = "ranking")
    FreestylerResponse toResponse(Freestyler freestyler, Integer ranking);

}
```

**Ancora da verificare** (punto già aperto nel dump precedente): in `target/generated-sources` controllare che con `roles` null nella request in `toEntity` non venga sovrascritto il set inizializzato nell'entity.

---

## 8. Service (package `com.viterba.barrefaul.service`)

Tutti i service sono `@Service`, `@RequiredArgsConstructor`, con `@Transactional(readOnly = true)` a livello di classe (le collezioni sono lazy e `open-in-view` è `false`) e `@Transactional` sui metodi di scrittura.

### 8.1 `SocialMerger` (componente condiviso)
```java
package com.viterba.barrefaul.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.entity.Social;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.SocialMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialMerger {

    private final SocialMapper socialMapper;

    // Merge per id: aggiorna i social con id, crea quelli senza id, elimina quelli assenti dalla lista.
    // L'ordine di visualizzazione è la posizione nella lista
    public void merge(Crew crew, List<SocialRequest> requests) {
        Map<Long, Social> existing = crew.getSocials().stream()
                .collect(Collectors.toMap(social -> social.getId(), Function.identity()));

        List<Social> merged = new ArrayList<>(requests.size());
        for (SocialRequest request : requests) {
            if (request.id() == null) {
                merged.add(socialMapper.toEntity(request));
                continue;
            }
            Social social = existing.get(request.id());
            if (social == null) {
                throw new ResourceNotFoundException("Social", request.id());
            }
            socialMapper.updateEntity(request, social);
            merged.add(social);
        }

        crew.getSocials().clear();
        crew.getSocials().addAll(merged);
    }

}
```

### 8.2 `CrewService`
```java
package com.viterba.barrefaul.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.CrewMapper;
import com.viterba.barrefaul.repository.CrewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: roles è lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class CrewService {

    private final CrewRepository crewRepository;
    private final CrewMapper crewMapper;
    private final SocialMerger socialMerger;

    public List<CrewResponse> findAll() {
        return crewRepository.findAll().stream()
                .map(crewMapper::toResponse)
                .toList();
    }

    public CrewResponse findById(Long id) {
        return crewMapper.toResponse(getCrew(id));
    }

    @Transactional
    public CrewResponse create(CrewRequest request) {
        Crew crew = crewMapper.toEntity(request);
        return crewMapper.toResponse(crewRepository.save(crew));
    }

    @Transactional
    public CrewResponse update(Long id, CrewRequest request) {
        Crew crew = getCrew(id);
        crewMapper.updateEntity(request, crew);
        // Socials null = invariati, lista vuota = tutti rimossi
        if (request.socials() != null) {
            socialMerger.merge(crew, request.socials());
        }
        // Flush immediato: gli id dei nuovi Social vengono valorizzati prima di costruire la response
        crewRepository.flush();
        return crewMapper.toResponse(crew);
    }

    @Transactional
    public void delete(Long id) {
        crewRepository.delete(getCrew(id));
    }

    private Crew getCrew(Long id) {
        return crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", id));
    }

}
```

### 8.3 `EventService`
```java
package com.viterba.barrefaul.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.EventMapper;
import com.viterba.barrefaul.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: judges e hosts sono lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventResponse> findAll() {
        return eventRepository.findAllByOrderByDateDesc().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponse findById(Long id) {
        return eventMapper.toResponse(getEvent(id));
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = eventMapper.toEntity(request);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEvent(id);
        eventMapper.updateEntity(request, event);
        // Entità già gestita: le modifiche vengono salvate al commit
        return eventMapper.toResponse(event);
    }

    @Transactional
    public void delete(Long id) {
        eventRepository.delete(getEvent(id));
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

}
```

### 8.4 `FreestylerService`
- **Ranking** (vedi sezione 9.2): `findAll` lo calcola in memoria dalla lista ordinata, `findById` con la query `count distinct`.
- `create` e `update` **garantiscono sempre il ruolo `FREESTYLER`** (i `roles` della request sostituiscono quelli del costruttore).
- `registerResults`: registra i piazzamenti di una serata in **un'unica transazione**. Prima valida tutto (duplicati e piazzamenti non ammessi per il formato) **senza toccare entità**, poi per ogni voce incrementa il contatore del piazzamento, `participations` di 1 e somma i punti a `score`. Se qualcosa non va non viene salvato nulla.
- Un freestyler può comparire **una sola volta** per invio (scelta dell'utente: "un freestyler non può partecipare più volte allo stesso torneo"), altrimenti 400.
- Un `freestylerId` inesistente dà 404 e annulla tutto (rollback della transazione).

```java
package com.viterba.barrefaul.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.PlacementEntry;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.entity.Freestyler;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.exception.InvalidRequestException;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.FreestylerMapper;
import com.viterba.barrefaul.repository.FreestylerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: roles è lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class FreestylerService {

    private final FreestylerRepository freestylerRepository;
    private final FreestylerMapper freestylerMapper;
    private final SocialMerger socialMerger;

    public List<FreestylerResponse> findAll() {
        List<Freestyler> freestylers = freestylerRepository.findAllByOrderByScoreDescAliasAsc();
        List<FreestylerResponse> responses = new ArrayList<>(freestylers.size());
        int ranking = 0;
        BigDecimal previousScore = BigDecimal.ZERO;
        for (Freestyler freestyler : freestylers) {
            // A pari punteggio la posizione resta la stessa e la successiva non salta (1, 1, 2).
            // compareTo e non equals: equals considera diversi 2.5 e 2.50
            if (responses.isEmpty() || freestyler.getScore().compareTo(previousScore) != 0) {
                ranking++;
            }
            previousScore = freestyler.getScore();
            responses.add(freestylerMapper.toResponse(freestyler, ranking));
        }
        return responses;
    }

    public FreestylerResponse findById(Long id) {
        return toResponse(getFreestyler(id));
    }

    @Transactional
    public FreestylerResponse create(FreestylerRequest request) {
        Freestyler freestyler = freestylerMapper.toEntity(request);
        // I roles della request sostituiscono quelli del costruttore: il ruolo FREESTYLER va sempre garantito
        freestyler.getRoles().add(Role.FREESTYLER);
        return toResponse(freestylerRepository.save(freestyler));
    }

    @Transactional
    public FreestylerResponse update(Long id, FreestylerRequest request) {
        Freestyler freestyler = getFreestyler(id);
        freestylerMapper.updateEntity(request, freestyler);
        freestyler.getRoles().add(Role.FREESTYLER);
        // Socials null = invariati, lista vuota = tutti rimossi
        if (request.socials() != null) {
            socialMerger.merge(freestyler, request.socials());
        }
        // Flush immediato: gli id dei nuovi Social vengono valorizzati prima di costruire la response
        freestylerRepository.flush();
        return toResponse(freestyler);
    }

    @Transactional
    public void delete(Long id) {
        freestylerRepository.delete(getFreestyler(id));
    }

    // Registra i piazzamenti di una serata in un'unica transazione: se un freestyler non esiste o
    // un piazzamento non è valido per il formato, non viene salvato nulla
    @Transactional
    public void registerResults(TournamentResultsRequest request) {
        boolean roundOf16Night = request.roundOf16Night();
        boolean threeWayFinalNight = request.threeWayFinalNight();

        // Ogni freestyler può comparire una sola volta per serata
        Set<Long> seenIds = new HashSet<>();
        for (PlacementEntry entry : request.entries()) {
            if (!seenIds.add(entry.freestylerId())) {
                throw new InvalidRequestException(
                        "Freestyler " + entry.freestylerId() + " appears more than once");
            }
            if (!entry.placement().isAllowed(roundOf16Night, threeWayFinalNight)) {
                throw new InvalidRequestException(
                        "Placement " + entry.placement() + " is not allowed for this night format");
            }
        }

        for (PlacementEntry entry : request.entries()) {
            Freestyler freestyler = getFreestyler(entry.freestylerId());
            entry.placement().incrementCounter(freestyler);
            freestyler.setParticipations(freestyler.getParticipations() + 1);
            freestyler.setScore(freestyler.getScore()
                    .add(entry.placement().points(roundOf16Night, threeWayFinalNight)));
        }
        // Entità già gestite: le modifiche vengono salvate al commit
    }

    private FreestylerResponse toResponse(Freestyler freestyler) {
        return freestylerMapper.toResponse(freestyler, computeRanking(freestyler.getScore()));
    }

    // Posizione = numero di punteggi distinti più alti + 1
    private int computeRanking(BigDecimal score) {
        return Math.toIntExact(freestylerRepository.countDistinctScoresGreaterThan(score)) + 1;
    }

    private Freestyler getFreestyler(Long id) {
        return freestylerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freestyler", id));
    }

}
```

---

## 9. Logica di business del ranking e decisioni collegate

### 9.1 Flusso desiderato dall'utente (lato admin)
- L'admin entra in una **"modalità aggiornamento dei dati"**: cerca un freestyler per nome, indica a che qualificazione è arrivato, poi lo fa con un altro, fino a tutti quelli che hanno partecipato quella serata. Poi chiude e **invia i dati una sola volta**.
- Il server calcola i nuovi punteggi e il ranking. Il ranking viene mostrato nella home page e sul profilo di ogni freestyler (con le sue info).
- **Nessun legame con `Event`**, nessuno storico delle serate: per questo i contatori sono campi sui `Freestyler`.
- Il frontend manda solo il piazzamento; la conversione in punti avviene nel server.
- **Nessun annullamento**: un invio sbagliato (o doppio) non si annulla. Serve un modo per **correggere a mano** score, partecipazioni e contatori. L'utente: "la correzione manuale la lascerei per quando abbiamo tutto funzionante".

### 9.2 Ranking
- Posizione in una lista ordinata per `score` decrescente, **a pari punteggio stessa posizione senza saltare la successiva (1, 1, 2)**. Non è una colonna: calcolato alla lettura (confermato dal dump precedente).
- A pari punteggio l'ordine nella lista è per alias (alfabetico).
- Confronto dei punteggi con `compareTo` (non `equals`) perché `BigDecimal.equals` distingue 2.5 da 2.50.

### 9.3 Update di `score` e `participations`: percorso e scelta
- Problema: con `int` nella request, un JSON senza quei campi li azzerava nell'update.
- Opzioni: (1) `Integer` con null = invariato, (2) endpoint separato per i punteggi. **Scelta dell'utente: la 2**. Poi l'utente ha spiegato il vero flusso (9.1) e l'endpoint è diventato la registrazione dei piazzamenti di una serata (non un `PATCH /stats` con valori assoluti).

### 9.4 Contatori: sette campi `int` (opzione A) rispetto alla mappa `Map<Placement, Integer>`
- **Scelta dell'utente: A** (sette campi `int` su `Freestyler`).
- Confronto presentato:
  - A: tutto in una riga, query semplici (`order by`), DTO con campi espliciti e JSON leggibile per Angular. Contro: un nuovo piazzamento richiede colonna, campo entity, campo in ogni DTO e ramo nel service.
  - B (mappa, tabella a parte con `freestyler_id`, `placement`, `occurrences`): un nuovo piazzamento è solo un valore di enum, ma le query sui singoli contatori richiedono un join e nel JSON i contatori diventano un oggetto con chiavi. Con B `THIRD_PLACE` non sarebbe stata una chiave e ci sarebbero stati due enum.
- Costo di eventuali cambi futuri della legenda con la A (domanda dell'utente "dici che sarà un casino?"):
  - **Nuovo piazzamento**: una colonna, un campo in `Freestyler`, un campo nei DTO, un valore nell'enum. Con `ddl-auto=update` la colonna si aggiunge da sola e le righe esistenti vanno controllate (devono avere 0).
  - **Cambio dei punti**: non tocca contatori né schema, ma `score` è un accumulato senza storico, quindi vale solo dalle serate successive.
  - **Rimozione o rinomina di un piazzamento**: il caso più scomodo e meno probabile. `ddl-auto=update` non cancella le colonne, i dati vanno spostati a mano.
  - Per limitare i danni il legame piazzamento → contatore e punti sta tutto in `Placement`. Passare alla B in futuro è possibile ma con migrazione dei dati.

### 9.5 Dove stanno i punti: enum `Placement` (opzione 1)
- Confronto presentato: (1) nell'enum `Placement`, (2) in configurazione con `@ConfigurationProperties`, (3) in una tabella modificabile dall'admin.
- **Scelta dell'utente: la 1**. Dichiarato che "in un futuro molto lontano" potrebbe arrivare alla 3 ("molto molto lontano questo futuro"). Poiché il service chiama solo `Placement.points(...)`, una tabella futura sostituirebbe quel metodo e non il resto.

### 9.6 Legenda di ranking (documento "Ranking Barre Faul 2026" mostrato dall'utente)
Regole di punteggio:
- 1° posto (p.p.) 5 pt, 2° posto (s.p.) 3 pt, semifinale (sm) 2 pt, quarto di finale (qf) 1 pt.
- Finale a tre: s.p. 4 pt e t.p. 3 pt (entrambi segnati come s.p.)*.
- Ottavi: o 1 pt, qf 2 pt, sm 3 pt, s.p. 4 pt, p.p. 5 pt°.
- Selezioni (sel) 0,5 pt. Giuria (g) 2,5 pt.
- Media ponderata: almeno 10 presenze. Moltiplicatore partecipazioni (su classifica ponderata): 10-19 presenze ×1,5, 20-29 ×2, 30-39 ×2,5, 40 o più ×3.
- `*` e `°` nel documento sono note (finale a tre, ottavi). Decisione: **non si salvano** (i contatori non le distinguono). Confermata con riserva ("credo di confermare").

Classifiche nel documento (tre):
1. **Generale**: per `score` (con le sette voci per freestyler e le presenze).
2. **Ponderata** (almeno 10 presenze): `score / partecipazioni`.
3. **Moltiplicatori**: ponderata × moltiplicatore.

Decisioni sul calcolo:
- **Calcoli esatti, nessun arrotondamento**, troncamento a due decimali solo in visualizzazione. L'utente accetta che i valori non coincidano più con il documento ("probabilmente abbiamo fatto qualche errore di calcolo"). Esempio di Claude: Corra 91,5/40 = 2,2875 (documento: 2,28), Tarvos nei moltiplicatori passa da 8,02 a 8,046875 (103/32 = 3,21875 × 2,5).
- **Ponderata e moltiplicatori**: si calcolano alla lettura da `score` e `participations`, **senza campi nuovi** in `Freestyler`. L'utente: "quella forse si potrebbe non per forza riportare nel freestyler ma calcolarla e restituirla in una sezione a parte più specifica, vedremo". **Non scritto**, forma di esposizione non decisa. (Nel file locale `Freestyler.java` c'è un TODO dell'utente sulla media ponderata.)
- **La giuria conta come partecipazione** (incrementa `participations`): deciso dall'utente.
- **Punto aperto (domanda 4 di Claude)**: nel documento `participations` non coincide sempre con la somma dei contatori. Esempi: Tarvos 34 piazzamenti contro 32 presenze, Saiko 19 contro 21, e anche Centos, Rey, Arfa e Fix. Non è noto se siano scarti voluti o errori. L'utente: "possiamo rimandare questa domanda? perché ora non so risponderti". **Rimandata.**

### 9.7 Archivio annuale (da scrivere in futuro)
- Il documento si intitola "Ranking 2026": il ranking è annuale.
- L'utente vuole **salvare la classifica finale annuale** in uno **storico a parte**, **non come campi su `Freestyler`**: "mi basta anche solo salvata la classifica finale annuale, ed eventualmente si può riprendere il posizionamento finale del freestyler nel ranking di quell'anno".
- **Non deciso e non scritto**: cosa salvare, come chiudere e azzerare l'anno, collegamento al freestyler. Non influisce su ciò che è stato scritto adesso.

---

## 10. Controller, gestione errori e API

### 10.1 Scelte (standard di Claude, non contestate)
- Package **`com.viterba.barrefaul.api`** (vedi fraintendimenti, sezione 2). I file stanno nella cartella `api`.
- Prefisso `/api`, come nella property commentata in `application.properties` (`spring.data.rest.base-path=/api` resta commentata): `/api/crew`, `/api/freestylers`, `/api/events`.
- `GET` lista, `GET /{id}`, `POST` (201), `PUT /{id}`, `DELETE /{id}` (204). Body sempre con `@Valid`.
- **Registrazione dei piazzamenti**: `POST /api/freestylers/tournament-results`, risponde **204** senza body (il service restituisce `void`). Il nuovo ranking si legge dalla lista (`GET /api/freestylers`). Claude ha chiesto se preferisce che l'invio restituisca la classifica aggiornata: l'utente non ha risposto.
- **Security**: finché è disabilitata anche create, update, delete e `tournament-results` sono aperti a tutti. Quando si abilita andranno limitati all'admin e le `GET` lasciate pubbliche.

### 10.2 `CrewController`
```java
package com.viterba.barrefaul.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.service.CrewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @GetMapping
    public List<CrewResponse> findAll() {
        return crewService.findAll();
    }

    @GetMapping("/{id}")
    public CrewResponse findById(@PathVariable Long id) {
        return crewService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrewResponse create(@Valid @RequestBody CrewRequest request) {
        return crewService.create(request);
    }

    @PutMapping("/{id}")
    public CrewResponse update(@PathVariable Long id, @Valid @RequestBody CrewRequest request) {
        return crewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        crewService.delete(id);
    }

}
```

### 10.3 `FreestylerController`
```java
package com.viterba.barrefaul.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.service.FreestylerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/freestylers")
@RequiredArgsConstructor
public class FreestylerController {

    private final FreestylerService freestylerService;

    // Lista già ordinata per ranking
    @GetMapping
    public List<FreestylerResponse> findAll() {
        return freestylerService.findAll();
    }

    @GetMapping("/{id}")
    public FreestylerResponse findById(@PathVariable Long id) {
        return freestylerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FreestylerResponse create(@Valid @RequestBody FreestylerRequest request) {
        return freestylerService.create(request);
    }

    @PutMapping("/{id}")
    public FreestylerResponse update(@PathVariable Long id, @Valid @RequestBody FreestylerRequest request) {
        return freestylerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        freestylerService.delete(id);
    }

    // Registrazione dei piazzamenti di una serata: il nuovo ranking si legge poi dalla lista
    @PostMapping("/tournament-results")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerResults(@Valid @RequestBody TournamentResultsRequest request) {
        freestylerService.registerResults(request);
    }

}
```

### 10.4 `EventController`
```java
package com.viterba.barrefaul.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Lista ordinata per data, dal più avanti nel tempo al più indietro
    @GetMapping
    public List<EventResponse> findAll() {
        return eventService.findAll();
    }

    @GetMapping("/{id}")
    public EventResponse findById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }

}
```

### 10.5 Package `com.viterba.barrefaul.exception`
- `ResourceNotFoundException` → 404.
- `InvalidRequestException` (nuova, per i controlli di business di `registerResults`) → 400.
- `ApiError(status, message, timestamp, errors)`: `errors` è una mappa campo → messaggio, usata solo per la validazione. Con `spring.jackson.default-property-inclusion=non_null` i campi nulli non compaiono.
- `GlobalExceptionHandler` gestisce: not found (404), `MethodArgumentNotValidException` (400 con `errors`, tiene il primo messaggio se più errori sullo stesso campo), `InvalidRequestException` (400), `HttpMessageNotReadableException` (400, JSON malformato o valore non valido come un `placement` inesistente), `MethodArgumentTypeMismatchException` (400, parametro dell'URL del tipo sbagliato, es. id non numerico).
- Nei due 400 sul body e sul parametro il **messaggio dell'eccezione originale non viene restituito** (contiene nomi di classi e posizione nel JSON, il dettaglio resta nei log). Se l'utente lo vuole nella risposta, si aggiunge. I due handler sono stati proposti da Claude con "si" dell'utente, applicazione non confermata esplicitamente.

```java
package com.viterba.barrefaul.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }

}
```

```java
package com.viterba.barrefaul.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

}
```

```java
package com.viterba.barrefaul.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(

    int status,
    String message,
    Instant timestamp,

    // Solo per gli errori di validazione, campo -> messaggio
    Map<String, String> errors

) {

}
```

```java
package com.viterba.barrefaul.exception;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // Più errori sullo stesso campo: si tiene il primo
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> Objects.toString(error.getDefaultMessage(), ""),
                        (first, second) -> first));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // JSON malformato o valore non valido (es. un placement che non esiste nell'enum)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid value", null);
    }

    // Parametro dell'URL del tipo sbagliato (es. id non numerico)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName(), null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), message, Instant.now(), errors));
    }

}
```

---

## 11. Upload di file (primo TODO)

**Decisione**: l'utente vuole l'**upload del file** per le immagini, e vuole cambiare anche `Social` con "un bel upload" (l'`image` di `Social` non sarà più un URL ma un riferimento a un file salvato). **Questo è il primo TODO da fare.** Nessun codice scritto ancora.

- **Difficoltà** (risposta data all'utente): non difficile. Backend base in tre pezzi: endpoint di upload (`MultipartFile`), servizio di storage (valida tipo e dimensione, salva con nome generato UUID, mai quello originale, per evitare collisioni e path traversal, e cancella), modo di servire i file al frontend (endpoint o resource handler sulla cartella). Lato Angular: `input type="file"` e `FormData`.
- **Cosa lo complica**:
  - Limite di default di Spring a 1 MB: vanno impostati `spring.servlet.multipart.max-file-size` e `max-request-size`.
  - Controllo del tipo sul **contenuto vero** (estensione e content-type del client si falsificano) per accettare solo immagini.
  - Packaging `war`: i file non vanno dentro l'applicazione (un nuovo deploy li cancellerebbe), ma in una **cartella esterna** configurata da una property.
  - **File orfani**: se un'immagine viene sostituita o cancellata il file su disco va rimosso insieme al riferimento.
  - Ridimensionamento e miniature: non servono subito, si possono aggiungere dopo.
  - Con la security attiva upload e cancellazione andranno limitati all'admin.
- **Dove salvare i file**: scelta dell'utente: **cartella sul server (fuori dal `war`)**. Serve un backup della cartella oltre a quello del DB. Alternative presentate e non scelte: BLOB nel database (un solo backup ma DB più pesante), storage cloud (es. S3).
- Collegamenti: l'upload risolve il limite dei 255 caratteri di `image` in `Social` (non di `link`, vedi fraintendimenti). Le immagini di `Event` e la galleria dipendono dal modello dell'upload, quindi si parte da lì.

---

## 12. Punti aperti e rimandati

### 12.1 Prossimi passi (ordine indicato dall'utente: upload per primo)
1. **Upload di file** con cartella esterna al `war` (sezione 11).
2. **Entità mancanti**: galleria **generale** e galleria **per evento** (le immagini di `Event` dipendono da come si modella l'upload), news, "chi siamo", contatti, e i campi dell'`Event` del requisito iniziale che oggi mancano (descrizione, immagini, prezzo, categoria).
3. **Correzione manuale** di `score`, partecipazioni e contatori (in coda, "per quando abbiamo tutto funzionante").
4. Cambio di `Social.image` da URL a file caricato.

### 12.2 Rimandati per scelta dell'utente
- **Classifiche ponderata e moltiplicatori**: da calcolare alla lettura, forma di esposizione ("sezione a parte più specifica") da decidere.
- **Archivio annuale** della classifica finale (sezione 9.7).
- **Partecipazioni annuali** (già nel dump precedente, ora legata all'archivio annuale).
- **Regola ricorrente `BarreFaulEventOG`**: repository, DTO, mapper, service, controller solo quando l'utente saprà come gestirla. Scelte ancora aperte: una sola regola (`GET`/`PUT` senza id, nessuna create/delete) oppure CRUD completo con id; se la prossima data la calcola il server (in quel caso fissare il fuso `Europe/Rome`) o il frontend; `List` con ordine invece di `Set` per giudici e conduttori.
- **Legare `Event` alla scheda del membro** (giudici e conduttori), "futuro parecchio lontano".
- **Tabella dei punti modificabile dall'admin**, "futuro molto molto lontano".
- **Domanda 4**: scarti tra `participations` e somma dei contatori nel documento.
- Resta rimandato dal dump precedente: Angular, Flyway, security (e `ROLE_`, jjwt 0.12.x, verifica Jackson 2/3), prenotazione posto, `bio` oltre 255 caratteri, lunghezza di `link`, lista di alias per `Crew`, sottoclasse `Member`, `sponsors`/`tds`/`dj` in `Event`, strategia di ereditarietà per `Event`, `EventDto` alternativo, DTO aggregatore del ranking, abilitazione di swagger/api-docs.

### 12.3 Segnalati da Claude, non risolti
- Hint `JAVA_AUTOWIRED_CONSTRUCTOR` nel `FreestylerMapperImpl` generato (3.4).
- Un `Crew` creato da `CrewService` con il ruolo `FREESTYLER` resta una riga di `Crew` e non diventa un `Freestyler`, quindi non compare nelle liste di `FreestylerService`.
- `CrewService.findAll` include anche i `Freestyler` ma senza i loro campi specifici.
- Id ripetuto nella stessa lista di `socials` non controllato dal merge.
- `judges` e `hosts` salvati come `Set`: nessun ordine.
- L'ordine a pari punteggio dipende dalla collation del database.
- Scala della colonna `score` non fissata.
- `alias` con `@NotNull`: una stringa vuota passa (`@NotBlank` non deciso, già nel dump precedente).
- Credenziali `root` / `1234` nel repo (già nel dump precedente), typo "Jacskon" in `application.properties`, esito dell'errore "duplicate groupId/artifactId" del language server (non confermato).
- L'utente ha detto "ho provato e compila": questo non dice che l'app parta. I drop delle tabelle (sezione 13) non sono confermati.

---

## 13. Da fare sul database prima del primo avvio (se le tabelle erano già state create)

`ddl-auto=update` non rimuove tabelle, colonne né vincoli, e non cambia i tipi.

- **`event_judges` ed `event_hosts` col vecchio schema** (con `crew_id` e chiave esterna): vanno droppate, ora hanno colonna `alias`.
- **`recurring_event`** e le sue due tabelle collegate, se erano state create con il nome `RecurringEvent` (ora `BarreFaulEventOG`, tabelle `barre_faul_event_og*`).
- **`freestyler.score`**: da `int` a decimale. Va modificata la colonna o droppata la tabella. I sette contatori nuovi li aggiunge da solo.
- **Righe esistenti di `social`**: `display_order` sarebbe nullo, vanno cancellate o aggiornate a mano.
