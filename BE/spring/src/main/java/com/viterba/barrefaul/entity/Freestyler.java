package com.viterba.barrefaul.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
// L’ereditarietà JOINED è dichiarata su Crew e Freestyler la eredita
public class Freestyler extends Crew {

    // Posizione in classifica
    // private Integer ranking; mo lo calcolo
    // TODO inserire media ponderata ecc...

    // Punteggio attuale
    private int score = 0;

    // partecipazioni (iniziamo totale, ma vorrei farla annuale) - La versione annuale non sarà più un singolo contatore, ma un conteggio per anno (probabilmente una tabella a parte)
    private int participations = 0;

    /* meglio introdurlo più avanti, ci semplifichiamo le cose adesso
    // quante volte si è guadagnato queste posizioni nel torneo settimanale
    private Integer primoPosto;
    private Integer secondoPosto;
    private Integer semifinale;
    // quarti di finale
    private Integer quarti;
    */

    // Assegna in automatico il ruolo FREESTYLER
    public Freestyler() {
        getRoles().add(Role.FREESTYLER);
    }

}