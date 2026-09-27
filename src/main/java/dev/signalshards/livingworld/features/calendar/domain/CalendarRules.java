package dev.signalshards.livingworld.features.calendar.domain;

public record CalendarRules(int monthsPerYear, int daysPerMonth) {
    public CalendarRules {
        if (monthsPerYear <= 0) {
            throw new IllegalArgumentException("A quantidade de meses por ano deve ser maior que zero");
        }
        if (daysPerMonth <= 0) {
            throw new IllegalArgumentException("A quantidade de dias por mês deve ser maior que zero");
        }
    }

    public static CalendarRules livingWorldDefaults() {
        return new CalendarRules(12, 8);
    }

    public long daysPerYear() {
        return Math.multiplyExact((long) monthsPerYear, daysPerMonth);
    }

    public CalendarDate dateFor(CalendarState state) {
        long elapsedDays = state.elapsedDays();
        long yearIndex = Math.floorDiv(elapsedDays, daysPerYear());
        long dayOfYear = Math.floorMod(elapsedDays, daysPerYear());
        int monthIndex = (int) (dayOfYear / daysPerMonth);
        int dayIndex = (int) (dayOfYear % daysPerMonth);

        return new CalendarDate(yearIndex + 1, monthIndex + 1, dayIndex + 1);
    }
}
