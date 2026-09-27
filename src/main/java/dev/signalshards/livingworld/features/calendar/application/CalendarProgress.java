package dev.signalshards.livingworld.features.calendar.application;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.seasons.domain.SeasonTransition;

import java.util.Objects;
import java.util.Optional;

public record CalendarProgress(
        long daysAdvanced,
        CalendarDate previousDate,
        CalendarDate currentDate,
        Optional<SeasonTransition> seasonTransition
) {
    public CalendarProgress {
        if (daysAdvanced <= 0) {
            throw new IllegalArgumentException("O avanço do calendário deve ser maior que zero");
        }
        Objects.requireNonNull(previousDate, "data anterior");
        Objects.requireNonNull(currentDate, "data atual");
        Objects.requireNonNull(seasonTransition, "transição de estação");
    }
}
