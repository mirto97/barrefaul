package com.viterba.barrefaul.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(

    int status,
    String message,
    Instant timestamp,

    // Solo per gli errori di validazione, campo -> messaggio
    Map<String, String> errors

) {
    
}
