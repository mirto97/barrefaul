package com.viterba.barrefaul.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.CrewMapper;
import com.viterba.barrefaul.repository.CrewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: roles è lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class CrewService {

    private final CrewRepository crewRepository;
    private final CrewMapper crewMapper;
    private final SocialMerger socialMerger;

    public List<CrewResponse> findAll() {
        return crewRepository.findAll().stream()
                .map(crewMapper::toResponse)
                .toList();
    }

    public CrewResponse findById(Long id) {
        return crewMapper.toResponse(getCrew(id));
    }

    @Transactional
    public CrewResponse create(CrewRequest request) {
        Crew crew = crewMapper.toEntity(request);
        return crewMapper.toResponse(crewRepository.save(crew));
    }

    @Transactional
    public CrewResponse update(Long id, CrewRequest request) {
        Crew crew = getCrew(id);
        crewMapper.updateEntity(request, crew);
        // Socials null = invariati, lista vuota = tutti rimossi
        if (request.socials() != null) {
            socialMerger.merge(crew, request.socials());
        }
        // Flush immediato: gli id dei nuovi Social vengono valorizzati prima di costruire la response
        crewRepository.flush();
        return crewMapper.toResponse(crew);
    }

    @Transactional
    public void delete(Long id) {
        crewRepository.delete(getCrew(id));
    }

    private Crew getCrew(Long id) {
        return crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", id));
    }

}