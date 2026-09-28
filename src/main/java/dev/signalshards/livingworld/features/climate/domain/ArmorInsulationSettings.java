package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning bounded de quanto o isolamento pode reduzir troca térmica com o ar.
 */
public record ArmorInsulationSettings(
        double maximumAirTransferReduction,
        double maximumWetInsulationLoss
) {
    public ArmorInsulationSettings {
        if (!Double.isFinite(maximumAirTransferReduction)
                || maximumAirTransferReduction < 0.0D
                || maximumAirTransferReduction >= 1.0D) {
            throw new IllegalArgumentException(
                    "A redução máxima do ar deve ser finita e ficar entre 0 e 1"
            );
        }
        if (!Double.isFinite(maximumWetInsulationLoss)
                || maximumWetInsulationLoss < 0.0D
                || maximumWetInsulationLoss > 1.0D) {
            throw new IllegalArgumentException(
                    "A perda máxima por wetness deve ficar entre 0 e 1"
            );
        }
    }

    public static ArmorInsulationSettings livingWorldDefaults() {
        return new ArmorInsulationSettings(0.75D, 0.50D);
    }
}
