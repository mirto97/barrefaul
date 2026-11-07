package com.viterba.barrefaul.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record TournamentResultsRequest(

    // Formato della serata: i due flag sono indipendenti e possono coincidere
    boolean roundOf16Night,

    boolean threeWayFinalNight,

    @NotEmpty
    List<@Valid PlacementEntry> entries

) {

}