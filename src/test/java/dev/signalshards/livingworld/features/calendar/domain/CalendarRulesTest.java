package dev.signalshards.livingworld.features.calendar.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalendarRulesTest {
    private final CalendarRules rules = CalendarRules.livingWorldDefaults();

    @Test
    void iniciaNoPrimeiroDiaDoPrimeiroMesDoPrimeiroAno() {
        assertEquals(new CalendarDate(1, 1, 1), rules.dateFor(CalendarState.initial()));
    }

    @Test
    void avancaParaOMesSeguinteDepoisDeOitoDias() {
        CalendarState state = CalendarState.initial().advanceDays(8);

        assertEquals(new CalendarDate(1, 2, 1), rules.dateFor(state));
    }

    @Test
    void avancaParaOAnoSeguinteDepoisDeNoventaESeisDias() {
        CalendarState state = CalendarState.initial().advanceDays(96);

        assertEquals(new CalendarDate(2, 1, 1), rules.dateFor(state));
    }

    @Test
    void permiteOutraDuracaoDeMesSemMudarOModelo() {
        CalendarRules customRules = new CalendarRules(12, 10);

        assertEquals(new CalendarDate(1, 2, 1), customRules.dateFor(new CalendarState(10)));
        assertEquals(120, customRules.daysPerYear());
    }

    @Test
    void rejeitaRegrasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new CalendarRules(0, 8));
        assertThrows(IllegalArgumentException.class, () -> new CalendarRules(12, 0));
    }

    @Test
    void naoPermiteRetrocederDiasPelaOperacaoNormal() {
        CalendarState state = new CalendarState(10);

        assertThrows(IllegalArgumentException.class, () -> state.advanceDays(-1));
    }
}
