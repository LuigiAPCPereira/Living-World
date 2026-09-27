package dev.signalshards.livingworld.features.calendar.application;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.calendar.domain.CalendarRules;
import dev.signalshards.livingworld.features.calendar.domain.CalendarState;
import dev.signalshards.livingworld.features.calendar.persistence.CalendarStateStore;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;

import java.util.Objects;
import java.util.Optional;

public final class CalendarRuntime {
    private final CalendarRules rules;
    private final SeasonCycle seasons;
    private final CalendarStateStore store;
    private CalendarState state;

    private CalendarRuntime(
            CalendarRules rules,
            SeasonCycle seasons,
            CalendarStateStore store,
            CalendarState state
    ) {
        this.rules = Objects.requireNonNull(rules, "regras do calendário");
        this.seasons = Objects.requireNonNull(seasons, "ciclo de estações");
        this.store = Objects.requireNonNull(store, "persistência do calendário");
        this.state = Objects.requireNonNull(state, "estado do calendário");
    }

    public static CalendarRuntime load(
            CalendarRules rules,
            SeasonCycle seasons,
            CalendarStateStore store
    ) {
        return new CalendarRuntime(rules, seasons, store, store.load());
    }

    public CalendarDate currentDate() {
        return rules.dateFor(state);
    }

    public Optional<CalendarProgress> advanceDays(long days) {
        if (days < 0) {
            throw new IllegalArgumentException("O avanço de dias não pode ser negativo");
        }
        if (days == 0) {
            return Optional.empty();
        }

        CalendarDate previousDate = rules.dateFor(state);
        CalendarState nextState = state.advanceDays(days);
        CalendarDate currentDate = rules.dateFor(nextState);

        store.save(nextState);
        state = nextState;

        return Optional.of(new CalendarProgress(
                days,
                previousDate,
                currentDate,
                seasons.transitionBetween(previousDate, currentDate)
        ));
    }
}
