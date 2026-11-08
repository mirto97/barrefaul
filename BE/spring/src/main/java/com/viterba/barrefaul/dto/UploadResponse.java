package com.viterba.barrefaul.dto;

public record UploadResponse(

    // Nome con cui il file è stato salvato, da memorizzare nell'entity che lo usa
    String filename

) {

}