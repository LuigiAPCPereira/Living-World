package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning Vanilla+ para contato térmico direto.
 *
 * <p>As taxas são carga térmica normalizada por segundo, não graus Celsius.</p>
 */
public record DirectThermalExposureSettings(
        double fireLoadPerSecond,
        double lavaLoadPerSecond,
        double powderSnowLoadPerSecond
) {
    public DirectThermalExposureSettings {
        if (!Double.isFinite(fireLoadPerSecond) || fireLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("A taxa de fogo deve ser positiva");
        }
        if (!Double.isFinite(lavaLoadPerSecond) || lavaLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("A taxa de lava deve ser positiva");
        }
        if (!Double.isFinite(powderSnowLoadPerSecond) || powderSnowLoadPerSecond >= 0.0D) {
            throw new IllegalArgumentException(
                    "A taxa de powder snow deve ser negativa"
            );
        }
        if (lavaLoadPerSecond <= fireLoadPerSecond) {
            throw new IllegalArgumentException(
                    "Contato com lava deve ser termicamente mais severo que fogo"
            );
        }
    }

    public static DirectThermalExposureSettings livingWorldDefaults() {
        return new DirectThermalExposureSettings(
                0.025D,
                0.080D,
                -0.030D
        );
    }
}
