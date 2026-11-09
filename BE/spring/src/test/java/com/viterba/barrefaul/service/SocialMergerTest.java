package com.viterba.barrefaul.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.entity.Crew;
import com.viterba.barrefaul.entity.Social;
import com.viterba.barrefaul.exception.ResourceNotFoundException;
import com.viterba.barrefaul.mapper.SocialMapperImpl;

@SpringJUnitConfig(classes = {SocialMerger.class, SocialMapperImpl.class})
class SocialMergerTest {

    @Autowired
    private SocialMerger socialMerger;

    @Test
    void merge_createsSocialsWithoutIdInRequestOrder() {
        Crew crew = new Crew();

        socialMerger.merge(crew, List.of(
                new SocialRequest(null, "Instagram", "https://instagram.com/alfa", "alfa.png"),
                new SocialRequest(null, "Spotify", "https://open.spotify.com/alfa", null)));

        assertThat(crew.getSocials()).extracting(social -> social.getName())
                .containsExactly("Instagram", "Spotify");
        assertThat(crew.getSocials()).extracting(social -> social.getId()).containsOnlyNulls();
        assertThat(crew.getSocials().get(0).getLink()).isEqualTo("https://instagram.com/alfa");
        assertThat(crew.getSocials().get(0).getImage()).isEqualTo("alfa.png");
    }

    @Test
    void merge_updatesExistingSocialInPlace() {
        Social existing = social(1L, "Instagram", "https://instagram.com/old", "old.png");
        Crew crew = crewWith(existing);

        socialMerger.merge(crew, List.of(new SocialRequest(1L, "IG", "https://instagram.com/new", "new.png")));

        // Resta la stessa istanza, con l'id invariato
        assertThat(crew.getSocials()).singleElement().isSameAs(existing);
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getName()).isEqualTo("IG");
        assertThat(existing.getLink()).isEqualTo("https://instagram.com/new");
        assertThat(existing.getImage()).isEqualTo("new.png");
    }

    @Test
    void merge_updateWithNullImageClearsImage() {
        Social existing = social(1L, "Instagram", "https://instagram.com/old", "old.png");
        Crew crew = crewWith(existing);

        socialMerger.merge(crew, List.of(new SocialRequest(1L, "Instagram", "https://instagram.com/old", null)));

        assertThat(existing.getImage()).isNull();
    }

    @Test
    void merge_removesSocialsMissingFromRequest() {
        Social first = social(1L, "Instagram", "https://instagram.com/alfa", null);
        Social second = social(2L, "Spotify", "https://open.spotify.com/alfa", null);
        Social third = social(3L, "YouTube", "https://youtube.com/alfa", null);
        Crew crew = crewWith(first, second, third);

        socialMerger.merge(crew, List.of(new SocialRequest(2L, "Spotify", "https://open.spotify.com/alfa", null)));

        assertThat(crew.getSocials()).containsExactly(second);
    }

    @Test
    void merge_reordersAccordingToRequest() {
        Social first = social(1L, "Instagram", "https://instagram.com/alfa", null);
        Social second = social(2L, "Spotify", "https://open.spotify.com/alfa", null);
        Social third = social(3L, "YouTube", "https://youtube.com/alfa", null);
        Crew crew = crewWith(first, second, third);

        socialMerger.merge(crew, List.of(
                new SocialRequest(3L, "YouTube", "https://youtube.com/alfa", null),
                new SocialRequest(1L, "Instagram", "https://instagram.com/alfa", null),
                new SocialRequest(2L, "Spotify", "https://open.spotify.com/alfa", null)));

        assertThat(crew.getSocials()).containsExactly(third, first, second);
    }

    @Test
    void merge_emptyRequestRemovesAll() {
        Crew crew = crewWith(social(1L, "Instagram", "https://instagram.com/alfa", null));

        socialMerger.merge(crew, List.of());

        assertThat(crew.getSocials()).isEmpty();
    }

    @Test
    void merge_combinesUpdateCreateAndDelete() {
        Social first = social(1L, "Instagram", "https://instagram.com/alfa", null);
        Social second = social(2L, "Spotify", "https://open.spotify.com/alfa", null);
        Crew crew = crewWith(first, second);

        socialMerger.merge(crew, List.of(
                new SocialRequest(2L, "Spotify updated", "https://open.spotify.com/new", null),
                new SocialRequest(null, "YouTube", "https://youtube.com/alfa", null)));

        assertThat(crew.getSocials()).hasSize(2);
        assertThat(crew.getSocials().get(0)).isSameAs(second);
        assertThat(second.getName()).isEqualTo("Spotify updated");
        assertThat(crew.getSocials().get(1).getId()).isNull();
        assertThat(crew.getSocials().get(1).getName()).isEqualTo("YouTube");
        assertThat(crew.getSocials()).doesNotContain(first);
    }

    @Test
    void merge_unknownIdThrowsNotFound() {
        Crew crew = crewWith(social(1L, "Instagram", "https://instagram.com/alfa", null));
        List<SocialRequest> requests = List.of(new SocialRequest(99L, "X", "https://x.com/alfa", null));

        assertThatThrownBy(() -> socialMerger.merge(crew, requests))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Social")
                .hasMessageContaining("99");
    }

    @Test
    void merge_unknownIdLeavesTheListUntouched() {
        Social existing = social(1L, "Instagram", "https://instagram.com/alfa", null);
        Crew crew = crewWith(existing);
        List<SocialRequest> requests = List.of(new SocialRequest(99L, "X", "https://x.com/alfa", null));

        assertThatThrownBy(() -> socialMerger.merge(crew, requests))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(crew.getSocials()).containsExactly(existing);
    }

    private static Social social(Long id, String name, String link, String image) {
        Social social = new Social();
        social.setId(id);
        social.setName(name);
        social.setLink(link);
        social.setImage(image);
        return social;
    }

    private static Crew crewWith(Social... socials) {
        Crew crew = new Crew();
        crew.setId(1L);
        crew.getSocials().addAll(List.of(socials));
        return crew;
    }

}