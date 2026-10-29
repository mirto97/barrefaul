package com.viterba.barrefaul.dto;

import java.util.List;
import java.util.Set;

import com.viterba.barrefaul.entity.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CrewRequest(

    @NotNull
    String alias,

    String firstName,

    String lastName,

    String bio,

    Set<Role> roles,

    List<@Valid SocialRequest> socials

) {
    
}
