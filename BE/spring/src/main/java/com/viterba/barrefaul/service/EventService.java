package com.viterba.barrefaul.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.EventMapper;
import com.viterba.barrefaul.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Transazione necessaria anche in lettura: judges e hosts sono lazy e open-in-view è disattivato
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventResponse> findAll() {
        return eventRepository.findAllByOrderByDateDesc().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponse findById(Long id) {
        return eventMapper.toResponse(getEvent(id));
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = eventMapper.toEntity(request);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEvent(id);
        eventMapper.updateEntity(request, event);
        // Entità già gestita: le modifiche vengono salvate al commit
        return eventMapper.toResponse(event);
    }

    @Transactional
    public void delete(Long id) {
        eventRepository.delete(getEvent(id));
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

}