package dev.signalshards.livingworld.features.climate.domain;

/**
 * Modulação ambiental por dimensão, sem depender de nomes de worldgen.
 */
public record EnvironmentalDimensionProfile(
        double seasonalityFactor,
        double dayNightFactor,
        double weatherFactor,
        boolean terrestrialEcology,
        boolean frozenSurfaces
) {
    public EnvironmentalDimensionProfile {
        validateFactor(seasonalityFactor, "seasonality");
        validateFactor(dayNightFactor, "day/night");
        validateFactor(weatherFactor, "weather");
    }

    private static void validateFactor(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(
                    "O fator " + name + " deve ficar entre 0 e 1"
            );
        }
    }
}
