package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de troca térmica do corpo com o ar.
 *
 * <p>Wetness aumenta transferência, mas nunca altera AmbientTemperature.
 * Vento, abrigo e isolamento poderão modificar o fator de transferência
 * sem mudar esta política.</p>
 */
public record AirThermalExchangeSettings(
        double neutralAmbientTemperatureCelsius,
        double celsiusPerFullThermalLoad,
        double dryResponsePerSecond,
        double maxWetnessTransferMultiplier,
        double maxAbsoluteLoadPerSecond
) {
    public AirThermalExchangeSettings {
        double[] values = {
                neutralAmbientTemperatureCelsius,
                celsiusPerFullThermalLoad,
                dryResponsePerSecond,
                maxWetnessTransferMultiplier,
                maxAbsoluteLoadPerSecond
        };
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os parâmetros de troca com ar devem ser finitos");
            }
        }
        if (celsiusPerFullThermalLoad <= 0.0D) {
            throw new IllegalArgumentException("A escala térmica em Celsius deve ser positiva");
        }
        if (dryResponsePerSecond <= 0.0D) {
            throw new IllegalArgumentException("A resposta térmica seca deve ser positiva");
        }
        if (maxWetnessTransferMultiplier < 1.0D) {
            throw new IllegalArgumentException("Wetness não pode reduzir o multiplicador máximo abaixo de 1");
        }
        if (maxAbsoluteLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("O limite de taxa térmica deve ser positivo");
        }
    }

    public static AirThermalExchangeSettings livingWorldDefaults() {
        return new AirThermalExchangeSettings(
                20.0D,
                25.0D,
                0.015D,
                2.5D,
                0.06D
        );
    }
}
