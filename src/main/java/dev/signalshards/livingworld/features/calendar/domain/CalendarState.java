package dev.signalshards.livingworld.features.calendar.domain;

public record CalendarState(long elapsedDays) {
    public CalendarState {
        if (elapsedDays < 0) {
            throw new IllegalArgumentException("Os dias transcorridos não podem ser negativos");
        }
    }

    public static CalendarState initial() {
        return new CalendarState(0);
    }

    public CalendarState advanceDays(long days) {
        if (days < 0) {
            throw new IllegalArgumentException("Não é possível retroceder o calendário por esta operação");
        }

        return new CalendarState(Math.addExact(elapsedDays, days));
    }
}
