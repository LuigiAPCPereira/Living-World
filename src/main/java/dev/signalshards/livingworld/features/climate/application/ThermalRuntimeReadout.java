package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;

import java.util.Objects;

/**
 * Snapshot somente leitura para diagnóstico do runtime térmico.
 */
public record ThermalRuntimeReadout(
        PlayerThermalBand thermalBand,
        double thermalLoad,
        double wetness,
        double ambientCelsius,
        PlayerActivity activity,
        double submergedFraction,
        double waterDepthBlocks,
        double precipitationExposure,
        DirectThermalExposure directExposure,
        double windExposure,
        double shelterFactor,
        int armorPieces,
        int localHeatSources,
        double airRatePerSecond,
        double waterRatePerSecond,
        double activityRatePerSecond,
        double localHeatRatePerSecond,
        double directExposureRatePerSecond,
        double netRatePerSecond,
        double wetnessRatePerSecond
) {
    public ThermalRuntimeReadout {
        Objects.requireNonNull(thermalBand, "faixa térmica");
        Objects.requireNonNull(activity, "atividade");
        Objects.requireNonNull(directExposure, "exposição direta");
    }
}
