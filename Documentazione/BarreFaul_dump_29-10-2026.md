# BarreFaul dump 2026-10-29

- **Data dump**: giovedì 29 ottobre 2026
- **Ora dump**: non disponibile (l'ora non è nota a Claude, da aggiungere a mano se serve)
- **Riferimenti temporali nella sessione**: i log Maven incollati dall'utente sono datati 2026-10-27 18:14 e 2026-10-27 18:45
- **Ambito**: solo il delta di questa conversazione (primo dump del progetto, quindi non esistono dump precedenti)
- **Natura del file**: archivio esteso, non riassunto. Include decisioni, motivazioni, codice, errori e fraintendimenti chiariti
- **Nota sul codice**: il codice qui sotto è ricostruito dalla conversazione. L'utente ha applicato in locale modifiche e correzioni dei commenti comunicate solo a parole (es. commenti originali ripristinati, `participations` passato a `int`). In caso di differenze **prevalgono i file locali dell'utente**

---

## 1. Progetto e regole di lavoro

### 1.1 Progetto
Sito web per un'associazione che gestisce eventi nella Tuscia viterbese. Backend Spring Boot, frontend Angular. Il nome del progetto è `barrefaul`.

### 1.2 Regole di lavoro (ribadite o emerse in questa sessione)
- L'utente è "la mente", Claude è "il braccio": l'utente decide, Claude scrive il codice.
- Nomi di classi, campi, ecc. in **inglese**, commenti in **italiano**.
- Risposte veloci e dirette, senza ripetere quanto già deciso.
- **Non ripetere il codice** quando l'utente dice di averlo già scritto o di starlo scrivendo: basta confermare.
- **Non modificare né parafrasare i commenti dell'utente** quando si riscrive il suo codice: restano esattamente come li ha scritti.
- L'utente scrive spesso in **pseudocodice** (es. `String?`, `notblank`, `private LocalTime "ora inizio serata"`): Claude lo interpreta e lo riscrive in Java corretto, chiedendo solo se qualcosa non è chiaro.
- Non supporre né dedurre: quando il messaggio è ambiguo ("proseguiamo", "ottimo così") si chiede quale strada scegliere.
- Quando l'utente chiede di commentare il codice, commenti asciutti da codice di progetto.
- Se i `dump` sono più d'uno, vale il più recente. Questo è il primo dump.

### 1.3 Fraintendimenti chiariti durante la sessione
- **Vecchie entità** (`Crew`, `Freestyler`, `Role`, `Social`): l'utente le ha incollate come spunti da un progetto precedente, da rivedere e rifare da zero. Claude inizialmente le ha trattate come codice già presente nel progetto e ha chiesto del perimetro rispetto ai requisiti.
- **Rename `CREW` → `CREW_MEMBER`**: riguarda solo il valore dell'enum `Role`. L'entità `Crew` non esisteva ancora, e inizialmente Claude aveva chiesto se riguardasse l'entità.
- **Tipo `Long` / `Integer`**: l'utente voleva `Integer` "per abitudine" riferendosi al tipo generico di `JpaRepository`, non all'id. Chiarito che il tipo del repository deve coincidere con quello del campo `@Id`: si è deciso di lasciare `Long` ovunque.
- **IDE**: Claude aveva ipotizzato Eclipse/STS, ma l'utente usa **VS Code** (estensione Red Hat Java).
- **`ranking`**: l'utente ha precisato che "stesso score" a pari punteggio significa stessa posizione (interpretazione di Claude: confermata dall'esempio 1, 1, 2).

---

## 2. Requisiti funzionali

### 2.1 Contenuto pubblico
Il visitatore deve poter vedere: lista eventi, dettaglio evento, chi siamo, contatti, galleria, news.

### 2.2 Gestione e utenti
- Gli eventi li inserisce e li modifica **un solo admin**.
- Il sito è **solo di consultazione** per i visitatori: nessuna registrazione, area riservata solo all'admin.
- **Prenotazione di un posto all'evento**: rimandata "molto più avanti".
- Gli eventi sono **solo informativi**: nessuna iscrizione né pagamento online per ora.
- Lingua del sito: **solo italiano**.

