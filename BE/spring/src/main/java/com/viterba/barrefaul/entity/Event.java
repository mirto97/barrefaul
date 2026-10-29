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

    // Classe base, estendibile in futuro da classi più specifiche

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank  
    private String name;

    @NotNull 
    private LocalDate date;

    @NotBlank 
    private String location;

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

    /* da aggiungere poi
    private List<?> sponsors;
    private List<Freestyler> tds;
    private List<Crew> dj;
    */

}