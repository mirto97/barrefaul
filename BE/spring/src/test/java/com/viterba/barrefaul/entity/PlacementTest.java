package com.viterba.barrefaul.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PlacementTest {

    // Colonne: piazzamento, serata con ottavi, serata con finale a tre, punti attesi.
    // Le combinazioni non ammesse (es. THIRD_PLACE senza finale a tre) sono coperte da isAllowed
    @ParameterizedTest
    @CsvSource({
        "FIRST_PLACE, false, false, 5",
        "FIRST_PLACE, true, false, 5",
        "FIRST_PLACE, false, true, 5",
        "FIRST_PLACE, true, true, 5",
        "SECOND_PLACE, false, false, 3",
        "SECOND_PLACE, true, false, 4",
        "SECOND_PLACE, false, true, 4",
        "SECOND_PLACE, true, true, 4",
        "THIRD_PLACE, false, true, 3",
        "THIRD_PLACE, true, true, 3",
        "SEMIFINAL, false, false, 2",
        "SEMIFINAL, true, false, 3",
        "SEMIFINAL, false, true, 2",
        "SEMIFINAL, true, true, 3",
        "QUARTERFINAL, false, false, 1",
        "QUARTERFINAL, true, false, 2",
        "QUARTERFINAL, false, true, 1",
        "QUARTERFINAL, true, true, 2",
        "ROUND_OF_16, true, false, 1",
        "ROUND_OF_16, true, true, 1",
        "SELECTION, false, false, 0.5",
        "SELECTION, true, false, 0.5",
        "SELECTION, false, true, 0.5",
        "SELECTION, true, true, 0.5",
        "JURY, false, false, 2.5",
        "JURY, true, false, 2.5",
        "JURY, false, true, 2.5",
        "JURY, true, true, 2.5"
    })
    void points_matchesLegendForNightFormat(
            Placement placement, boolean roundOf16Night, boolean threeWayFinalNight, BigDecimal expected) {
        // compareTo e non equals: BigDecimal.equals distingue 2.5 da 2.50
        assertThat(placement.points(roundOf16Night, threeWayFinalNight)).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "THIRD_PLACE, false, false, false",
        "THIRD_PLACE, true, false, false",
        "THIRD_PLACE, false, true, true",
        "THIRD_PLACE, true, true, true",
        "ROUND_OF_16, false, false, false",
        "ROUND_OF_16, false, true, false",
        "ROUND_OF_16, true, false, true",
        "ROUND_OF_16, true, true, true"
    })
    void isAllowed_restrictedPlacementsRequireMatchingFormat(
            Placement placement, boolean roundOf16Night, boolean threeWayFinalNight, boolean expected) {
        assertThat(placement.isAllowed(roundOf16Night, threeWayFinalNight)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = Placement.class, names = {"THIRD_PLACE", "ROUND_OF_16"}, mode = EnumSource.Mode.EXCLUDE)
    void isAllowed_unrestrictedPlacementsAreAlwaysAllowed(Placement placement) {
        for (boolean roundOf16Night : new boolean[] {false, true}) {
            for (boolean threeWayFinalNight : new boolean[] {false, true}) {
                assertThat(placement.isAllowed(roundOf16Night, threeWayFinalNight)).isTrue();
            }
        }
    }

    // L'indice è la posizione del contatore nell'array restituito da counters()
    @ParameterizedTest
    @CsvSource({
        "FIRST_PLACE, 0",
        "SECOND_PLACE, 1",
        "THIRD_PLACE, 1",
        "SEMIFINAL, 2",
        "QUARTERFINAL, 3",
        "ROUND_OF_16, 4",
        "SELECTION, 5",
        "JURY, 6"
    })
    void incrementCounter_incrementsOnlyTheMatchingCounter(Placement placement, int expectedIndex) {
        Freestyler freestyler = new Freestyler();

        placement.incrementCounter(freestyler);

        assertThat(counters(freestyler)).containsExactly(oneAt(expectedIndex));
    }

    @Test
    void incrementCounter_accumulatesAcrossCalls() {
        Freestyler freestyler = new Freestyler();

        Placement.SELECTION.incrementCounter(freestyler);
        Placement.SELECTION.incrementCounter(freestyler);

        assertThat(freestyler.getSelections()).isEqualTo(2);
    }

    @Test
    void incrementCounter_doesNotTouchScoreOrParticipations() {
        Freestyler freestyler = new Freestyler();

        Placement.FIRST_PLACE.incrementCounter(freestyler);

        assertThat(freestyler.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(freestyler.getParticipations()).isZero();
    }

    private static int[] counters(Freestyler freestyler) {
        return new int[] {
            freestyler.getFirstPlaces(),
            freestyler.getSecondPlaces(),
            freestyler.getSemifinals(),
            freestyler.getQuarterfinals(),
            freestyler.getRoundOf16(),
            freestyler.getSelections(),
            freestyler.getJury()
        };
    }

    private static int[] oneAt(int index) {
        int[] expected = new int[7];
        expected[index] = 1;
        return expected;
    }

}