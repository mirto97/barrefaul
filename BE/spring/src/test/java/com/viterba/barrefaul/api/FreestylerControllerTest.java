package com.viterba.barrefaul.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.entity.Placement;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.service.FreestylerService;

@WebMvcTest(FreestylerController.class)
class FreestylerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FreestylerService freestylerService;

    @Test
    void findAll_returnsListInServiceOrder() throws Exception {
        when(freestylerService.findAll()).thenReturn(List.of(
                response(1L, "Alfa", "10", 1),
                response(2L, "Bravo", "10", 1),
                response(3L, "Charlie", "7.5", 2)));

        mockMvc.perform(get("/api/freestylers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].alias").value("Alfa"))
                .andExpect(jsonPath("$[1].alias").value("Bravo"))
                .andExpect(jsonPath("$[2].alias").value("Charlie"))
                .andExpect(jsonPath("$[2].score").value(7.5))
                .andExpect(jsonPath("$[2].ranking").value(2))
                // Jackson non serializza i null: nome e cognome non compaiono
                .andExpect(jsonPath("$[0].firstName").doesNotExist());
    }

    @Test
    void findById_returnsAllFields() throws Exception {
        when(freestylerService.findById(1L)).thenReturn(new FreestylerResponse(
                1L, "Alfa", "Mario", "Rossi", "Bio", Set.of(Role.FREESTYLER), List.of(),
                new BigDecimal("12.5"), 8, 1, 2, 3, 4, 5, 6, 7, 3));

        mockMvc.perform(get("/api/freestylers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alias").value("Alfa"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.bio").value("Bio"))
                .andExpect(jsonPath("$.roles[0]").value("FREESTYLER"))
                .andExpect(jsonPath("$.socials.length()").value(0))
                .andExpect(jsonPath("$.score").value(12.5))
                .andExpect(jsonPath("$.participations").value(8))
                .andExpect(jsonPath("$.firstPlaces").value(1))
                .andExpect(jsonPath("$.secondPlaces").value(2))
                .andExpect(jsonPath("$.semifinals").value(3))
                .andExpect(jsonPath("$.quarterfinals").value(4))
                .andExpect(jsonPath("$.roundOf16").value(5))
                .andExpect(jsonPath("$.selections").value(6))
                .andExpect(jsonPath("$.jury").value(7))
                .andExpect(jsonPath("$.ranking").value(3));
    }

    @Test
    void create_returns201AndPassesMappedRequestToService() throws Exception {
        when(freestylerService.create(any(FreestylerRequest.class))).thenReturn(response(1L, "Alfa", "0", 1));

        mockMvc.perform(post("/api/freestylers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alias": "Alfa",
                                  "firstName": "Mario",
                                  "roles": ["HOST"],
                                  "socials": [{"name": "Instagram", "link": "https://instagram.com/alfa"}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alias").value("Alfa"));

        ArgumentCaptor<FreestylerRequest> captor = ArgumentCaptor.forClass(FreestylerRequest.class);
        verify(freestylerService).create(captor.capture());
        FreestylerRequest request = captor.getValue();
        assertThat(request.alias()).isEqualTo("Alfa");
        assertThat(request.firstName()).isEqualTo("Mario");
        assertThat(request.roles()).containsExactly(Role.HOST);
        assertThat(request.socials()).singleElement().satisfies(social -> {
            assertThat(social.name()).isEqualTo("Instagram");
            assertThat(social.id()).isNull();
        });
    }

    @Test
    void update_returns200AndPassesIdAndRequestToService() throws Exception {
        when(freestylerService.update(eq(1L), any(FreestylerRequest.class))).thenReturn(response(1L, "New", "10", 1));

        mockMvc.perform(put("/api/freestylers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "New"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("New"));

        ArgumentCaptor<FreestylerRequest> captor = ArgumentCaptor.forClass(FreestylerRequest.class);
        verify(freestylerService).update(eq(1L), captor.capture());
        assertThat(captor.getValue().alias()).isEqualTo("New");
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/freestylers/1"))
                .andExpect(status().isNoContent());

        verify(freestylerService).delete(1L);
    }

    @Test
    void delete_unknownIdReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Freestyler", 99L)).when(freestylerService).delete(99L);

        mockMvc.perform(delete("/api/freestylers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerResults_returns204WithoutBodyAndPassesMappedRequestToService() throws Exception {
        mockMvc.perform(post("/api/freestylers/tournament-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roundOf16Night": true,
                                  "threeWayFinalNight": false,
                                  "entries": [
                                    {"freestylerId": 1, "placement": "FIRST_PLACE"},
                                    {"freestylerId": 2, "placement": "THIRD_PLACE"}
                                  ]
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<TournamentResultsRequest> captor = ArgumentCaptor.forClass(TournamentResultsRequest.class);
        verify(freestylerService).registerResults(captor.capture());
        TournamentResultsRequest request = captor.getValue();
        assertThat(request.roundOf16Night()).isTrue();
        assertThat(request.threeWayFinalNight()).isFalse();
        assertThat(request.entries()).extracting(entry -> entry.freestylerId()).containsExactly(1L, 2L);
        assertThat(request.entries()).extracting(entry -> entry.placement())
                .containsExactly(Placement.FIRST_PLACE, Placement.THIRD_PLACE);
    }

    // I due flag sono boolean primitivi: se mancano nel JSON valgono false
    @Test
    void registerResults_missingNightFlagsDefaultToFalse() throws Exception {
        mockMvc.perform(post("/api/freestylers/tournament-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"entries": [{"freestylerId": 1, "placement": "SELECTION"}]}
                                """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<TournamentResultsRequest> captor = ArgumentCaptor.forClass(TournamentResultsRequest.class);
        verify(freestylerService).registerResults(captor.capture());
        assertThat(captor.getValue().roundOf16Night()).isFalse();
        assertThat(captor.getValue().threeWayFinalNight()).isFalse();
    }

    private static FreestylerResponse response(Long id, String alias, String score, Integer ranking) {
        return new FreestylerResponse(id, alias, null, null, null, Set.of(Role.FREESTYLER), List.of(),
                new BigDecimal(score), 0, 0, 0, 0, 0, 0, 0, 0, ranking);
    }

}