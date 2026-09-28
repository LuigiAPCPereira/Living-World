package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning bounded para converter vento/abrigo em transferência térmica do ar.
 */
public record AirTransferSettings(
        double maxWindBonus,
        double maxShelterReduction,
        double minimumTransferFactor,
        double maximumTransferFactor
) {
    public AirTransferSettings {
        double[] values = {
                maxWindBonus,
                maxShelterReduction,
                minimumTransferFactor,
                maximumTransferFactor
        };
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os parâmetros de transferência devem ser finitos");
            }
        }
        if (maxWindBonus < 0.0D) {
            throw new IllegalArgumentException("O bônus máximo de vento não pode ser negativo");
        }
        if (maxShelterReduction < 0.0D || maxShelterReduction > 1.0D) {
            throw new IllegalArgumentException("A redução máxima de abrigo deve ficar entre 0 e 1");
        }
        if (minimumTransferFactor < 0.0D) {
            throw new IllegalArgumentException("O fator mínimo não pode ser negativo");
        }
        if (maximumTransferFactor < minimumTransferFactor) {
            throw new IllegalArgumentException("O fator máximo não pode ser menor que o mínimo");
        }
    }

    public static AirTransferSettings livingWorldDefaults() {
        return new AirTransferSettings(
                1.0D,
                0.75D,
                0.20D,
                2.50D
        );
    }
}
