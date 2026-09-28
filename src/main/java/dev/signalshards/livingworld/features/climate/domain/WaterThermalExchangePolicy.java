package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Converte temperatura + intensidade de contato com água em troca térmica corporal.
 *
 * <p>A política calcula um equilíbrio-alvo normalizado para a temperatura da
 * água e move o corpo em direção a ele. Portanto a mesma água pode resfriar
 * um jogador quente e aquecer um jogador ainda mais frio que seu equilíbrio.
 * O resultado é uma {@link ThermalExchangeRate}, não uma alteração direta em
 * graus Celsius.</p>
 */
public final class WaterThermalExchangePolicy {
    private final WaterThermalExchangeSettings settings;

    public WaterThermalExchangePolicy() {
        this(WaterThermalExchangeSettings.livingWorldDefaults());
    }

    public WaterThermalExchangePolicy(WaterThermalExchangeSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de troca térmica da água");
    }

    public ThermalExchangeRate exchangeRate(
            PlayerThermalState currentState,
            WaterTemperature waterTemperature,
            WaterThermalTransferFactor transferFactor
    ) {
        Objects.requireNonNull(currentState, "estado térmico atual");
        Objects.requireNonNull(waterTemperature, "temperatura da água");
        Objects.requireNonNull(transferFactor, "fator de transferência da água");
        if (transferFactor.value() == 0.0D) {
            return ThermalExchangeRate.neutral();
        }

        double targetLoad = targetLoad(waterTemperature);
        double difference = targetLoad - currentState.thermalLoad();
        double rawRate = difference * transferFactor.value() * settings.responsePerSecond();
        double boundedRate = Math.clamp(
                rawRate,
                -settings.maxAbsoluteLoadPerSecond(),
                settings.maxAbsoluteLoadPerSecond()
        );
        return new ThermalExchangeRate(boundedRate);
    }

    private double targetLoad(WaterTemperature waterTemperature) {
        return Math.clamp(
                (waterTemperature.degreesCelsius() - settings.neutralWaterTemperatureCelsius())
                        / settings.celsiusPerFullThermalLoad(),
                PlayerThermalState.MIN_LOAD,
                PlayerThermalState.MAX_LOAD
        );
    }
}
