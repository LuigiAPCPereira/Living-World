package dev.signalshards.livingworld.features.climate.domain;

/**
 * Limite global de calor local para evitar stacking ilimitado de fontes.
 */
public record LocalHeatSettings(double maximumCombinedLoadPerSecond) {
    public LocalHeatSettings {
        if (!Double.isFinite(maximumCombinedLoadPerSecond)
                || maximumCombinedLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("O limite combinado de calor deve ser positivo");
        }
    }

    public static LocalHeatSettings livingWorldDefaults() {
        return new LocalHeatSettings(0.025D);
    }
}
