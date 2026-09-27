package dev.signalshards.livingworld.features.calendar.application;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.calendar.domain.CalendarRules;
import dev.signalshards.livingworld.features.calendar.domain.CalendarState;
import dev.signalshards.livingworld.features.calendar.persistence.CalendarStateStore;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarRuntimeTest {
    @Test
    void persisteAvancoEDerivaTransicaoDeEstacao() {
        InMemoryStore store = new InMemoryStore(new CalendarState(23));
        CalendarRuntime runtime = CalendarRuntime.load(
                CalendarRules.livingWorldDefaults(),
                SeasonCycle.livingWorldDefaults(),
                store
        );

        CalendarProgress progress = runtime.advanceDays(1).orElseThrow();

        assertEquals(new CalendarDate(1, 4, 1), progress.currentDate());
        assertEquals(24, store.state.elapsedDays());
        assertEquals(Season.VERAO, progress.seasonTransition().orElseThrow().current());
    }

    @Test
    void zeroDiasNaoPersisteNemEmiteProgresso() {
        InMemoryStore store = new InMemoryStore(CalendarState.initial());
        CalendarRuntime runtime = CalendarRuntime.load(
                CalendarRules.livingWorldDefaults(),
                SeasonCycle.livingWorldDefaults(),
                store
        );

        assertTrue(runtime.advanceDays(0).isEmpty());
        assertEquals(0, store.saves);
    }

    private static final class InMemoryStore implements CalendarStateStore {
        private CalendarState state;
        private int saves;

        private InMemoryStore(CalendarState state) {
            this.state = state;
        }

        @Override
        public CalendarState load() {
            return state;
        }

        @Override
        public void save(CalendarState state) {
            this.state = state;
            saves++;
        }
    }
}