### 2.3 Dati di un evento (requisito iniziale)
Titolo, data, luogo, descrizione, immagini, prezzo, categoria. L'utente ha precisato "per ora, poi modificheremo in base alle esigenze".

### 2.4 Galleria e news
- Galleria **generale** dell'associazione, più una galleria **per ogni singolo evento**.
- News: sezione piccola, separata dagli eventi.

### 2.5 Eventi
- Ogni evento ha una **data singola** (niente eventi su più giorni).
- **Barre Faul**: evento fisso, **ogni mercoledì alle 21:30**, sempre lo stesso, con quel nome. Ogni 3-4 mesi si fanno altri eventi diversi.
- Barre Faul **non viene mai annullato né spostato**. Al massimo cambia location, e succede quando piove (ci si sposta in un posto al chiuso).
- Modellazione concordata: la location di Barre Faul è **fissa**, non si gestisce alcun override per data. In più c'è una **location alternativa in caso di pioggia**, **sempre la stessa**, mostrata come semplice informazione.
- Quindi la regola ricorrente ha **due campi location**: principale e alternativa (pioggia).
- **Da confermare**: Claude aveva proposto due approcci (regola ricorrente come entità a parte, oppure eventi normali inseriti a mano ogni mercoledì con un "duplica evento"). L'utente non ha scelto esplicitamente tra i due, ha risposto sulle eccezioni. Claude ha assunto la regola ricorrente come entità. L'entità non è ancora stata scritta.

---

## 3. Setup tecnico

