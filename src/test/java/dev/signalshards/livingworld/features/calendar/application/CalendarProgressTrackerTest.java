package dev.signalshards.livingworld.features.calendar.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarProgressTrackerTest {
    @Test
    void detectaFronteiraNaturalDeDiaMesmoComAmostragemEsparsa() {
        CalendarProgressTracker tracker = new CalendarProgressTracker(23_990);

        assertEquals(1, tracker.sample(24_010));
    }

    @Test
    void comandoDeTempoNaoAvancaCalendario() {
        CalendarProgressTracker tracker = new CalendarProgressTracker(23_000);

        assertEquals(
                0,
                tracker.observeSkip(CalendarTimeSkipCause.COMANDO, 23_000, 25_000)
        );
        assertEquals(0, tracker.sample(48_000));
    }

    @Test
    void saltoDePluginNaoAvancaCalendario() {
        CalendarProgressTracker tracker = new CalendarProgressTracker(10_000);

        assertEquals(
                0,
                tracker.observeSkip(CalendarTimeSkipCause.PLUGIN, 10_000, 30_000)
        );
        assertEquals(0, tracker.sample(40_000));
    }

    @Test
    void sonoContaODiaQuandoCruzaAMeiaNoiteDoRelogioAbsoluto() {
        CalendarProgressTracker tracker = new CalendarProgressTracker(13_000);

        assertEquals(
                1,
                tracker.observeSkip(CalendarTimeSkipCause.SONO, 13_000, 11_000)
        );
        assertEquals(0, tracker.sample(24_000));
    }

    @Test
    void retrocessoNaoCriaDiasNegativos() {
        CalendarProgressTracker tracker = new CalendarProgressTracker(48_000);

        assertEquals(0, tracker.sample(12_000));
        assertEquals(0, tracker.sample(12_100));
    }
}
