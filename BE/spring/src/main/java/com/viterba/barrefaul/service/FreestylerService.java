package com.viterba.barrefaul.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.entity.Freestyler;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.FreestylerMapper;
import com.viterba.barrefaul.repository.FreestylerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: roles è lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class FreestylerService {

    private final FreestylerRepository freestylerRepository;
    private final FreestylerMapper freestylerMapper;
    private final SocialMerger socialMerger;

    public List<FreestylerResponse> findAll() {
        List<Freestyler> freestylers = freestylerRepository.findAllByOrderByScoreDescAliasAsc();
        List<FreestylerResponse> responses = new ArrayList<>(freestylers.size());
        int ranking = 0;
        int previousScore = 0;
        for (Freestyler freestyler : freestylers) {
            // A pari punteggio la posizione resta la stessa e la successiva non salta (1, 1, 2)
            if (responses.isEmpty() || freestyler.getScore() != previousScore) {
                ranking++;
            }
            previousScore = freestyler.getScore();
            responses.add(freestylerMapper.toResponse(freestyler, ranking));
        }
        return responses;
    }

    public FreestylerResponse findById(Long id) {
        return toResponse(getFreestyler(id));
    }

    @Transactional
    public FreestylerResponse create(FreestylerRequest request) {
        Freestyler freestyler = freestylerMapper.toEntity(request);
        // I roles della request sostituiscono quelli del costruttore: il ruolo FREESTYLER va sempre garantito
        freestyler.getRoles().add(Role.FREESTYLER);
        return toResponse(freestylerRepository.save(freestyler));
    }

    @Transactional
    public FreestylerResponse update(Long id, FreestylerRequest request) {
        Freestyler freestyler = getFreestyler(id);
        freestylerMapper.updateEntity(request, freestyler);
        freestyler.getRoles().add(Role.FREESTYLER);
        // Socials null = invariati, lista vuota = tutti rimossi
        if (request.socials() != null) {
            socialMerger.merge(freestyler, request.socials());
        }
        // Flush immediato: gli id dei nuovi Social vengono valorizzati prima di costruire la response
        freestylerRepository.flush();
        return toResponse(freestyler);
    }

    @Transactional
    public void delete(Long id) {
        freestylerRepository.delete(getFreestyler(id));
    }

    private FreestylerResponse toResponse(Freestyler freestyler) {
        return freestylerMapper.toResponse(freestyler, computeRanking(freestyler.getScore()));
    }

    // Posizione = numero di punteggi distinti più alti + 1
    private int computeRanking(int score) {
        return Math.toIntExact(freestylerRepository.countDistinctScoresGreaterThan(score)) + 1;
    }

    private Freestyler getFreestyler(Long id) {
        return freestylerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freestyler", id));
    }

}