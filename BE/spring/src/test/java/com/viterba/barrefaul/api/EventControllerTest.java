package com.viterba.barrefaul.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.service.EventService;

@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final String VALID_BODY = """
            {
              "name": "Barre Faul",
              "date": "2026-11-11",
              "advertisedStartTime": "21:30",
              "selectionsStartTime": "22:00",
              "location": "Piazza",
              "judges": ["Judge1", "Judge2"],
              "hosts": ["Host1"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void findAll_returnsListInServiceOrder() throws Exception {
        when(eventService.findAll()).thenReturn(List.of(
                response(3L, "Terzo", LocalDate.of(2026, 11, 11)),
                response(1L, "Primo", LocalDate.of(2026, 11, 4)),
                response(2L, "Secondo", LocalDate.of(2026, 10, 28))));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[2].id").value(2));
    }

    @Test
    void findById_returnsAllFieldsAndOmitsNullOnes() throws Exception {
        when(eventService.findById(1L)).thenReturn(new EventResponse(
                1L, "Barre Faul", LocalDate.of(2026, 11, 11), LocalTime.of(21, 30), null, "Piazza",
                Set.of("Judge1"), Set.of("Host1")));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Barre Faul"))
                .andExpect(jsonPath("$.date").value("2026-11-11"))
                .andExpect(jsonPath("$.advertisedStartTime").value("21:30:00"))
                .andExpect(jsonPath("$.location").value("Piazza"))
                .andExpect(jsonPath("$.judges[0]").value("Judge1"))
                .andExpect(jsonPath("$.hosts[0]").value("Host1"))
                .andExpect(jsonPath("$.selectionsStartTime").doesNotExist());
    }

    @Test
    void create_returns201AndPassesMappedRequestToService() throws Exception {
        when(eventService.create(any(EventRequest.class)))
                .thenReturn(response(1L, "Barre Faul", LocalDate.of(2026, 11, 11)));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<EventRequest> captor = ArgumentCaptor.forClass(EventRequest.class);
        verify(eventService).create(captor.capture());
        EventRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo("Barre Faul");
        assertThat(request.date()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(request.advertisedStartTime()).isEqualTo(LocalTime.of(21, 30));
        assertThat(request.selectionsStartTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(request.location()).isEqualTo("Piazza");
        assertThat(request.judges()).containsExactlyInAnyOrder("Judge1", "Judge2");
        assertThat(request.hosts()).containsExactly("Host1");
    }

    @Test
    void create_missingRequiredFieldsReturns400ForEachOfThem() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").isNotEmpty())
                .andExpect(jsonPath("$.errors.date").isNotEmpty())
                .andExpect(jsonPath("$.errors.advertisedStartTime").isNotEmpty())
                .andExpect(jsonPath("$.errors.location").isNotEmpty())
                // selectionsStartTime è facoltativo
                .andExpect(jsonPath("$.errors.selectionsStartTime").doesNotExist());

        verifyNoInteractions(eventService);
    }

    @Test
    void create_blankJudgeAliasReturns400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Barre Faul",
                                  "date": "2026-11-11",
                                  "advertisedStartTime": "21:30",
                                  "location": "Piazza",
                                  "judges": [""]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                // La chiave del campo per gli elementi di un Set non la conosco: si controlla solo che ci sia un errore
                .andExpect(jsonPath("$.errors").isNotEmpty());

        verifyNoInteractions(eventService);
    }

    @Test
    void create_invalidDateFormatReturns400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Barre Faul",
                                  "date": "11/11/2026",
                                  "advertisedStartTime": "21:30",
                                  "location": "Piazza"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid value"));

        verifyNoInteractions(eventService);
    }

    @Test
    void update_returns200AndPassesIdAndRequestToService() throws Exception {
        when(eventService.update(eq(1L), any(EventRequest.class)))
                .thenReturn(response(1L, "Barre Faul", LocalDate.of(2026, 11, 11)));

        mockMvc.perform(put("/api/events/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<EventRequest> captor = ArgumentCaptor.forClass(EventRequest.class);
        verify(eventService).update(eq(1L), captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Barre Faul");
    }

    @Test
    void update_unknownIdReturns404() throws Exception {
        when(eventService.update(eq(99L), any(EventRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event", 99L));

        mockMvc.perform(put("/api/events/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id: 99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNoContent());

        verify(eventService).delete(1L);
    }

    private static EventResponse response(Long id, String name, LocalDate date) {
        return new EventResponse(id, name, date, LocalTime.of(21, 30), null, "Piazza", Set.of(), Set.of());
    }

}