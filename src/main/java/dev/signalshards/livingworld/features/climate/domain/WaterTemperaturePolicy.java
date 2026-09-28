package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Resolve uma temperatura de água a partir do ambiente e da profundidade.
 *
 * <p>O modelo é deliberadamente bounded: água de superfície acompanha parte
 * da temperatura ambiente; água profunda é mais estável. A policy não usa
 * nome de bioma, não conhece jogador e não introduz estado temporal próprio.
 * Persistência térmica de corpos d'água só deve existir se gameplay futuro
 * justificar esse custo.</p>
 */
public final class WaterTemperaturePolicy {
    private final WaterTemperatureSettings settings;

    public WaterTemperaturePolicy() {
        this(WaterTemperatureSettings.livingWorldDefaults());
    }

    public WaterTemperaturePolicy(WaterTemperatureSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de temperatura da água");
    }

    public WaterTemperature temperature(
            AmbientTemperature ambientTemperature,
            double depthBlocks
    ) {
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        if (!Double.isFinite(depthBlocks) || depthBlocks < 0.0D) {
            throw new IllegalArgumentException("A profundidade deve ser finita e não negativa");
        }

        double stable = settings.stableTemperatureCelsius();
        double surface = surfaceTemperature(ambientTemperature, stable);
        double stabilization = depthStabilization(depthBlocks);
        double temperature = stable + ((surface - stable) * (1.0D - stabilization));

        return new WaterTemperature(Math.clamp(
                temperature,
                settings.minimumLiquidTemperatureCelsius(),
                settings.maximumLiquidTemperatureCelsius()
        ));
    }

    private double surfaceTemperature(
            AmbientTemperature ambientTemperature,
            double stableTemperature
    ) {
        return stableTemperature + (
                (ambientTemperature.degreesCelsius() - stableTemperature)
                        * settings.surfaceAmbientCoupling()
        );
    }

    private double depthStabilization(double depthBlocks) {
        double normalizedDepth = Math.clamp(
                depthBlocks / settings.depthForMaxStabilizationBlocks(),
                0.0D,
                1.0D
        );
        return settings.maxDepthStabilization() * normalizedDepth;
    }
}
