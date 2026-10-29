package com.viterba.barrefaul.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record EventResponse(

    Long id,
    String name,
    LocalDate date,
    LocalTime advertisedStartTime,
    LocalTime selectionsStartTime,
    String location,
    Set<CrewSummary> judges,
    Set<CrewSummary> hosts

) {
    
}
