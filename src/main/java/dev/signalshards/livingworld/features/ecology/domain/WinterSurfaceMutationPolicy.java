package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionProfile;

import java.util.Objects;

/**
 * Decide apenas mutações físicas de gelo; não conhece Bukkit nem blocos.
 */
public final class WinterSurfaceMutationPolicy {
    public WinterSurfaceMutation decide(
            WinterThermalPhase phase,
            EnvironmentalDimensionProfile dimension,
            WinterSurfaceTarget target
    ) {
        Objects.requireNonNull(phase, "fase térmica");
        Objects.requireNonNull(dimension, "perfil dimensional");
        Objects.requireNonNull(target, "alvo");

        if (!dimension.frozenSurfaces()) {
            return target == WinterSurfaceTarget.OWNED_ICE
                    ? WinterSurfaceMutation.THAW_OWNED_ICE
                    : WinterSurfaceMutation.NONE;
        }

        return switch (phase) {
            case FREEZING -> target == WinterSurfaceTarget.EXPOSED_SOURCE_WATER
                    ? WinterSurfaceMutation.FREEZE_WATER
                    : WinterSurfaceMutation.NONE;
            case HOLDING -> WinterSurfaceMutation.NONE;
            case THAWING -> target == WinterSurfaceTarget.OWNED_ICE
                    ? WinterSurfaceMutation.THAW_OWNED_ICE
                    : WinterSurfaceMutation.NONE;
        };
    }
}
