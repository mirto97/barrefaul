package com.viterba.barrefaul.dto;

import com.viterba.barrefaul.entity.Placement;

import jakarta.validation.constraints.NotNull;

public record PlacementEntry(

    @NotNull
    Long freestylerId,

    @NotNull
    Placement placement

) {

}