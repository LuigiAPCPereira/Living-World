package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

/**
 * Converte perfil de bioma + estação + anomalias limitadas em uma fotografia
 * climática. Não agenda clima no Paper e não altera blocos/entidades.
 */
public final class ClimatePolicy {
    public ClimateSnapshot evaluate(ClimateProfile profile, Season season, ClimateState state) {
        Objects.requireNonNull(profile, "perfil climático");
        Objects.requireNonNull(season, "estação");
        Objects.requireNonNull(state, "estado climático");

        int seasonalTemperatureShift = switch (season) {
            case PRIMAVERA, OUTONO -> 0;
            case VERAO -> 1;
            case INVERNO -> -1;
        };

        int seasonalMoistureShift = switch (season) {
            case PRIMAVERA -> 1;
            case VERAO -> -1;
            case OUTONO, INVERNO -> 0;
        };

        ThermalBand temperature = profile.temperature()
                .shift(seasonalTemperatureShift + state.temperatureShift());
        MoistureBand moisture = profile.moisture()
                .shift(seasonalMoistureShift + state.moistureShift());

        return new ClimateSnapshot(
                temperature,
                moisture,
                weatherFor(moisture, state.stormPressure())
        );
    }

    private WeatherTendency weatherFor(MoistureBand moisture, int stormPressure) {
        if (stormPressure == 2 && moisture == MoistureBand.ENCHARCADO) {
            return WeatherTendency.TEMPESTADE;
        }

        return switch (moisture) {
            case ARIDO, SECO -> WeatherTendency.TEMPO_LIMPO;
            case EQUILIBRADO -> WeatherTendency.ESTAVEL;
            case UMIDO, ENCHARCADO -> WeatherTendency.PRECIPITACAO;
        };
    }
}
