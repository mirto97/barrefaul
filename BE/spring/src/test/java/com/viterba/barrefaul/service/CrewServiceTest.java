package com.viterba.barrefaul.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.entity.Freestyler;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.entity.Social;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.CrewMapperImpl;
import com.viterba.barrefaul.mapper.SocialMapperImpl;
import com.viterba.barrefaul.repository.CrewRepository;

// Contesto minimo: service, mapper generati e SocialMerger reali, solo il repository è un mock
@SpringJUnitConfig(classes = {
    CrewService.class,
    CrewMapperImpl.class,
    SocialMapperImpl.class,
    SocialMerger.class
})
class CrewServiceTest {

    @MockitoBean
    private CrewRepository crewRepository;

    @Autowired
    private CrewService crewService;

    // findAll

    // La query è polimorfica: i Freestyler arrivano mappati come Crew, senza i loro campi specifici
    @Test
    void findAll_includesFreestylersMappedAsCrew() {
        Crew crew = crew(1L, "Alfa");
        Freestyler freestyler = new Freestyler();
        freestyler.setId(2L);
        freestyler.setAlias("Bravo");
        when(crewRepository.findAll()).thenReturn(List.of(crew, freestyler));

        List<CrewResponse> result = crewService.findAll();

        assertThat(result).extracting(response -> response.alias()).containsExactly("Alfa", "Bravo");
        assertThat(result.get(1).roles()).containsExactly(Role.FREESTYLER);
    }

    @Test
    void findAll_emptyRepositoryReturnsEmptyList() {
        when(crewRepository.findAll()).thenReturn(List.of());

        assertThat(crewService.findAll()).isEmpty();
    }

    // findById