### 3.1 Scelte
| Voce | Scelta |
|---|---|
| Build tool | Maven |
| Database | MySQL |
| Repo | Monorepo |
| Spring Boot | 4.1.2 |
| Java | **21** (Temurin 21.0.10) |
| Packaging | `war` (confermato dall'utente) |
| `groupId` / `artifactId` | `com.viterba` / `barrefaul` |
| Package base | `com.viterba.barrefaul` |
| Schema DB | Hibernate con `ddl-auto=update` |
| Angular | Versione da decidere più avanti |
| Security | Da abilitare più tardi |
| Flyway | Eventualmente in futuro. Per ora si usa Hibernate (dipendenze necessarie: `flyway-core` e `flyway-mysql`) |

### 3.2 Pom
- Il pom iniziale era Spring Boot 4.0.6 con Java 21. L'utente ha poi reinizializzato Spring: nuovo pom con Spring Boot **4.1.2** e Java 27.
- **Java 27 era un errore dell'utente** (voleva metterlo ma non aveva il JDK): `mvnw clean compile` dava `release version 27 not supported` e a runtime la classe compilata a livello 71 non girava sul runtime dell'estensione Java di VS Code (livello 69, Java 25). Si è tornati a **Java 21**, la build è `SUCCESS`.
- **Lombok**: la property `lombok.version` (1.18.36) è stata **rimossa** dal pom e il tag `<version>` è stato tolto dalla dipendenza, così la versione è gestita dal parent di Spring Boot. Verificato con `.\mvnw dependency:tree`: Lombok risolto alla **1.18.48**. L'`annotationProcessorPaths` non è stato toccato, e continua a usare `${lombok.version}`, che ora arriva dal parent.
- **jjwt**: resta alla 0.11.5, si passerà alla **0.12.x** insieme all'abilitazione della security.
- **Jackson doppio**: Spring Boot 4 usa Jackson 3 (`tools.jackson`), `jjwt-jackson` porta Jackson 2 (`com.fasterxml.jackson`). Convivono (package diversi) e non danno errori ora. **Da verificare la serializzazione quando si abiliterà il JWT.**
- Dipendenze già nel pom: web, springdoc-openapi 3.0.3, data-jpa, validation, mysql-connector-j, mapstruct 1.6.3, lombok, jjwt, devtools, test. `spring-boot-starter-security` resta commentata.

### 3.3 Ambiente
- Maven non è nel PATH: si usa il wrapper `.\mvnw` dalla cartella del `pom.xml`.
- `java -version`: Temurin 21.0.10 LTS. `JAVA_HOME` non è comparso nell'output mostrato.
- VS Code usa di default il runtime incluso nell'estensione Red Hat (Java 25). Funziona perché 25 ≥ 21. Opzionale: puntare al JDK 21 con `java.configuration.runtimes`.
- L'errore `Unresolved compilation problem: SpringApplication cannot be resolved` era legato ai problemi di classpath/versione Java di cui sopra.

### 3.4 Repository Git
- Repo GitHub: `https://github.com/mirto97/barrefaul.git`
- `git init` fatto nella cartella **`Barre Faul sito`** (radice del monorepo, con `BE` e in futuro `FE`).
- Il `pom.xml` è stato **spostato** da `BE\spring\barrefaul\` a `BE\spring\`: `src`, `mvnw` e `.mvn` devono stare nella stessa cartella del pom (build verificata da `BE\spring`).
- Errore del language server "1 Maven project(s) could not be imported due to duplicate groupId/artifactId": al controllo risultava un solo `pom.xml` (in `BE\spring`) e si è ipotizzata una cache dell'IDE. **Lo stato finale dell'errore non è stato confermato** dall'utente.

### 3.5 Impostazioni VS Code (settings.json, definitive)
```jsonc
{
    // Analisi dei null basata sulle annotation (@Nullable, @NonNull): si attiva da sola se rileva quelle annotation nel progetto
    "java.compile.nullAnalysis.mode": "automatic",

    // Aggiorna la configurazione del progetto da solo quando cambia il pom.xml, senza chiedere conferma
    "java.configuration.updateBuildConfiguration": "automatic",

    // Se la build ha errori di compilazione il debug non parte, invece di avviare l'app con classi incomplete
    "java.debug.settings.onBuildFailureProceed": false,

    "editor.codeActionsOnSave": {
        // Aggiunge/ordina gli import e rimuove quelli inutilizzati, sia al salvataggio manuale sia all'auto save
        "source.organizeImports": "always"
    },

    // Salva in automatico quando l'editor perde il focus (cambio file o finestra)
    "files.autoSave": "onFocusChange",
}
```
- Scorciatoie: **`Shift + Alt + O`** organizza gli import, **`Ctrl + .`** quick fix su una classe mancante.
- `"explicit"` esegue l'organizzazione solo al salvataggio manuale. `"always"` la esegue anche agli auto save da cambio finestra/focus (da documentazione VS Code). Con `afterDelay` il comportamento non è garantito.
- Rischio noto: gli import inutilizzati vengono rimossi in silenzio se un salvataggio parte prima di usarli.
- `onBuildFailureProceed` è stato messo a `false` dall'utente, perché con `true` l'app parte anche con errori e genera "Unresolved compilation problem".

### 3.6 `application.properties`
Stato finale (rispetto all'originale è stata tolta `spring.jpa.show-sql=true`):
```properties
spring.application.name=barrefaul

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/barrefaul?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver


# Hibernate settings for automatic schema creation
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true

# Spring Data REST properties: per le rest api
# spring.data.rest.base-path=/api

# custom path per swagger
springdoc.swagger-ui.path=/swagger
springdoc.swagger-ui.enabled=false

# custom path per API docs (così per il json, con .yaml per lo yaml)
springdoc.api-docs.path=/apidocs
springdoc.api-docs.enabled=false

# impostazione di Jacskon per produrre i json: non mandare i valori nulli
spring.jackson.default-property-inclusion=non_null

# Show JPA/Hibernate logging messages
logging.level.org.hibernate.SQL=trace
logging.level.org.hibernate.orm.jdbc.bind=trace

# spring security impostazione per vedere solo i log
logging.level.org.springframework.security=debug

# impostazione per ridurre il logging e tenere solo i warn
logging.level.root=warn

# per togliere un avviso di spring boot di routine
spring.jpa.open-in-view=false
```
Decisioni sui punti segnalati da Claude:
1. **Credenziali** `root` / `1234` nel repo: l'utente le lascia **per ora** (rischio noto: finiscono su GitHub; alternative proposte: variabili d'ambiente o `application-local.properties` fuori dal `.gitignore`, e un utente DB dedicato al posto di `root`).
2. **`spring.jackson.default-property-inclusion`**: verificato via ricerca, la property **resta valida con Jackson 3 in Spring Boot 4**. Il prefisso `spring.jackson2.*` serve solo se si usa Jackson 2.
3. **Log SQL**: tenuto solo il logging di Hibernate (`org.hibernate.SQL` + `org.hibernate.orm.jdbc.bind=trace`, che mostra anche i valori dei parametri). Tolto `show-sql` (stampa su stdout senza parametri, causava output doppio). `format_sql` resta. `logging.level.root=warn` non è stato toccato.
4. **springdoc**: swagger e api-docs sono `enabled=false`, l'utente li abiliterà quando servono.
- Typo nel commento "Jacskon" segnalato, correzione non confermata.
- `ddl-auto=update` coerente con la scelta di usare Hibernate per ora.

---

## 4. Modello dati (package `com.viterba.barrefaul.entity`)

### 4.1 Decisioni sul vecchio tentativo
Corrette tutte le segnalazioni di Claude, approvate dall'utente:
- Nomi in **inglese** (`nome` → `firstName`, `cognome` → `lastName`, `punteggio` → `score`).
- Campo `Ranking` maiuscolo → minuscolo (poi il campo è stato eliminato, vedi 4.5).
- `@Data` su entità JPA **sostituito** con `@Getter` / `@Setter`: `@Data` genera `equals`/`hashCode` su tutti i campi e dà problemi con ereditarietà e proxy Hibernate.
- `int id` → wrapper `Long` (un primitivo non permette di distinguere un'entità non ancora salvata).
- `List<Role>` → `Set<Role>`: i ruoli non hanno ordine né duplicati.
- Il costruttore di `Freestyler` che aggiunge il ruolo viene **ripreso**.
- Il collegamento `Crew`–`Social` (commentato nel vecchio codice) viene riattivato.

### 4.2 `Role`
- Unione di ruoli di scena (Barre Faul) e ruoli lato server: scelta dell'utente.
- `ORGANIZER` si **tiene per il futuro** ("tanto male non fa"). La sovrapposizione con `ADMIN`, `CEO`, `CREW_MEMBER` non è un problema perché i ruoli sono una collezione.
- Rinominato `CREW` → `CREW_MEMBER` (convenzione Java: underscore per le costanti composte).
- Per Spring Security servirà il prefisso `ROLE_` (es. `ROLE_ADMIN`): si gestisce al momento della security, l'enum non cambia.

```java
package com.viterba.barrefaul.entity;

public enum Role {

    // ruoli bfaul
    CEO,
    HOST,
    FREESTYLER,
    CREW_MEMBER,   // membri del collettivo
    LOVER,         // pubblico e appassionati

    // ruoli server
    USER,
    ADMIN,
    ORGANIZER

}
```

### 4.3 `Crew`
Percorso della decisione: l'utente voleva prima una classe astratta `Person` da estendere con `Crew`; poi ha preferito partire da **`Crew` concreta** e fare sottoclassi per i ruoli con campi specifici. Claude ha segnalato il limite: una riga appartiene a **una sola sottoclasse**, quindi le sottoclassi hanno senso solo per ruoli con campi propri, gli altri restano valori nel `Set<Role>`.

- Strategia di ereditarietà: **`JOINED`** ("rimaniamo sempre su joined").
- Nome della base: `Crew`.
- `alias` obbligatorio (`@NotNull`), `firstName` e `lastName` nullable.
- Idea **rimandata**: lista di alias per lo stesso membro (stile Genius).
- Campi **`private`**. Motivo: `protected` in Java è visibile a tutto il package, quindi altre entità potrebbero aggirare getter e setter, e con i proxy lazy di Hibernate la lettura diretta del campo restituirebbe `null` senza inizializzare. Le sottoclassi usano i getter Lombok. L'utente non ha obiettato (se preferisce `protected` basta cambiare il modificatore).
- `bio` resta a **255 caratteri** (default `String`): **da allungare in futuro**, annotato dall'utente.
- `roles` è lazy: con `spring.jpa.open-in-view=false` va letto **dentro una transazione** (service).
- Tabella ruoli: `crew_roles`, colonna `role`, join `crew_id`.
- `socials`: `@OneToMany` con cascade e `orphanRemoval`, perché ogni `Social` appartiene a un solo membro (con `link` dentro `Social` un `@ManyToMany` non ha senso). `@JoinColumn(nullable = false)` impedisce un `Social` senza `Crew`.
- `List<Social>` senza `@OrderColumn`: l'ordine **non viene salvato**. Se servirà un ordine di visualizzazione dei link, si aggiunge.

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
    private List<Social> socials = new ArrayList<>();

}
```

### 4.4 `Social`
- Rappresenta un link social o di contatto (ig, yt, spotify, soundcloud, sito, linktree, email, telefono, whatsapp...). Nome e immagine li sceglie chi lo inserisce, come i link di DistroKid dell'hyperfollow.
- `name` e `link` obbligatori (`@NotBlank`), `image` facoltativo.
- `image` è per ora un **URL**; la gestione del caricamento delle immagini verrà decisa in futuro.
- `link` e `image` restano a 255 caratteri: **da rivedere insieme alla gestione dell'upload** (gli URL possono superarli).
- **Nessun `SocialRepository`**: si gestisce solo passando da `Crew` (cascade). L'utente ha confermato.

```java
package com.viterba.barrefaul.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Social {

    // classe che rappresenta il social che poi abbineremo all'url
    // ig, yt, spoti, soundcloud... oppure solo un sito, un linktree, una email, ma anche numero di telefono o whatsapp account...
    // lo mettono loro associando immagine e nome (alla stringa), tipo i link di distrokid dell'hyperfollow

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String link;

    private String image;
    // boh per ora facciamo che mettono l'url dell'immagine e poi in futuro vediamo come farglielo mettere

}
```

### 4.5 `Freestyler`
- **Sottoclasse di `Crew`** (non di un eventuale `Member`). L'utente ha accantonato la classe `Member`: se in futuro servirà, `Freestyler extends Member`.
- `score`: `int` con default 0 (scelta consapevole dell'utente).
- `participations`: partecipazioni **totali** per ora, `int` con default 0 (l'utente l'ha cambiato da `Integer` perché "più veloce da incrementare"). In futuro vorrebbe una versione **annuale**, che non sarà più un singolo contatore ma probabilmente una tabella a parte (conteggio per anno).
- Nome scelto per il campo: `participations` (alternative scartate: `appearances`, `eventsAttended`).
- **`ranking` NON è un campo dell'entity**: è calcolato alla lettura (vedi sezione 7).
- Il costruttore aggiunge `Role.FREESTYLER` al set.
- Le statistiche dei piazzamenti restano commentate, da introdurre più avanti.

```java
package com.viterba.barrefaul.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Freestyler extends Crew {

    // punteggio attuale
    private int score = 0;

    // partecipazioni (iniziamo totale, ma vorrei farla annuale)
    private int participations = 0;

    /* meglio introdurlo più avanti, ci semplifichiamo le cose adesso
    // quante volte si è guadagnato queste posizioni nel torneo settimanale
    private Integer primoPosto;
    private Integer secondoPosto;
    private Integer semifinale;
    // quarti di finale
    private Integer quarti;
    */

    // aggiungo il ruolo di freestyler nel costruttore
    public Freestyler() {
        getRoles().add(Role.FREESTYLER);
    }

}
```

### 4.6 `Event`
- Classe base, estendibile in futuro da classi più specifiche. Nessun `@Inheritance` per ora: la strategia si sceglie quando nasceranno le sottoclassi.
- Campi in inglese: `advertisedStartTime` (ora inizio serata da locandina; alternativa scartata `flyerStartTime`) e `selectionsStartTime` (ora inizio selezioni; alternativa scartata `qualifiersStartTime`).
- `judges` e `hosts`: **`@ManyToMany`** verso `Crew` (lo stesso membro può giudicare o condurre più eventi). Usati **`Set`** invece di `List` (con una `List` in una `@ManyToMany` Hibernate cancella e reinserisce tutte le righe della relazione a ogni modifica).
- **`@JoinTable` esplicite** `event_judges` ed `event_hosts` (con nomi di default entrambe le relazioni finirebbero nella stessa tabella `event_crew`).
- Validazione: `name` `@NotBlank` (copre già il `null`, `@NotNull` ridondante tolto), `date` `@NotNull`, `advertisedStartTime` `@NotNull`, `selectionsStartTime` facoltativo, `location` `@NotBlank`. Hibernate applica questi vincoli anche allo schema (colonne `NOT NULL`).
- Campi futuri lasciati in un commento: `sponsors`, `tds` (`List<Freestyler>`), `dj` (`List<Crew>`). Corretto il typo `sporsors` → `sponsors`.

```java
package com.viterba.barrefaul.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @ManyToMany
    @JoinTable(
        name = "event_judges",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "crew_id")
    )
    private Set<Crew> judges = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "event_hosts",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "crew_id")
    )
    private Set<Crew> hosts = new HashSet<>();

    @NotBlank
    private String location;

    /* da aggiungere poi
    private List<?> sponsors;
    private List<Freestyler> tds;
    private List<Crew> dj;
    */

}
```

---

## 5. Repository (package `com.viterba.barrefaul.repository`)

- `JpaRepository<..., Long>` per `Crew`, `Freestyler` ed `Event`, senza query custom (non ancora decise).
- **Senza `@Repository`**: ridondante, Spring Boot attiva Spring Data JPA da solo, che rileva le interfacce che estendono `JpaRepository` (scansione dal package della classe principale, sottopackage inclusi) e applica già la traduzione delle eccezioni.
- Il tipo del secondo parametro deve coincidere con il tipo del campo `@Id` dell'entità: **`Long` ovunque** (valutato e scartato `Integer`, anche se basterebbe: la differenza `INT`/`BIGINT` è trascurabile e cambiarlo dopo costerebbe la modifica di id e chiavi esterne, con `ddl-auto=update` inaffidabile sui cambi di tipo).
- `FreestylerRepository` usa `Long` perché eredita l'id da `Crew`.

```java
package com.viterba.barrefaul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Crew;

public interface CrewRepository extends JpaRepository<Crew, Long> {

}
```

```java
package com.viterba.barrefaul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Freestyler;

public interface FreestylerRepository extends JpaRepository<Freestyler, Long> {

}
```

```java
package com.viterba.barrefaul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
```

---

## 6. DTO e mapper

### 6.1 Decisioni
- DTO come **`record`**, in `com.viterba.barrefaul.dto`.
- **Request e response separati** (la request non ha l'`id`).
- **DTO separati per `Freestyler`**, non un DTO polimorfico. Motivazioni confrontate:
  - Separati: semplici, nessuna configurazione Jackson, un endpoint per tipo. Contro: un campo aggiunto a `Crew` va cambiato in due punti, e una lista mista di `Crew` perde i campi di `Freestyler`.
  - Polimorfico (interfaccia sealed): un endpoint per lista mista. Contro: configurazione `@JsonTypeInfo`/`@JsonSubTypes`, `@SubclassMapping` in MapStruct, discriminatore lato Angular. Inoltre l'utente non è ancora sicuro di tenere `Freestyler` come sottoclasse.
- **Validazione anche sui request DTO** (stesse annotation delle entity). Senza, l'errore arriva tardi come eccezione di persistenza e l'API risponde con un 500 generico invece di un 400 sui campi sbagliati. Per attivarla nei controller serve `@Valid` (da mettere quando si scrivono).
- Nei DTO di `Event`, `judges` e `hosts` usano un **riassunto** (`CrewSummary`: `id` e `alias`). In scrittura arrivano come **liste di id** (`judgeIds`, `hostIds`).
- L'utente vuole **anche un `EventDto` alternativo** in futuro: per ora solo quello descritto sopra.
- `Social` è un DTO annidato dentro `Crew` e `Freestyler`.
- Mapper: **MapStruct** con `componentModel = "spring"`, in `com.viterba.barrefaul.mapper`.

### 6.2 DTO
```java
package com.viterba.barrefaul.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialRequest(

    @NotBlank
    String name,

    @NotBlank
    String link,

    String image

) {

}
```

```java
package com.viterba.barrefaul.dto;

public record SocialResponse(

    Long id,
    String name,
    String link,
    String image

) {

}
```

```java
package com.viterba.barrefaul.dto;

import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CrewRequest(

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

import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

public record CrewResponse(

    Long id,
    String alias,
    String firstName,
    String lastName,
    String bio,
    Set<Role> roles,
    List<SocialResponse> socials

) {

}
```

```java
package com.viterba.barrefaul.dto;

public record CrewSummary(

    Long id,
    String alias

) {

}
```

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

    // Id dei membri, risolti nel service
    Set<Long> judgeIds,

    Set<Long> hostIds

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
    Set<CrewSummary> judges,
    Set<CrewSummary> hosts

) {

}
```

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

    List<@Valid SocialRequest> socials,

    int score,

    int participations

) {

}
```

```java
package com.viterba.barrefaul.dto;

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
    int score,
    int participations,

    // Calcolato dal service alla lettura, non è nell'entity
    Integer ranking

) {

}
```

Decisioni su `FreestylerRequest` / `FreestylerResponse`:
- `ranking` **solo in lettura** (calcolato), quindi solo in `FreestylerResponse`.
- `score` e `participations` **scrivibili**.
- **`roles` nella request**: l'admin può assegnare anche altri ruoli (es. `HOST`); il mapper copia il set così com'è (sostituendo quello creato dal costruttore), quindi il **service deve garantire che `FREESTYLER` ci sia sempre**.

### 6.3 Mapper
```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.dto.SocialResponse;
import com.viterba.barrefaul.entity.Social;

@Mapper(componentModel = "spring")
public interface SocialMapper {

    @Mapping(target = "id", ignore = true)
    Social toEntity(SocialRequest request);

    SocialResponse toResponse(Social social);

}
```

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.CrewSummary;
import com.viterba.barrefaul.entity.Crew;

@Mapper(componentModel = "spring", uses = SocialMapper.class)
public interface CrewMapper {

    @Mapping(target = "id", ignore = true)
    Crew toEntity(CrewRequest request);

    CrewResponse toResponse(Crew crew);

    CrewSummary toSummary(Crew crew);

}
```

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;

@Mapper(componentModel = "spring", uses = CrewMapper.class)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "judges", ignore = true)
    @Mapping(target = "hosts", ignore = true)
    Event toEntity(EventRequest request);

    EventResponse toResponse(Event event);

}
```

```java
package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.entity.Freestyler;

@Mapper(componentModel = "spring", uses = SocialMapper.class)
public interface FreestylerMapper {

    @Mapping(target = "id", ignore = true)
    Freestyler toEntity(FreestylerRequest request);

    // Il ranking non è nell'entity: è calcolato dal service e passato qui
    @Mapping(target = "ranking", source = "ranking")
    FreestylerResponse toResponse(Freestyler freestyler, Integer ranking);

}
```

Motivazioni:
- In `EventMapper`, `judges` e `hosts` sono **ignorati** in `toEntity`: il mapper non accede al database, quindi trasformare gli id in `Crew` spetta al **service**, tramite `CrewRepository`.
- In `FreestylerMapper.toResponse` i campi arrivano dal primo parametro e `ranking` dal secondo: la `@Mapping` esplicita su `ranking` è necessaria. Soluzione approvata dall'utente ("ottimo così").
- `alias` ha `@NotNull` come nell'entity: una stringa vuota passa. Se si vuole bloccarla serve `@NotBlank` (non deciso).
- Da verificare in `target/generated-sources` che, con `roles` e `socials` nulli nella request, un valore `null` non sovrascriva le collezioni inizializzate nelle entity.

---

## 7. Ranking dei freestyler

- Il ranking è la **posizione in una lista ordinata per `score` decrescente**.
- **Non è una colonna**: calcolarlo alla lettura evita di ricalcolare tutte le posizioni a ogni modifica di uno score e il rischio di disallineamento.
- **Pari merito**: stessa posizione, **senza saltare** la successiva (1, 1, 2).
- L'idea dell'utente di una mappa `Integer -> List<Freestyler>` come entità aggregatrice: **non persistita** (ricadrebbe nel problema della colonna), ma **calcolata dal service alla lettura**, quindi come DTO e non come entity. La mappa era solo un'idea, la forma non è decisa. Nota tecnica: una `Map<Integer, List<...>>` in JSON diventa un oggetto con chiavi stringa, e l'ordine non è garantito per chi legge. Una lista di elementi con `position` e `freestylers` si itera in ordine.
- Una lista di `FreestylerResponse` ordinata per `score` contiene già le posizioni nel campo `ranking`.
- Il **DTO aggregatore (freestyler raggruppati per posizione) è stato rimandato**: l'utente potrebbe averne bisogno. Rischio del rinvio quasi nullo, perché è un'aggiunta pura (nessun impatto su entity, DB e DTO già scritti), purché venga aggiunto come endpoint separato senza cambiare la forma di quello esistente.

---

## 8. Punti aperti e rimandati

### 8.1 Da fare nel prossimo lavoro
- Service (non ancora scritti): risolvere `judgeIds`/`hostIds` in `Crew` con `CrewRepository`, garantire il ruolo `FREESTYLER`, calcolare il `ranking`.
- Controller, con `@Valid` sui request DTO.
- Entità della **regola ricorrente Barre Faul** (location principale + alternativa in caso di pioggia, ogni mercoledì 21:30) e conferma esplicita del modello (regola a parte oppure eventi normali).
- Non ancora modellati: galleria generale e per evento, news, "chi siamo", contatti, e i dati evento del requisito iniziale (descrizione, immagini, prezzo, categoria), diversi dai campi dell'`Event` scritto finora (giudici, conduttori, orari, location).

### 8.2 Rimandati per scelta dell'utente
- **Angular**: versione da decidere.
- **Flyway** (per ora Hibernate), con `flyway-core` e `flyway-mysql`.
- **Security** (`spring-boot-starter-security` commentata), **jjwt 0.12.x**, verifica Jackson 2/3, prefisso `ROLE_`.
- **Prenotazione posto** agli eventi, "molto più avanti".
- **`bio` oltre 255 caratteri**, **lunghezza `link`/`image`** e gestione upload immagini.
- **Partecipazioni annuali** (probabile tabella a parte per anno).
- **Lista di alias** per `Crew` (stile Genius).
- **Sottoclasse `Member`**, solo se servirà (poi `Freestyler extends Member`).
- **Statistiche piazzamenti** di `Freestyler` (commentate).
- **`sponsors`, `tds`, `dj`** in `Event`.
- **Strategia di ereditarietà per `Event`**, se nasceranno sottoclassi.
- **`EventDto` alternativo**.
- **DTO aggregatore** del ranking.
- **Ordine di visualizzazione** dei `Social` (`@OrderColumn`).
- **Abilitazione di swagger/api-docs** (`springdoc.*.enabled`).

### 8.3 Segnalati da Claude, non risolti
- Credenziali `root` / `1234` nel repo (accettate per ora).
- Typo "Jacskon" nei commenti di `application.properties`.
- `alias` con `@NotNull`: una stringa vuota è accettata (`@NotBlank` non deciso).
- Esito dell'errore "duplicate groupId/artifactId" del language server (non confermato).
- `JAVA_HOME` non verificato; opzionale puntare VS Code al JDK 21 con `java.configuration.runtimes`.
- Java 27 non è LTS e Lombok 1.18.36 non lo supportava: tema chiuso con il ritorno a Java 21.
- Campi di `Crew` scritti `private`: l'utente non ha obiettato, ma non ha confermato esplicitamente.
