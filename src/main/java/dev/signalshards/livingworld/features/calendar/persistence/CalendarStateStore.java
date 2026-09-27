package dev.signalshards.livingworld.features.calendar.persistence;

import dev.signalshards.livingworld.features.calendar.domain.CalendarState;

public interface CalendarStateStore {
    CalendarState load();

    void save(CalendarState state);
}
