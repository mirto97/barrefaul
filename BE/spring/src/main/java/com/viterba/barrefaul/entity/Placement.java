package com.viterba.barrefaul.entity;

import java.math.BigDecimal;
import java.util.function.Consumer;

// Piazzamento ottenuto in una serata, abbinato al contatore di Freestyler da incrementare e ai punti assegnati
public enum Placement {

    // TODO da commentare 

    FIRST_PLACE(freestyler -> freestyler.setFirstPlaces(freestyler.getFirstPlaces() + 1)),
    SECOND_PLACE(freestyler -> freestyler.setSecondPlaces(freestyler.getSecondPlaces() + 1)),
    // Terzo posto della finale a tre: viene contato come secondo posto
    THIRD_PLACE(freestyler -> freestyler.setSecondPlaces(freestyler.getSecondPlaces() + 1)),
    SEMIFINAL(freestyler -> freestyler.setSemifinals(freestyler.getSemifinals() + 1)),
    QUARTERFINAL(freestyler -> freestyler.setQuarterfinals(freestyler.getQuarterfinals() + 1)),
    ROUND_OF_16(freestyler -> freestyler.setRoundOf16(freestyler.getRoundOf16() + 1)),
    SELECTION(freestyler -> freestyler.setSelections(freestyler.getSelections() + 1)),
    JURY(freestyler -> freestyler.setJury(freestyler.getJury() + 1));

    private final Consumer<Freestyler> counterIncrement;

    Placement(Consumer<Freestyler> counterIncrement) {
        this.counterIncrement = counterIncrement;
    }

    public void incrementCounter(Freestyler freestyler) {
        counterIncrement.accept(freestyler);
    }

    // Punti in base al formato della serata: ottavi e finale a tre sono indipendenti e possono coincidere
    public BigDecimal points(boolean roundOf16Night, boolean threeWayFinalNight) {
        return switch (this) {
            case FIRST_PLACE -> BigDecimal.valueOf(5);
            case SECOND_PLACE -> BigDecimal.valueOf(roundOf16Night || threeWayFinalNight ? 4 : 3);
            case THIRD_PLACE -> BigDecimal.valueOf(3);
            case SEMIFINAL -> BigDecimal.valueOf(roundOf16Night ? 3 : 2);
            case QUARTERFINAL -> BigDecimal.valueOf(roundOf16Night ? 2 : 1);
            case ROUND_OF_16 -> BigDecimal.ONE;
            case SELECTION -> new BigDecimal("0.5");
            case JURY -> new BigDecimal("2.5");
        };
    }

    // Piazzamenti validi solo con il formato di serata corrispondente
    public boolean isAllowed(boolean roundOf16Night, boolean threeWayFinalNight) {
        return switch (this) {
            case THIRD_PLACE -> threeWayFinalNight;
            case ROUND_OF_16 -> roundOf16Night;
            default -> true;
        };
    }

}