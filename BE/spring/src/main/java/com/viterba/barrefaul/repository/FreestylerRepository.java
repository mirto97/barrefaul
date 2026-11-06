package com.viterba.barrefaul.repository;

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
    long countDistinctScoresGreaterThan(@Param("score") int score);

}
