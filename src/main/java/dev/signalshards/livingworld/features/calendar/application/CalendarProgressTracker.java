package dev.signalshards.livingworld.features.calendar.application;

import dev.signalshards.livingworld.features.calendar.domain.WorldTime;

/**
 * Observa o relógio absoluto do mundo sem assumir que a tarefa agendada executa
 * exatamente no intervalo esperado.
 *
 * <p>Saltos administrativos são absorvidos no baseline e não avançam o
 * calendário lógico. O salto noturno causado por sono pode avançar dias quando
 * realmente cruza a fronteira de 24.000 ticks.</p>
 */
public final class CalendarProgressTracker {
    private long observedFullTime;

    public CalendarProgressTracker(long initialFullTime) {
        this.observedFullTime = initialFullTime;
    }

    public long sample(long currentFullTime) {
        if (currentFullTime < observedFullTime) {
            observedFullTime = currentFullTime;
            return 0;
        }

        long advancedDays = dayIndex(currentFullTime) - dayIndex(observedFullTime);
        observedFullTime = currentFullTime;
        return advancedDays;
    }

    public long observeSkip(
            CalendarTimeSkipCause cause,
            long currentFullTime,
            long skipAmount
    ) {
        long naturalAdvance = sample(currentFullTime);
        long targetFullTime = Math.addExact(currentFullTime, skipAmount);
        long skippedDays = 0;

        if (cause == CalendarTimeSkipCause.SONO && targetFullTime > currentFullTime) {
            skippedDays = Math.max(0, dayIndex(targetFullTime) - dayIndex(currentFullTime));
        }

        observedFullTime = targetFullTime;
        return Math.addExact(naturalAdvance, skippedDays);
    }

    private long dayIndex(long fullTime) {
        return Math.floorDiv(fullTime, WorldTime.TICKS_PER_DAY);
    }
}
