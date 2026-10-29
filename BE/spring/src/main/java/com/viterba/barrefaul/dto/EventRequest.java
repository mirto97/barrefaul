package com.viterba.barrefaul.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(


    @NotBlank
    String name,

    @NotNull
    LocalDate date,

    @NotNull
    LocalTime advertisedStartTime,

    LocalTime selectionsStartTime,

    @NotBlank
    String location,

    // Id dei membri, risolti nel service
    Set<Long> judgeIds,
    Set<Long> hostIds

) {
    
}
