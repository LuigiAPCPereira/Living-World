package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Resolve a troca térmica corporal com o ar/microclima.
 *
 * <p>O ambiente define um equilíbrio-alvo normalizado. Wetness aumenta a
 * velocidade da troca sem mudar a temperatura ambiente. O fator externo
 * permite que vento/abrigo sejam introduzidos depois sem acoplar esta policy
 * a geometria, Paper ou scans.</p>
 */
public final class AirThermalExchangePolicy {
    private final AirThermalExchangeSettings settings;

    public AirThermalExchangePolicy() {
        this(AirThermalExchangeSettings.livingWorldDefaults());
    }

    public AirThermalExchangePolicy(AirThermalExchangeSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de troca térmica do ar");
    }

    public ThermalExchangeRate exchangeRate(
            PlayerThermalState currentState,
            AmbientTemperature ambientTemperature,
            WetnessState wetness,
            AirThermalTransferFactor transferFactor
    ) {
        Objects.requireNonNull(currentState, "estado térmico atual");
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        Objects.requireNonNull(wetness, "wetness");
        Objects.requireNonNull(transferFactor, "fator de transferência do ar");
        if (transferFactor.value() == 0.0D) {
            return ThermalExchangeRate.neutral();
        }

        double targetLoad = Math.clamp(
                (ambientTemperature.degreesCelsius() - settings.neutralAmbientTemperatureCelsius())
                        / settings.celsiusPerFullThermalLoad(),
                PlayerThermalState.MIN_LOAD,
                PlayerThermalState.MAX_LOAD
        );
        double wetnessMultiplier = 1.0D + (
                wetness.level() * (settings.maxWetnessTransferMultiplier() - 1.0D)
        );
        double rawRate = (targetLoad - currentState.thermalLoad())
                * settings.dryResponsePerSecond()
                * wetnessMultiplier
                * transferFactor.value();

        return new ThermalExchangeRate(Math.clamp(
                rawRate,
                -settings.maxAbsoluteLoadPerSecond(),
                settings.maxAbsoluteLoadPerSecond()
        ));
    }
}
