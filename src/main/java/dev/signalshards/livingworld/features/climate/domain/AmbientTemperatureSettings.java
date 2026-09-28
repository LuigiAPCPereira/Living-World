package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de microclima temporal para AmbientTemperature.
 */
public record AmbientTemperatureSettings(
        double daytimeWarmingCelsius,
        double nighttimeCoolingCelsius,
        double rainCoolingCelsius,
        double thunderCoolingCelsius
) {
    public AmbientTemperatureSettings {
        double[] values = {
                daytimeWarmingCelsius,
                nighttimeCoolingCelsius,
                rainCoolingCelsius,
                thunderCoolingCelsius
        };
        for (double value : values) {
            if (!Double.isFinite(value) || value < 0.0D) {
                throw new IllegalArgumentException(
                        "Os ajustes de microclima devem ser finitos e não negativos"
                );
            }
        }
        if (thunderCoolingCelsius < rainCoolingCelsius) {
            throw new IllegalArgumentException(
                    "Thunder não pode resfriar menos que chuva"
            );
        }
    }

    public static AmbientTemperatureSettings livingWorldDefaults() {
        return new AmbientTemperatureSettings(
                2.5D,
                3.5D,
                1.5D,
                2.5D
        );
    }
}
