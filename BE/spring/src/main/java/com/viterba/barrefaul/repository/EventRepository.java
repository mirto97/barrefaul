package com.viterba.barrefaul.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    
        // Dal più avanti nel tempo al più indietro
    List<Event> findAllByOrderByDateDesc();
    
}
