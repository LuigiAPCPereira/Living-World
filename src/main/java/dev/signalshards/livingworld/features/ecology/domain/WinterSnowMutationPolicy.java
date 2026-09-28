package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionProfile;

import java.util.Objects;

public final class WinterSnowMutationPolicy {
    private final PhysicalSnowSettings settings;

    public WinterSnowMutationPolicy(PhysicalSnowSettings settings) {
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de neve física"
        );
    }

    public WinterSnowMutation decide(
            WinterThermalPhase phase,
            EnvironmentalDimensionProfile dimension,
            boolean precipitating,
            WinterSnowTarget target,
            int currentLayers
    ) {
        Objects.requireNonNull(phase, "fase térmica");
        Objects.requireNonNull(dimension, "perfil dimensional");
        Objects.requireNonNull(target, "alvo de neve");
        if (currentLayers < 0 || currentLayers > 8) {
            throw new IllegalArgumentException(
                    "Camadas atuais de neve devem ficar entre 0 e 8"
            );
        }

        if (!dimension.frozenSurfaces()) {
            return target == WinterSnowTarget.OWNED_SNOW
                    ? WinterSnowMutation.MELT_SNOW_LAYER
                    : WinterSnowMutation.NONE;
        }
        if (phase == WinterThermalPhase.THAWING) {
            return target == WinterSnowTarget.OWNED_SNOW
                    ? WinterSnowMutation.MELT_SNOW_LAYER
                    : WinterSnowMutation.NONE;
        }
        if (phase != WinterThermalPhase.FREEZING || !precipitating) {
            return WinterSnowMutation.NONE;
        }
        if (target == WinterSnowTarget.EXPOSED_SUPPORT) {
            return WinterSnowMutation.PLACE_SNOW;
        }
        if (target == WinterSnowTarget.OWNED_SNOW
                && currentLayers < settings.maxLayers()) {
            return WinterSnowMutation.ADD_SNOW_LAYER;
        }
        return WinterSnowMutation.NONE;
    }
}
