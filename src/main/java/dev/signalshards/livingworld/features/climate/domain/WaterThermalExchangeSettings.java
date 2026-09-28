package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de conversão entre temperatura da água e carga térmica corporal.
 *
 * <p>Os parâmetros representam comportamento de gameplay, não fisiologia
 * clínica. A água possui uma temperatura de equilíbrio; a diferença entre
 * esse alvo e o estado corporal atual produz uma taxa bounded.</p>
 */
public record WaterThermalExchangeSettings(
        double neutralWaterTemperatureCelsius,
        double celsiusPerFullThermalLoad,
        double responsePerSecond,
        double maxAbsoluteLoadPerSecond
) {
    public WaterThermalExchangeSettings {
        double[] values = {
                neutralWaterTemperatureCelsius,
                celsiusPerFullThermalLoad,
                responsePerSecond,
                maxAbsoluteLoadPerSecond
        };
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os parâmetros de troca com água devem ser finitos");
            }
        }
        if (celsiusPerFullThermalLoad <= 0.0D) {
            throw new IllegalArgumentException("A escala térmica em Celsius deve ser positiva");
        }
        if (responsePerSecond <= 0.0D) {
            throw new IllegalArgumentException("A resposta térmica por segundo deve ser positiva");
        }
        if (maxAbsoluteLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("O limite de taxa térmica deve ser positivo");
        }
    }

    public static WaterThermalExchangeSettings livingWorldDefaults() {
        return new WaterThermalExchangeSettings(
                32.0D,
                20.0D,
                0.08D,
                0.12D
        );
    }
}
