package com.viterba.barrefaul.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
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

    // punteggio attuale
    @Column(nullable = false)
    private BigDecimal score = BigDecimal.ZERO;

    // partecipazioni
    private int participations = 0;

    // Contatori dei piazzamenti
    private int firstPlaces = 0;
    private int secondPlaces = 0;
    private int semifinals = 0;
    private int quarterfinals = 0;
    private int roundOf16 = 0;
    private int selections = 0;
    private int jury = 0;
    
    

    // Assegna in automatico il ruolo FREESTYLER
    public Freestyler() {
        getRoles().add(Role.FREESTYLER);
    }

}