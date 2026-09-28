package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Fatores ambientais adicionais ao escalar térmico coordenado do Paper.
 *
 * <p>Altitude não aparece aqui: World#getTemperature(x,y,z) já delega ao
 * cálculo de temperatura do biome com BlockPos + sea level no Paper 26.3.</p>
 */
public record AmbientTemperatureFactors(
        double skyExposure,
        int timeOfDayTicks,
        AmbientWeather weather
) {
    public AmbientTemperatureFactors {
        if (!Double.isFinite(skyExposure)
                || skyExposure < 0.0D
                || skyExposure > 1.0D) {
            throw new IllegalArgumentException(
                    "A exposição ao céu deve ficar entre 0 e 1"
            );
        }
        if (timeOfDayTicks < 0 || timeOfDayTicks >= 24_000) {
            throw new IllegalArgumentException(
                    "O horário deve ficar entre 0 e 23999 ticks"
            );
        }
        Objects.requireNonNull(weather, "weather ambiente");
    }
}
