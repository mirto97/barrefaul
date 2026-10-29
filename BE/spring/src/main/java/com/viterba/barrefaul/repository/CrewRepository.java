package com.viterba.barrefaul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viterba.barrefaul.entity.Crew;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    
}
