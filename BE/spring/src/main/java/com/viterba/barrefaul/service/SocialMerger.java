package com.viterba.barrefaul.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.entity.Social;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.SocialMapper;

import lombok.RequiredArgsConstructor;

// bean per mergeare i social durante gli update, usato da freestyler e crew
@Component
@RequiredArgsConstructor
public class SocialMerger {

    private final SocialMapper socialMapper;

    // Merge per id: aggiorna i social con id, crea quelli senza id, elimina quelli assenti dalla lista.
    // L'ordine di visualizzazione è la posizione nella lista
    public void merge(Crew crew, List<SocialRequest> requests) {
        Map<Long, Social> existing = crew.getSocials().stream()
                .collect(Collectors.toMap(social -> social.getId(), Function.identity()));

        List<Social> merged = new ArrayList<>(requests.size());
        for (SocialRequest request : requests) {
            if (request.id() == null) {
                merged.add(socialMapper.toEntity(request));
                continue;
            }
            Social social = existing.get(request.id());
            if (social == null) {
                throw new ResourceNotFoundException("Social", request.id());
            }
            socialMapper.updateEntity(request, social);
            merged.add(social);
        }

        crew.getSocials().clear();
        crew.getSocials().addAll(merged);
    }

}