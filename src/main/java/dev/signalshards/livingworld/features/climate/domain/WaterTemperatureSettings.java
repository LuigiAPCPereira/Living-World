package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning da aproximação espacial de temperatura da água.
 *
 * <p>Água superficial acompanha parcialmente o ambiente; profundidade
 * reduz essa influência e aproxima a água de uma temperatura estável.
 * Os defaults são parâmetros de gameplay e devem ser calibrados por
 * playtest, não tratados como constantes universais da natureza.</p>
 */
public record WaterTemperatureSettings(
        double stableTemperatureCelsius,
        double surfaceAmbientCoupling,
        double depthForMaxStabilizationBlocks,
        double maxDepthStabilization,
        double minimumLiquidTemperatureCelsius,
        double maximumLiquidTemperatureCelsius
) {
    public WaterTemperatureSettings {
        double[] values = {
                stableTemperatureCelsius,
                surfaceAmbientCoupling,
                depthForMaxStabilizationBlocks,
                maxDepthStabilization,
                minimumLiquidTemperatureCelsius,
                maximumLiquidTemperatureCelsius
        };
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os parâmetros de temperatura da água devem ser finitos");
            }
        }
        if (surfaceAmbientCoupling < 0.0D || surfaceAmbientCoupling > 1.0D) {
            throw new IllegalArgumentException("O acoplamento superficial deve ficar entre 0 e 1");
        }
        if (depthForMaxStabilizationBlocks <= 0.0D) {
            throw new IllegalArgumentException("A profundidade de estabilização deve ser positiva");
        }
        if (maxDepthStabilization < 0.0D || maxDepthStabilization > 1.0D) {
            throw new IllegalArgumentException("A estabilização máxima deve ficar entre 0 e 1");
        }
        if (minimumLiquidTemperatureCelsius >= maximumLiquidTemperatureCelsius) {
            throw new IllegalArgumentException("O intervalo líquido de temperatura deve ser crescente");
        }
        if (stableTemperatureCelsius < minimumLiquidTemperatureCelsius
                || stableTemperatureCelsius > maximumLiquidTemperatureCelsius) {
            throw new IllegalArgumentException("A temperatura estável deve pertencer ao intervalo líquido");
        }
    }

    public static WaterTemperatureSettings livingWorldDefaults() {
        return new WaterTemperatureSettings(
                8.0D,
                0.65D,
                32.0D,
                0.70D,
                0.0D,
                40.0D
        );
    }
}
