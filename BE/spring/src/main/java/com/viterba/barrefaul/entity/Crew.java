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

    // Nome d'arte, in futuro potremmo fare una lista e metterli vari come su genius
    @NotNull
    private String alias;

    private String firstName;

    private String lastName;

    // così il massimo caratteri è 255, andrà cambiato
    private String bio = "";

    // Però senza @OrderColumn l’ordine non viene salvato. Se servisse un ordine di visualizzazione dei link, lo aggiungiamo.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "crew_id", nullable = false)
    private List<Social> socials = new ArrayList<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "crew_roles", joinColumns = @JoinColumn(name = "crew_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

}