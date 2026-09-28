package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning Vanilla+ para chuva e secagem ambiental.
 */
public record WetnessEnvironmentSettings(
        double precipitationWetnessPerSecond,
        double coldPrecipitationMultiplier,
        double baseDryingPerSecond,
        double windDryingBonus,
        double warmDryingBonus,
        double warmDryingStartCelsius,
        double warmDryingFullCelsius,
        double localHeatDryingBonus,
        double localHeatForMaxDryingBonus
) {
    public WetnessEnvironmentSettings {
        double[] nonNegative = {
                precipitationWetnessPerSecond,
                coldPrecipitationMultiplier,
                baseDryingPerSecond,
                windDryingBonus,
                warmDryingBonus,
                localHeatDryingBonus,
                localHeatForMaxDryingBonus
        };
        for (double value : nonNegative) {
            if (!Double.isFinite(value) || value < 0.0D) {
                throw new IllegalArgumentException(
                        "Parâmetros de wetness/secagem devem ser finitos e não negativos"
                );
            }
        }
        if (!Double.isFinite(warmDryingStartCelsius)
                || !Double.isFinite(warmDryingFullCelsius)
                || warmDryingFullCelsius <= warmDryingStartCelsius) {
            throw new IllegalArgumentException(
                    "A faixa térmica de secagem deve ser finita e crescente"
            );
        }
        if (coldPrecipitationMultiplier > 1.0D) {
            throw new IllegalArgumentException(
                    "O multiplicador de precipitação fria não pode exceder 1"
            );
        }
        if (localHeatForMaxDryingBonus <= 0.0D) {
            throw new IllegalArgumentException(
                    "A referência de calor local para secagem deve ser positiva"
            );
        }
    }

    public static WetnessEnvironmentSettings livingWorldDefaults() {
        return new WetnessEnvironmentSettings(
                0.025D,
                0.60D,
                0.004D,
                1.00D,
                0.75D,
                5.0D,
                30.0D,
                2.00D,
                0.010D
        );
    }
}
