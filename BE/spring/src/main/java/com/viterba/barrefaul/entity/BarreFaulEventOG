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

    // TODO vedere come gestirlo

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