package dev.signalshards.livingworld.features.calendar.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldTimeTest {
    @Test
    void representaInicioDoPrimeiroDia() {
        WorldTime time = new WorldTime(0);

        assertEquals(0, time.dayIndex());
        assertEquals(0, time.tickOfDay());
    }

    @Test
    void mantemUltimoTickNoMesmoDia() {
        WorldTime time = new WorldTime(WorldTime.TICKS_PER_DAY - 1);

        assertEquals(0, time.dayIndex());
        assertEquals(23_999, time.tickOfDay());
    }

    @Test
    void avancaParaODiaSeguinteNoLimite() {
        WorldTime time = new WorldTime(WorldTime.TICKS_PER_DAY);

        assertEquals(1, time.dayIndex());
        assertEquals(0, time.tickOfDay());
    }

    @Test
    void normalizaTicksNegativosSemQuebrarOInvarianteDoDia() {
        WorldTime time = new WorldTime(-1);

        assertEquals(-1, time.dayIndex());
        assertEquals(23_999, time.tickOfDay());
    }
}
