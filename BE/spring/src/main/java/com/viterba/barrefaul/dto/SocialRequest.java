package com.viterba.barrefaul.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialRequest(

    // Null per un nuovo social, valorizzato per aggiornarne uno esistente
    Long id,
    
    @NotBlank
    String name,

    @NotBlank
    String link,

    String image

) {
    
}
