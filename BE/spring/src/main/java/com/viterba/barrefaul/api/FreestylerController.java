package com.viterba.barrefaul.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.service.FreestylerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/freestylers")
@RequiredArgsConstructor
public class FreestylerController {

    private final FreestylerService freestylerService;

    // Lista già ordinata per ranking
    @GetMapping
    public List<FreestylerResponse> findAll() {
        return freestylerService.findAll();
    }

    @GetMapping("/{id}")
    public FreestylerResponse findById(@PathVariable Long id) {
        return freestylerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FreestylerResponse create(@Valid @RequestBody FreestylerRequest request) {
        return freestylerService.create(request);
    }

    @PutMapping("/{id}")
    public FreestylerResponse update(@PathVariable Long id, @Valid @RequestBody FreestylerRequest request) {
        return freestylerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        freestylerService.delete(id);
    }

    // Registrazione dei piazzamenti di una serata: il nuovo ranking si legge poi dalla lista
    @PostMapping("/tournament-results")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerResults(@Valid @RequestBody TournamentResultsRequest request) {
        freestylerService.registerResults(request);
    }

}