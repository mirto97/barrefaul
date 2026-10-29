package com.viterba.barrefaul.dto;

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
    int score,
    int participations,

    // Calcolato dal service alla lettura, non è nell'entity
    Integer ranking

) {
    
}
