package dev.signalshards.livingworld.features.climate.domain;

/**
 * Limites configuráveis do índice corporal para as faixas térmicas.
 *
 * <p>Os defaults são uma calibração inicial de gameplay e não um modelo
 * fisiológico. O índice permanece independente de Celsius.</p>
 */
public record PlayerThermalThresholds(
        double extremeColdMax,
        double freezingMax,
        double veryColdMax,
        double coldMax,
        double coolMax,
        double comfortableMax,
        double warmMax,
        double hotMax,
        double overheatingMax
) {
    public PlayerThermalThresholds {
        double[] values = {
                extremeColdMax,
                freezingMax,
                veryColdMax,
                coldMax,
                coolMax,
                comfortableMax,
                warmMax,
                hotMax,
                overheatingMax
        };
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os limites térmicos devem ser finitos");
            }
            if (value <= PlayerThermalState.MIN_LOAD
                    || value >= PlayerThermalState.MAX_LOAD) {
                throw new IllegalArgumentException(
                        "Os limites térmicos devem ficar estritamente entre -1 e 1"
                );
            }
        }
        for (int index = 1; index < values.length; index++) {
            if (values[index] <= values[index - 1]) {
                throw new IllegalArgumentException(
                        "Os limites térmicos devem estar em ordem estritamente crescente"
                );
            }
        }
    }

    public static PlayerThermalThresholds livingWorldDefaults() {
        return new PlayerThermalThresholds(
                -0.65D,
                -0.45D,
                -0.30D,
                -0.20D,
                -0.10D,
                0.10D,
                0.25D,
                0.45D,
                0.65D
        );
    }
}
