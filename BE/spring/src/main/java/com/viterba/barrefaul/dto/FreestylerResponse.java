package com.viterba.barrefaul.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

public record FreestylerResponse(

    Long id,
    String alias,
    String firstName,
    String lastName,
    String bio,
    Set<Role> roles,
    List<SocialResponse> socials,
    BigDecimal score,
    int participations,
    int firstPlaces,
    int secondPlaces,
    int semifinals,
    int quarterfinals,
    int roundOf16,
    int selections,
    int jury,

    // Calcolato dal service alla lettura, non è nell'entity
    Integer ranking

) {
    
}
