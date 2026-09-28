package dev.signalshards.livingworld.features.seasons.domain;

import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;

import java.util.List;
import java.util.Optional;

public final class SeasonCycle {
    private static final List<Season> DEFAULT_ORDER = List.of(
            Season.PRIMAVERA,
            Season.VERAO,
            Season.OUTONO,
            Season.INVERNO
    );

    private final List<Season> order;
    private final int monthsPerSeason;

    public SeasonCycle(List<Season> order, int monthsPerSeason) {
        if (order.isEmpty()) {
            throw new IllegalArgumentException("O ciclo de estações precisa ter pelo menos uma estação");
        }
        if (monthsPerSeason <= 0) {
            throw new IllegalArgumentException("A quantidade de meses por estação deve ser maior que zero");
        }

        this.order = List.copyOf(order);
        this.monthsPerSeason = monthsPerSeason;
    }

    public static SeasonCycle livingWorldDefaults() {
        return new SeasonCycle(DEFAULT_ORDER, 3);
    }

    public Season seasonFor(CalendarDate date) {
        int monthIndex = date.month() - 1;
        int seasonIndex = monthIndex / monthsPerSeason;
        if (seasonIndex >= order.size()) {
            throw new IllegalArgumentException(
                    "O mês " + date.month() + " não pertence ao ciclo de estações configurado"
            );
        }

        return order.get(seasonIndex);
    }

    public double progressFor(CalendarDate date, int daysPerMonth) {
        seasonFor(date);
        if (daysPerMonth <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade de dias por mês deve ser maior que zero"
            );
        }
        if (date.day() > daysPerMonth) {
            throw new IllegalArgumentException(
                    "O dia " + date.day() + " excede o mês configurado"
            );
        }

        int monthIndexWithinSeason = (date.month() - 1) % monthsPerSeason;
        int totalDays = Math.multiplyExact(monthsPerSeason, daysPerMonth);
        if (totalDays == 1) {
            return 0.5D;
        }
        int dayIndex = (monthIndexWithinSeason * daysPerMonth) + (date.day() - 1);
        return dayIndex / (double) (totalDays - 1);
    }

    public Optional<SeasonTransition> transitionBetween(CalendarDate previous, CalendarDate current) {
        Season previousSeason = seasonFor(previous);
        Season currentSeason = seasonFor(current);

        if (previousSeason == currentSeason) {
            return Optional.empty();
        }

        return Optional.of(new SeasonTransition(previousSeason, currentSeason));
    }
}
