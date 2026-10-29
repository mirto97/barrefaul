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

    // boh per ora facciamo che mettono l'url dell'immagine e poi in futuro vediamo come farglielo mettere
    private String image;

}