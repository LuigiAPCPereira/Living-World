package dev.signalshards.livingworld.features.seasons.domain;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonCycleTest {
    private final SeasonCycle cycle = SeasonCycle.livingWorldDefaults();

    @Test
    void mapeiaTresMesesParaCadaEstacao() {
        assertEquals(Season.PRIMAVERA, cycle.seasonFor(new CalendarDate(1, 1, 1)));
        assertEquals(Season.PRIMAVERA, cycle.seasonFor(new CalendarDate(1, 3, 8)));
        assertEquals(Season.VERAO, cycle.seasonFor(new CalendarDate(1, 4, 1)));
        assertEquals(Season.OUTONO, cycle.seasonFor(new CalendarDate(1, 7, 1)));
        assertEquals(Season.INVERNO, cycle.seasonFor(new CalendarDate(1, 10, 1)));
        assertEquals(Season.INVERNO, cycle.seasonFor(new CalendarDate(1, 12, 8)));
    }

    @Test
    void naoEmiteTransicaoDentroDaMesmaEstacao() {
        Optional<SeasonTransition> transition = cycle.transitionBetween(
                new CalendarDate(1, 1, 1),
                new CalendarDate(1, 3, 8)
        );

        assertTrue(transition.isEmpty());
    }

    @Test
    void emiteTransicaoAoCruzarFronteiraDeEstacao() {
        Optional<SeasonTransition> transition = cycle.transitionBetween(
                new CalendarDate(1, 3, 8),
                new CalendarDate(1, 4, 1)
        );

        assertEquals(
                new SeasonTransition(Season.PRIMAVERA, Season.VERAO),
                transition.orElseThrow()
        );
    }

    @Test
    void rejeitaMesForaDoCicloConfigurado() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cycle.seasonFor(new CalendarDate(1, 13, 1))
        );
    }
}
