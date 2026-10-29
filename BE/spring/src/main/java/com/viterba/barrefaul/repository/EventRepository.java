package com.viterba.barrefaul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    
}
