package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de quanto water resistance pode retardar a entrada de wetness.
 */
public record ArmorWetnessSettings(double maximumWetnessIngressReduction) {
    public ArmorWetnessSettings {
        if (!Double.isFinite(maximumWetnessIngressReduction)
                || maximumWetnessIngressReduction < 0.0D
                || maximumWetnessIngressReduction >= 1.0D) {
            throw new IllegalArgumentException(
                    "A redução máxima de wetness deve ficar entre 0 e 1"
            );
        }
    }

    public static ArmorWetnessSettings livingWorldDefaults() {
        return new ArmorWetnessSettings(0.80D);
    }
}
