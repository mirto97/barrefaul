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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.SocialResponse;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.service.CrewService;

@WebMvcTest(CrewController.class)
class CrewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrewService crewService;

    @Test
    void findAll_returnsListInServiceOrder() throws Exception {
        when(crewService.findAll()).thenReturn(List.of(response(1L, "Alfa"), response(2L, "Bravo")));

        mockMvc.perform(get("/api/crew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].alias").value("Alfa"))
                .andExpect(jsonPath("$[1].alias").value("Bravo"));
    }

    @Test
    void findById_returnsAllFieldsIncludingSocials() throws Exception {
        when(crewService.findById(1L)).thenReturn(new CrewResponse(
                1L, "Alfa", "Mario", "Rossi", "Bio", Set.of(Role.HOST),
                List.of(new SocialResponse(5L, "Instagram", "https://instagram.com/alfa", "alfa.png"))));

        mockMvc.perform(get("/api/crew/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alias").value("Alfa"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.bio").value("Bio"))
                .andExpect(jsonPath("$.roles[0]").value("HOST"))
                .andExpect(jsonPath("$.socials[0].id").value(5))
                .andExpect(jsonPath("$.socials[0].name").value("Instagram"))
                .andExpect(jsonPath("$.socials[0].link").value("https://instagram.com/alfa"))
                .andExpect(jsonPath("$.socials[0].image").value("alfa.png"));
    }

    @Test
    void create_returns201AndPassesMappedRequestToService() throws Exception {
        when(crewService.create(any(CrewRequest.class))).thenReturn(response(1L, "Alfa"));

        mockMvc.perform(post("/api/crew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alias": "Alfa",
                                  "firstName": "Mario",
                                  "roles": ["HOST", "CEO"],
                                  "socials": [
                                    {"name": "Instagram", "link": "https://instagram.com/alfa", "image": "alfa.png"},
                                    {"id": 5, "name": "Spotify", "link": "https://open.spotify.com/alfa"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alias").value("Alfa"));

        ArgumentCaptor<CrewRequest> captor = ArgumentCaptor.forClass(CrewRequest.class);
        verify(crewService).create(captor.capture());
        CrewRequest request = captor.getValue();
        assertThat(request.alias()).isEqualTo("Alfa");
        assertThat(request.firstName()).isEqualTo("Mario");
        assertThat(request.roles()).containsExactlyInAnyOrder(Role.HOST, Role.CEO);
        // L'ordine della lista si conserva: è l'ordine di visualizzazione
        assertThat(request.socials()).extracting(social -> social.name()).containsExactly("Instagram", "Spotify");
        assertThat(request.socials()).extracting(social -> social.id()).containsExactly(null, 5L);
    }

    @Test
    void create_invalidNestedSocialsReturns400WithFieldPaths() throws Exception {
        mockMvc.perform(post("/api/crew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "Alfa", "socials": [{}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors['socials[0].name']").isNotEmpty())
                .andExpect(jsonPath("$.errors['socials[0].link']").isNotEmpty());

        verifyNoInteractions(crewService);
    }

    @Test
    void create_unknownRoleReturns400() throws Exception {
        mockMvc.perform(post("/api/crew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "Alfa", "roles": ["WIZARD"]}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(crewService);
    }

    @Test
    void update_returns200AndPassesIdAndRequestToService() throws Exception {
        when(crewService.update(eq(1L), any(CrewRequest.class))).thenReturn(response(1L, "New"));

        mockMvc.perform(put("/api/crew/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "New"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("New"));

        ArgumentCaptor<CrewRequest> captor = ArgumentCaptor.forClass(CrewRequest.class);
        verify(crewService).update(eq(1L), captor.capture());
        assertThat(captor.getValue().alias()).isEqualTo("New");
    }

    @Test
    void update_unknownIdReturns404() throws Exception {
        when(crewService.update(eq(99L), any(CrewRequest.class)))
                .thenThrow(new ResourceNotFoundException("Crew", 99L));

        mockMvc.perform(put("/api/crew/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "New"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Crew not found with id: 99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/crew/1"))
                .andExpect(status().isNoContent());

        verify(crewService).delete(1L);
    }

    private static CrewResponse response(Long id, String alias) {
        return new CrewResponse(id, alias, null, null, null, Set.of(Role.HOST), List.of());
    }

}