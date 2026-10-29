package com.viterba.barrefaul.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialRequest(

    @NotBlank
    String name,

    @NotBlank
    String link,

    String image

) {
    
}