    @Test
    void findById_mapsAllFields() {
        Crew crew = existingCrew();
        when(crewRepository.findById(1L)).thenReturn(Optional.of(crew));

        CrewResponse response = crewService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.alias()).isEqualTo("Old");
        assertThat(response.firstName()).isEqualTo("Mario");
        assertThat(response.lastName()).isNull();
        assertThat(response.bio()).isEqualTo("Old bio");
        assertThat(response.roles()).containsExactly(Role.HOST);
        assertThat(response.socials()).singleElement().satisfies(social -> {
            assertThat(social.id()).isEqualTo(5L);
            assertThat(social.name()).isEqualTo("Instagram");
            assertThat(social.link()).isEqualTo("https://instagram.com/old");
            assertThat(social.image()).isNull();
        });
    }

    @Test
    void findById_unknownIdThrowsNotFound() {
        when(crewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crewService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Crew")
                .hasMessageContaining("99");
    }

    // create

    @Test
    void create_mapsFieldsRolesAndSocialsInOrder() {
        stubSaveReturnsArgument();
        CrewRequest request = new CrewRequest("Alfa", "Mario", "Rossi", "Bio", Set.of(Role.HOST), List.of(
                new SocialRequest(null, "Instagram", "https://instagram.com/alfa", "alfa.png"),
                new SocialRequest(null, "Spotify", "https://open.spotify.com/alfa", null)));

        CrewResponse response = crewService.create(request);

        ArgumentCaptor<Crew> captor = ArgumentCaptor.forClass(Crew.class);
        verify(crewRepository).save(captor.capture());
        Crew saved = captor.getValue();
        assertThat(saved.getAlias()).isEqualTo("Alfa");
        assertThat(saved.getFirstName()).isEqualTo("Mario");
        assertThat(saved.getLastName()).isEqualTo("Rossi");
        assertThat(saved.getBio()).isEqualTo("Bio");
        assertThat(saved.getSocials()).extracting(social -> social.getName())
                .containsExactly("Instagram", "Spotify");
        assertThat(response.alias()).isEqualTo("Alfa");
        assertThat(response.roles()).containsExactly(Role.HOST);
        assertThat(response.socials()).extracting(social -> social.name())
                .containsExactly("Instagram", "Spotify");
    }

    // A differenza di Freestyler, Crew non aggiunge nessun ruolo da solo
    @Test
    void create_doesNotAddAnyRoleOnItsOwn() {
        stubSaveReturnsArgument();
        CrewRequest request = new CrewRequest("Alfa", null, null, null, Set.of(Role.CEO), null);

        CrewResponse response = crewService.create(request);

        assertThat(response.roles()).containsExactly(Role.CEO);
    }

    // Punto aperto dei dump: con roles e socials null il mapper non deve lasciare le collezioni dell'entity a null
    @Test
    void create_withNullRolesAndSocialsHasEmptyCollections() {
        stubSaveReturnsArgument();
        CrewRequest request = new CrewRequest("Alfa", null, null, null, null, null);

        crewService.create(request);

        // Si controlla l'entity salvata e non la response, perché il mapper di risposta maschererebbe un null
        ArgumentCaptor<Crew> captor = ArgumentCaptor.forClass(Crew.class);
        verify(crewRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).isEmpty();
        assertThat(captor.getValue().getSocials()).isEmpty();
    }

    // update

    @Test
    void update_replacesScalarFieldsAndNullClearsThem() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("New", null, "Rossi", null, null, null);

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.alias()).isEqualTo("New");
        assertThat(response.firstName()).isNull();
        assertThat(response.lastName()).isEqualTo("Rossi");
        assertThat(response.bio()).isNull();
    }

    @Test
    void update_nullRolesLeaveRolesUnchanged() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, null, null);

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.roles()).containsExactly(Role.HOST);
    }

    @Test
    void update_replacesRoles() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, Set.of(Role.CEO), null);

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.roles()).containsExactly(Role.CEO);
    }

    @Test
    void update_nullSocialsLeaveSocialsUnchanged() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, null, null);

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.socials()).singleElement()
                .satisfies(social -> assertThat(social.id()).isEqualTo(5L));
    }

    @Test
    void update_emptySocialsRemovesAll() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, null, List.of());

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.socials()).isEmpty();
    }

    // Il flush è un mock: l'id del nuovo social resta null, qui conta solo il merge
    @Test
    void update_mergesSocialsById() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, null, List.of(
                new SocialRequest(5L, "IG", "https://instagram.com/new", null),
                new SocialRequest(null, "Spotify", "https://open.spotify.com/alfa", null)));

        CrewResponse response = crewService.update(1L, request);

        assertThat(response.socials()).extracting(social -> social.name()).containsExactly("IG", "Spotify");
        assertThat(response.socials().get(0).id()).isEqualTo(5L);
    }

    @Test
    void update_flushesRepository() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));

        crewService.update(1L, new CrewRequest("Old", null, null, null, null, null));

        verify(crewRepository).flush();
    }

    @Test
    void update_unknownIdThrowsNotFound() {
        when(crewRepository.findById(99L)).thenReturn(Optional.empty());
        CrewRequest request = new CrewRequest("Alfa", null, null, null, null, null);

        assertThatThrownBy(() -> crewService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Crew")
                .hasMessageContaining("99");
    }

    @Test
    void update_unknownSocialIdThrowsNotFound() {
        when(crewRepository.findById(1L)).thenReturn(Optional.of(existingCrew()));
        CrewRequest request = new CrewRequest("Old", null, null, null, null,
                List.of(new SocialRequest(99L, "X", "https://x.com/alfa", null)));

        assertThatThrownBy(() -> crewService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Social")
                .hasMessageContaining("99");
    }

    // delete

    @Test
    void delete_removesExistingCrew() {
        Crew crew = crew(1L, "Alfa");
        when(crewRepository.findById(1L)).thenReturn(Optional.of(crew));

        crewService.delete(1L);

        verify(crewRepository).delete(crew);
    }

    @Test
    void delete_unknownIdThrowsNotFoundAndDeletesNothing() {
        when(crewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crewService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(crewRepository, never()).delete(any(Crew.class));
    }

    // Helper

    private void stubSaveReturnsArgument() {
        when(crewRepository.save(any(Crew.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Crew crew(Long id, String alias) {
        Crew crew = new Crew();
        crew.setId(id);
        crew.setAlias(alias);
        return crew;
    }

    // Crew con ruolo HOST e un social con id 5
    private static Crew existingCrew() {
        Crew crew = crew(1L, "Old");
        crew.setFirstName("Mario");
        crew.setBio("Old bio");
        crew.getRoles().add(Role.HOST);
        Social social = new Social();
        social.setId(5L);
        social.setName("Instagram");
        social.setLink("https://instagram.com/old");
        crew.getSocials().add(social);
        return crew;
    }

}