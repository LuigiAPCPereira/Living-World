package dev.signalshards.livingworld.features.climate.domain;

public record ClimateSnapshot(
        ThermalBand temperature,
        MoistureBand moisture,
        WeatherTendency weatherTendency
) {
}
