package dev.signalshards.livingworld.features.calendar.domain;

public record CalendarDate(long year, int month, int day) {
    public CalendarDate {
        if (year <= 0) {
            throw new IllegalArgumentException("O ano deve ser maior que zero");
        }
        if (month <= 0) {
            throw new IllegalArgumentException("O mês deve ser maior que zero");
        }
        if (day <= 0) {
            throw new IllegalArgumentException("O dia deve ser maior que zero");
        }
    }
}
