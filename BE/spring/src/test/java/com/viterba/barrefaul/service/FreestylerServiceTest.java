package com.viterba.barrefaul.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.dto.PlacementEntry;
import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.entity.Freestyler;
import com.viterba.barrefaul.entity.Placement;
import com.viterba.barrefaul.entity.Role;
import com.viterba.barrefaul.entity.Social;
import com.viterba.barrefaul.exception.InvalidRequestException;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.FreestylerMapperImpl;
import com.viterba.barrefaul.mapper.SocialMapperImpl;
import com.viterba.barrefaul.repository.FreestylerRepository;

// Contesto minimo: service, mapper generati e SocialMerger reali, solo il repository è un mock
@SpringJUnitConfig(classes = {
    FreestylerService.class,
    FreestylerMapperImpl.class,
    SocialMapperImpl.class,
    SocialMerger.class
})
class FreestylerServiceTest {

    @MockitoBean
    private FreestylerRepository freestylerRepository;

    @Autowired
    private FreestylerService freestylerService;

    // findAll

    @Test
    void findAll_assignsSamePositionToTiesWithoutSkipping() {
        // 10 e 10.0 e poi 7.5 e 7.50: stesso valore con scale diverse, devono pareggiare
        when(freestylerRepository.findAllByOrderByScoreDescAliasAsc()).thenReturn(List.of(
                freestyler(1L, "Alfa", "10"),
                freestyler(2L, "Bravo", "10.0"),
                freestyler(3L, "Charlie", "7.5"),
                freestyler(4L, "Delta", "7.50"),
                freestyler(5L, "Echo", "0")));

        List<FreestylerResponse> result = freestylerService.findAll();

        assertThat(result).extracting(response -> response.alias())
                .containsExactly("Alfa", "Bravo", "Charlie", "Delta", "Echo");
        assertThat(result).extracting(response -> response.ranking())
                .containsExactly(1, 1, 2, 2, 3);
    }

    @Test
    void findAll_singleFreestylerWithZeroScoreIsFirst() {
        when(freestylerRepository.findAllByOrderByScoreDescAliasAsc())
                .thenReturn(List.of(freestyler(1L, "Alfa", "0")));

        assertThat(freestylerService.findAll()).extracting(response -> response.ranking())
                .containsExactly(1);
    }

    @Test
    void findAll_emptyRepositoryReturnsEmptyList() {
        when(freestylerRepository.findAllByOrderByScoreDescAliasAsc()).thenReturn(List.of());

        assertThat(freestylerService.findAll()).isEmpty();
    }

