package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning bounded de quanto o isolamento pode reduzir troca térmica com o ar.
 */
public record ArmorInsulationSettings(double maximumAirTransferReduction) {
    public ArmorInsulationSettings {
        if (!Double.isFinite(maximumAirTransferReduction)
                || maximumAirTransferReduction < 0.0D
                || maximumAirTransferReduction >= 1.0D) {
            throw new IllegalArgumentException(
                    "A redução máxima do ar deve ser finita e ficar entre 0 e 1"
            );
        }
    }

    public static ArmorInsulationSettings livingWorldDefaults() {
        return new ArmorInsulationSettings(0.75D);
    }
}
