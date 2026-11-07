package com.viterba.barrefaul.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.PlacementEntry;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.entity.Freestyler;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.exception.InvalidRequestException;
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

    // Registra i piazzamenti di una serata in un'unica transazione: se un freestyler non esiste o
    // un piazzamento non è valido per il formato, non viene salvato nulla
    @Transactional
    public void registerResults(TournamentResultsRequest request) {
        boolean roundOf16Night = request.roundOf16Night();
        boolean threeWayFinalNight = request.threeWayFinalNight();
        
        // Ogni freestyler può comparire una sola volta per serata
        Set<Long> seenIds = new HashSet<>();
        for (PlacementEntry entry : request.entries()) {
            if (!seenIds.add(entry.freestylerId())) {
                throw new InvalidRequestException(
                        "Freestyler " + entry.freestylerId() + " appears more than once");
            }
            if (!entry.placement().isAllowed(roundOf16Night, threeWayFinalNight)) {
                throw new InvalidRequestException(
                        "Placement " + entry.placement() + " is not allowed for this night format");
            }
        }

        for (PlacementEntry entry : request.entries()) {
            Freestyler freestyler = getFreestyler(entry.freestylerId());
            entry.placement().incrementCounter(freestyler);
            freestyler.setParticipations(freestyler.getParticipations() + 1);
            freestyler.setScore(freestyler.getScore()
                    .add(entry.placement().points(roundOf16Night, threeWayFinalNight)));
        }
        // Entità già gestite: le modifiche vengono salvate al commit
    }

    public List<FreestylerResponse> findAll() {
        List<Freestyler> freestylers = freestylerRepository.findAllByOrderByScoreDescAliasAsc();
        List<FreestylerResponse> responses = new ArrayList<>(freestylers.size());
        int ranking = 0;
        BigDecimal previousScore = BigDecimal.ZERO;
        for (Freestyler freestyler : freestylers) {
            // A pari punteggio la posizione resta la stessa e la successiva non salta (1, 1, 2).
            // compareTo e non equals: equals considera diversi 2.5 e 2.50
            if (responses.isEmpty() || freestyler.getScore().compareTo(previousScore) != 0) {
                ranking++;
            }
            previousScore = freestyler.getScore();
            responses.add(freestylerMapper.toResponse(freestyler, ranking));
        }
        return responses;
    }
    
    // Posizione = numero di punteggi distinti più alti + 1
    private int computeRanking(BigDecimal score) {
        return Math.toIntExact(freestylerRepository.countDistinctScoresGreaterThan(score)) + 1;
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


    private Freestyler getFreestyler(Long id) {
        return freestylerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freestyler", id));
    }

}