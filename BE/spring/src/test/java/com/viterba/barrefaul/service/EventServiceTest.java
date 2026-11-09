package com.viterba.barrefaul.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.EventMapperImpl;
import com.viterba.barrefaul.repository.EventRepository;

// Contesto minimo: service e mapper generato reali, solo il repository è un mock
@SpringJUnitConfig(classes = {EventService.class, EventMapperImpl.class})
class EventServiceTest {

    @MockitoBean
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    // findAll

    // L'ordinamento per data è del repository (test sul database): qui si verifica che il service lo rispetti
    @Test
    void findAll_keepsRepositoryOrder() {
        when(eventRepository.findAllByOrderByDateDesc()).thenReturn(List.of(
                event(3L, "Terzo", LocalDate.of(2026, 11, 11)),
                event(1L, "Primo", LocalDate.of(2026, 11, 4)),
                event(2L, "Secondo", LocalDate.of(2026, 10, 28))));

        List<EventResponse> result = eventService.findAll();

        assertThat(result).extracting(response -> response.id()).containsExactly(3L, 1L, 2L);
    }

    @Test
    void findAll_emptyRepositoryReturnsEmptyList() {
        when(eventRepository.findAllByOrderByDateDesc()).thenReturn(List.of());

        assertThat(eventService.findAll()).isEmpty();
    }

    // findById

    @Test
    void findById_mapsAllFields() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent()));

        EventResponse response = eventService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Old");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(response.advertisedStartTime()).isEqualTo(LocalTime.of(21, 30));
        assertThat(response.selectionsStartTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(response.location()).isEqualTo("Old location");
        assertThat(response.judges()).containsExactly("OldJudge");
        assertThat(response.hosts()).containsExactly("OldHost");
    }

    @Test
    void findById_unknownIdThrowsNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event")
                .hasMessageContaining("99");
    }

    // create

    @Test
    void create_mapsFieldsAndCollections() {
        stubSaveReturnsArgument();
        EventRequest request = new EventRequest("Barre Faul", LocalDate.of(2026, 11, 11), LocalTime.of(21, 30),
                LocalTime.of(22, 0), "Piazza", Set.of("Judge1", "Judge2"), Set.of("Host1"));

        EventResponse response = eventService.create(request);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Barre Faul");
        assertThat(saved.getLocation()).isEqualTo("Piazza");
        assertThat(saved.getJudges()).containsExactlyInAnyOrder("Judge1", "Judge2");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(response.advertisedStartTime()).isEqualTo(LocalTime.of(21, 30));
        assertThat(response.selectionsStartTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(response.judges()).containsExactlyInAnyOrder("Judge1", "Judge2");
        assertThat(response.hosts()).containsExactly("Host1");
    }

    // Punto aperto dei dump: con judges e hosts null il mapper non deve lasciare le collezioni a null
    @Test
    void create_withNullCollectionsAndNoSelectionsTimeHasEmptySets() {
        stubSaveReturnsArgument();
        EventRequest request = new EventRequest("Barre Faul", LocalDate.of(2026, 11, 11), LocalTime.of(21, 30),
                null, "Piazza", null, null);

        EventResponse response = eventService.create(request);

        assertThat(response.selectionsStartTime()).isNull();
        assertThat(response.judges()).isEmpty();
        assertThat(response.hosts()).isEmpty();
    }

    // update

    @Test
    void update_replacesScalarFieldsAndNullClearsSelectionsStartTime() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent()));
        EventRequest request = new EventRequest("New", LocalDate.of(2026, 11, 11), LocalTime.of(21, 0),
                null, "New location", null, null);

        EventResponse response = eventService.update(1L, request);

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(response.advertisedStartTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(response.selectionsStartTime()).isNull();
        assertThat(response.location()).isEqualTo("New location");
    }

    @Test
    void update_nullJudgesAndHostsLeaveCollectionsUnchanged() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent()));
        EventRequest request = new EventRequest("Old", LocalDate.of(2026, 11, 4), LocalTime.of(21, 30),
                null, "Old location", null, null);

        EventResponse response = eventService.update(1L, request);

        assertThat(response.judges()).containsExactly("OldJudge");
        assertThat(response.hosts()).containsExactly("OldHost");
    }

    @Test
    void update_replacesJudgesAndHosts() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent()));
        EventRequest request = new EventRequest("Old", LocalDate.of(2026, 11, 4), LocalTime.of(21, 30),
                null, "Old location", Set.of("New1", "New2"), Set.of("NewHost"));

        EventResponse response = eventService.update(1L, request);

        assertThat(response.judges()).containsExactlyInAnyOrder("New1", "New2");
        assertThat(response.hosts()).containsExactly("NewHost");
    }

    @Test
    void update_emptySetsClearJudgesAndHosts() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent()));
        EventRequest request = new EventRequest("Old", LocalDate.of(2026, 11, 4), LocalTime.of(21, 30),
                null, "Old location", Set.of(), Set.of());

        EventResponse response = eventService.update(1L, request);

        assertThat(response.judges()).isEmpty();
        assertThat(response.hosts()).isEmpty();
    }

    @Test
    void update_unknownIdThrowsNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        EventRequest request = new EventRequest("Barre Faul", LocalDate.of(2026, 11, 11), LocalTime.of(21, 30),
                null, "Piazza", null, null);

        assertThatThrownBy(() -> eventService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event")
                .hasMessageContaining("99");
    }

    // delete

    @Test
    void delete_removesExistingEvent() {
        Event event = event(1L, "Barre Faul", LocalDate.of(2026, 11, 4));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.delete(1L);

        verify(eventRepository).delete(event);
    }

    @Test
    void delete_unknownIdThrowsNotFoundAndDeletesNothing() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    // Helper

    private void stubSaveReturnsArgument() {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Event event(Long id, String name, LocalDate date) {
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setDate(date);
        return event;
    }

    // Evento con tutti i campi valorizzati, un giudice e un conduttore
    private static Event existingEvent() {
        Event event = event(1L, "Old", LocalDate.of(2026, 11, 4));
        event.setAdvertisedStartTime(LocalTime.of(21, 30));
        event.setSelectionsStartTime(LocalTime.of(22, 0));
        event.setLocation("Old location");
        event.getJudges().add("OldJudge");
        event.getHosts().add("OldHost");
        return event;
    }

}