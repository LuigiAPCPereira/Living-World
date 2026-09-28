package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AirThermalTransferFactor;
import dev.signalshards.livingworld.features.climate.domain.ThermalExchangeRate;
import dev.signalshards.livingworld.features.climate.domain.WaterTemperature;
import dev.signalshards.livingworld.features.climate.domain.WetnessRate;

import java.util.Objects;
import java.util.Optional;

/**
 * Breakdown diagnóstico de uma resolução térmica instantânea.
 */
public record ThermalExchangeResolution(
        ThermalExchangeRate airRate,
        ThermalExchangeRate waterRate,
        ThermalExchangeRate activityRate,
        ThermalExchangeRate localHeatRate,
        ThermalExchangeRate directExposureRate,
        ThermalExchangeRate netRate,
        WetnessRate wetnessRate,
        AirThermalTransferFactor airTransferFactor,
        Optional<WaterTemperature> waterTemperature
) {
    public ThermalExchangeResolution {
        Objects.requireNonNull(airRate, "taxa do ar");
        Objects.requireNonNull(waterRate, "taxa da água");
        Objects.requireNonNull(activityRate, "taxa de atividade");
        Objects.requireNonNull(localHeatRate, "taxa de calor local");
        Objects.requireNonNull(directExposureRate, "taxa de exposição direta");
        Objects.requireNonNull(netRate, "taxa líquida");
        Objects.requireNonNull(wetnessRate, "taxa de wetness");
        Objects.requireNonNull(airTransferFactor, "fator de transferência do ar");
        Objects.requireNonNull(waterTemperature, "temperatura opcional da água");
    }
}
