package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Converte contato/submersão/profundidade em efeitos térmicos independentes.
 *
 * <p>Submersão controla a velocidade de molhamento e a intensidade básica de
 * transferência. Profundidade aumenta apenas a intensidade da transferência,
 * com bônus limitado. A política não assume que água sempre esfria: o sinal
 * do fluxo dependerá de WaterTemperature em uma etapa posterior.</p>
 */
public final class WaterExposurePolicy {
    private final WaterExposureSettings settings;

    public WaterExposurePolicy() {
        this(WaterExposureSettings.livingWorldDefaults());
    }

    public WaterExposurePolicy(WaterExposureSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de exposição à água");
    }

    public WaterExposureEffect evaluate(WaterExposure exposure) {
        Objects.requireNonNull(exposure, "exposição à água");
        if (exposure.submergedFraction() == 0.0D) {
            return WaterExposureEffect.none();
        }

        double fraction = exposure.submergedFraction();
        double wetnessPerSecond = settings.fullSubmersionWetnessPerSecond() * fraction;
        double normalizedDepth = Math.clamp(
                exposure.depthBlocks() / settings.depthForMaxTransferBonusBlocks(),
                0.0D,
                1.0D
        );
        double transferFactor = fraction * (
                1.0D + (settings.maxDepthTransferBonus() * normalizedDepth)
        );

        return new WaterExposureEffect(
                new WetnessRate(wetnessPerSecond),
                new WaterThermalTransferFactor(transferFactor)
        );
    }
}
