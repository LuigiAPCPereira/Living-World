package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Efeito puro de uma exposição à água antes da integração temporal.
 */
public record WaterExposureEffect(
        WetnessRate wetnessRate,
        WaterThermalTransferFactor thermalTransferFactor
) {
    public WaterExposureEffect {
        Objects.requireNonNull(wetnessRate, "taxa de wetness");
        Objects.requireNonNull(thermalTransferFactor, "fator de transferência térmica");
    }

    public static WaterExposureEffect none() {
        return new WaterExposureEffect(
                WetnessRate.neutral(),
                WaterThermalTransferFactor.neutral()
        );
    }
}