    @Test
    void findAll_mapsAllFields() {
        Freestyler freestyler = freestyler(1L, "Alfa", "12.5");
        freestyler.setFirstName("Mario");
        freestyler.setLastName("Rossi");
        freestyler.setBio("Bio");
        freestyler.setParticipations(8);
        freestyler.setFirstPlaces(1);
        freestyler.setSecondPlaces(2);
        freestyler.setSemifinals(3);
        freestyler.setQuarterfinals(4);
        freestyler.setRoundOf16(5);
        freestyler.setSelections(6);
        freestyler.setJury(7);
        freestyler.getSocials().add(social(10L, "Instagram", "https://instagram.com/alfa", "alfa.png"));
        when(freestylerRepository.findAllByOrderByScoreDescAliasAsc()).thenReturn(List.of(freestyler));

        FreestylerResponse response = freestylerService.findAll().get(0);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.alias()).isEqualTo("Alfa");
        assertThat(response.firstName()).isEqualTo("Mario");
        assertThat(response.lastName()).isEqualTo("Rossi");
        assertThat(response.bio()).isEqualTo("Bio");
        assertThat(response.roles()).isEqualTo(Set.of(Role.FREESTYLER));
        assertThat(response.score()).isEqualByComparingTo("12.5");
        assertThat(response.participations()).isEqualTo(8);
        assertThat(response.firstPlaces()).isEqualTo(1);
        assertThat(response.secondPlaces()).isEqualTo(2);
        assertThat(response.semifinals()).isEqualTo(3);
        assertThat(response.quarterfinals()).isEqualTo(4);
        assertThat(response.roundOf16()).isEqualTo(5);
        assertThat(response.selections()).isEqualTo(6);
        assertThat(response.jury()).isEqualTo(7);
        assertThat(response.ranking()).isEqualTo(1);
        assertThat(response.socials()).singleElement().satisfies(social -> {
            assertThat(social.id()).isEqualTo(10L);
            assertThat(social.name()).isEqualTo("Instagram");
            assertThat(social.link()).isEqualTo("https://instagram.com/alfa");
            assertThat(social.image()).isEqualTo("alfa.png");
        });
    }

    // findById

    @Test
    void findById_rankingCountsDistinctHigherScoresPlusOne() {
        Freestyler freestyler = freestyler(1L, "Alfa", "12.5");
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(freestyler));
        when(freestylerRepository.countDistinctScoresGreaterThan(freestyler.getScore())).thenReturn(2L);

        assertThat(freestylerService.findById(1L).ranking()).isEqualTo(3);
    }

    @Test
    void findById_rankingIsOneWhenNoHigherScoreExists() {
        Freestyler freestyler = freestyler(1L, "Alfa", "12.5");
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(freestyler));
        when(freestylerRepository.countDistinctScoresGreaterThan(freestyler.getScore())).thenReturn(0L);

        assertThat(freestylerService.findById(1L).ranking()).isEqualTo(1);
    }

    @Test
    void findById_unknownIdThrowsNotFound() {
        when(freestylerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> freestylerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // create

    @Test
    void create_addsFreestylerRoleToProvidedRoles() {
        stubSaveReturnsArgument();
        FreestylerRequest request = new FreestylerRequest("Alfa", null, null, null, Set.of(Role.HOST), null);

        FreestylerResponse response = freestylerService.create(request);

        assertThat(response.roles()).containsExactlyInAnyOrder(Role.HOST, Role.FREESTYLER);
    }

    // Punto aperto del dump: con roles null il mapper non deve lasciare il set a null
    @Test
    void create_withNullRolesHasOnlyFreestylerRole() {
        stubSaveReturnsArgument();
        FreestylerRequest request = new FreestylerRequest("Alfa", null, null, null, null, null);

        FreestylerResponse response = freestylerService.create(request);

        assertThat(response.roles()).containsExactly(Role.FREESTYLER);
    }

    // Con socials null il mapper non deve lasciare la lista dell'entity a null
    @Test
    void create_withNullSocialsHasEmptySocialsOnEntity() {
        stubSaveReturnsArgument();
        FreestylerRequest request = new FreestylerRequest("Alfa", null, null, null, null, null);

        freestylerService.create(request);

        ArgumentCaptor<Freestyler> captor = ArgumentCaptor.forClass(Freestyler.class);
        verify(freestylerRepository).save(captor.capture());
        assertThat(captor.getValue().getSocials()).isEmpty();
    }

    @Test
    void create_mapsScalarFieldsAndSocialsInOrder() {
        stubSaveReturnsArgument();
        FreestylerRequest request = new FreestylerRequest("Alfa", "Mario", "Rossi", "Bio", null, List.of(
                new SocialRequest(null, "Instagram", "https://instagram.com/alfa", "alfa.png"),
                new SocialRequest(null, "Spotify", "https://open.spotify.com/alfa", null)));

        FreestylerResponse response = freestylerService.create(request);

        ArgumentCaptor<Freestyler> captor = ArgumentCaptor.forClass(Freestyler.class);
        verify(freestylerRepository).save(captor.capture());
        Freestyler saved = captor.getValue();
        assertThat(saved.getAlias()).isEqualTo("Alfa");
        assertThat(saved.getFirstName()).isEqualTo("Mario");
        assertThat(saved.getLastName()).isEqualTo("Rossi");
        assertThat(saved.getBio()).isEqualTo("Bio");
        // Punteggio e partecipazioni non arrivano dalla request
        assertThat(saved.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getParticipations()).isZero();
        assertThat(saved.getSocials()).extracting(social -> social.getName())
                .containsExactly("Instagram", "Spotify");
        assertThat(response.socials()).extracting(social -> social.name())
                .containsExactly("Instagram", "Spotify");
    }

    // update

    @Test
    void update_replacesScalarFieldsAndNullClearsThem() {
        Freestyler existing = existingFreestyler();
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("New", null, "Rossi", null, null, null);

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.alias()).isEqualTo("New");
        assertThat(response.firstName()).isNull();
        assertThat(response.lastName()).isEqualTo("Rossi");
        assertThat(response.bio()).isNull();
    }

    @Test
    void update_doesNotTouchScoreParticipationsOrCounters() {
        Freestyler existing = existingFreestyler();
        existing.setParticipations(4);
        existing.setFirstPlaces(2);
        existing.setJury(1);
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("Old", null, null, null, null, null);

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.score()).isEqualByComparingTo("10");
        assertThat(response.participations()).isEqualTo(4);
        assertThat(response.firstPlaces()).isEqualTo(2);
        assertThat(response.jury()).isEqualTo(1);
    }

    @Test
    void update_nullRolesLeaveRolesUnchanged() {
        Freestyler existing = existingFreestyler();
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("Old", null, null, null, null, null);

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.roles()).containsExactlyInAnyOrder(Role.HOST, Role.FREESTYLER);
    }

    @Test
    void update_replacesRolesButKeepsFreestylerRole() {
        Freestyler existing = existingFreestyler();
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("Old", null, null, null, Set.of(Role.CEO), null);

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.roles()).containsExactlyInAnyOrder(Role.CEO, Role.FREESTYLER);
    }

    @Test
    void update_nullSocialsLeaveSocialsUnchanged() {
        Freestyler existing = existingFreestyler();
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("Old", null, null, null, null, null);

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.socials()).singleElement()
                .satisfies(social -> assertThat(social.id()).isEqualTo(5L));
    }

    @Test
    void update_emptySocialsRemovesAll() {
        Freestyler existing = existingFreestyler();
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(existing));
        FreestylerRequest request = new FreestylerRequest("Old", null, null, null, null, List.of());

        FreestylerResponse response = freestylerService.update(1L, request);

        assertThat(response.socials()).isEmpty();
    }

    @Test
    void update_unknownIdThrowsNotFound() {
        when(freestylerRepository.findById(99L)).thenReturn(Optional.empty());
        FreestylerRequest request = new FreestylerRequest("Alfa", null, null, null, null, null);

        assertThatThrownBy(() -> freestylerService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // delete

    @Test
    void delete_removesExistingFreestyler() {
        Freestyler freestyler = freestyler(1L, "Alfa", "0");
        when(freestylerRepository.findById(1L)).thenReturn(Optional.of(freestyler));

        freestylerService.delete(1L);

        verify(freestylerRepository).delete(freestyler);
    }

    @Test
    void delete_unknownIdThrowsNotFoundAndDeletesNothing() {
        when(freestylerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> freestylerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(freestylerRepository, never()).delete(any(Freestyler.class));
    }

    // registerResults

    @Test
    void registerResults_standardNightUpdatesScoreCountersAndParticipations() {
        Freestyler first = freestyler(1L, "Alfa", "10");
        Freestyler second = freestyler(2L, "Bravo", "2.5");
        stubFindById(first, second);

        freestylerService.registerResults(results(false, false,
                new PlacementEntry(1L, Placement.FIRST_PLACE),
                new PlacementEntry(2L, Placement.SECOND_PLACE)));

        assertThat(first.getScore()).isEqualByComparingTo("15");
        assertThat(first.getFirstPlaces()).isEqualTo(1);
        assertThat(first.getParticipations()).isEqualTo(1);
        assertThat(second.getScore()).isEqualByComparingTo("5.5");
        assertThat(second.getSecondPlaces()).isEqualTo(1);
        assertThat(second.getParticipations()).isEqualTo(1);
    }

    @Test
    void registerResults_roundOf16NightUsesRaisedPoints() {
        Freestyler semifinalist = freestyler(1L, "Alfa", "0");
        Freestyler roundOf16Player = freestyler(2L, "Bravo", "0");
        stubFindById(semifinalist, roundOf16Player);

        freestylerService.registerResults(results(true, false,
                new PlacementEntry(1L, Placement.SEMIFINAL),
                new PlacementEntry(2L, Placement.ROUND_OF_16)));

        assertThat(semifinalist.getScore()).isEqualByComparingTo("3");
        assertThat(semifinalist.getSemifinals()).isEqualTo(1);
        assertThat(roundOf16Player.getScore()).isEqualByComparingTo("1");
        assertThat(roundOf16Player.getRoundOf16()).isEqualTo(1);
    }

    @Test
    void registerResults_roundOf16AndThreeWayFinalCanCoincide() {
        Freestyler runnerUp = freestyler(1L, "Alfa", "0");
        Freestyler thirdPlace = freestyler(2L, "Bravo", "0");
        stubFindById(runnerUp, thirdPlace);

        freestylerService.registerResults(results(true, true,
                new PlacementEntry(1L, Placement.SECOND_PLACE),
                new PlacementEntry(2L, Placement.THIRD_PLACE)));

        assertThat(runnerUp.getScore()).isEqualByComparingTo("4");
        assertThat(thirdPlace.getScore()).isEqualByComparingTo("3");
        // Il terzo posto viene contato come secondo posto
        assertThat(thirdPlace.getSecondPlaces()).isEqualTo(1);
    }

    @Test
    void registerResults_juryCountsAsParticipation() {
        Freestyler juror = freestyler(1L, "Alfa", "0");
        stubFindById(juror);

        freestylerService.registerResults(results(false, false, new PlacementEntry(1L, Placement.JURY)));

        assertThat(juror.getScore()).isEqualByComparingTo("2.5");
        assertThat(juror.getJury()).isEqualTo(1);
        assertThat(juror.getParticipations()).isEqualTo(1);
    }

    @Test
    void registerResults_accumulatesAcrossNights() {
        Freestyler freestyler = freestyler(1L, "Alfa", "0");
        stubFindById(freestyler);
        TournamentResultsRequest night = results(false, false, new PlacementEntry(1L, Placement.FIRST_PLACE));

        freestylerService.registerResults(night);
        freestylerService.registerResults(night);

        assertThat(freestyler.getScore()).isEqualByComparingTo("10");
        assertThat(freestyler.getFirstPlaces()).isEqualTo(2);
        assertThat(freestyler.getParticipations()).isEqualTo(2);
    }

    @Test
    void registerResults_duplicateFreestylerThrowsBeforeTouchingTheRepository() {
        TournamentResultsRequest request = results(false, false,
                new PlacementEntry(1L, Placement.FIRST_PLACE),
                new PlacementEntry(1L, Placement.SELECTION));

        assertThatThrownBy(() -> freestylerService.registerResults(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("more than once");
        verifyNoInteractions(freestylerRepository);
    }

    @Test
    void registerResults_thirdPlaceWithoutThreeWayFinalIsRejected() {
        TournamentResultsRequest request = results(true, false, new PlacementEntry(1L, Placement.THIRD_PLACE));

        assertThatThrownBy(() -> freestylerService.registerResults(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("not allowed");
        verifyNoInteractions(freestylerRepository);
    }

    @Test
    void registerResults_roundOf16WithoutRoundOf16NightIsRejected() {
        TournamentResultsRequest request = results(false, true, new PlacementEntry(1L, Placement.ROUND_OF_16));

        assertThatThrownBy(() -> freestylerService.registerResults(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("not allowed");
        verifyNoInteractions(freestylerRepository);
    }

    // Il rollback dei freestyler già modificati non si prova qui (nessuna transazione): vedi test sul database
    @Test
    void registerResults_unknownFreestylerThrowsNotFound() {
        when(freestylerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> freestylerService.registerResults(
                results(false, false, new PlacementEntry(99L, Placement.FIRST_PLACE))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Helper

    private void stubSaveReturnsArgument() {
        when(freestylerRepository.save(any(Freestyler.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubFindById(Freestyler... freestylers) {
        for (Freestyler freestyler : freestylers) {
            when(freestylerRepository.findById(freestyler.getId())).thenReturn(Optional.of(freestyler));
        }
    }

    private static TournamentResultsRequest results(
            boolean roundOf16Night, boolean threeWayFinalNight, PlacementEntry... entries) {
        return new TournamentResultsRequest(roundOf16Night, threeWayFinalNight, List.of(entries));
    }

    private static Freestyler freestyler(Long id, String alias, String score) {
        Freestyler freestyler = new Freestyler();
        freestyler.setId(id);
        freestyler.setAlias(alias);
        freestyler.setScore(new BigDecimal(score));
        return freestyler;
    }

    private static Social social(Long id, String name, String link, String image) {
        Social social = new Social();
        social.setId(id);
        social.setName(name);
        social.setLink(link);
        social.setImage(image);
        return social;
    }

    // Freestyler con ruolo HOST in più, punteggio 10 e un social con id 5
    private static Freestyler existingFreestyler() {
        Freestyler freestyler = freestyler(1L, "Old", "10");
        freestyler.setFirstName("Mario");
        freestyler.setBio("Old bio");
        freestyler.getRoles().add(Role.HOST);
        freestyler.getSocials().add(social(5L, "Instagram", "https://instagram.com/old", null));
        return freestyler;
    }

}