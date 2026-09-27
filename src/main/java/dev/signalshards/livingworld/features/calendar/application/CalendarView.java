package dev.signalshards.livingworld.features.calendar.application;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.seasons.domain.Season;

public interface CalendarView {
    CalendarDate currentDate();

    Season currentSeason();

    int daysPerMonth();
}
