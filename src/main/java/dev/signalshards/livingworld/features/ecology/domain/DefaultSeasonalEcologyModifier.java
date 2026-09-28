package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

public final class DefaultSeasonalEcologyModifier implements SeasonalEcologyModifier {
    private static final double MIN = 0.0D;
    private static final double MAX = 1.5D;

    @Override
    public double cropMultiplier(Season season) {
        return switch (Objects.requireNonNull(season, "estação")) {
            case PRIMAVERA -> 1.15D;
            case VERAO -> 1.05D;
            case OUTONO -> 0.85D;
            case INVERNO -> 0.50D;
        };
    }

    @Override
    public double treeMultiplier(Season season) {
        return switch (Objects.requireNonNull(season, "estação")) {
            case PRIMAVERA -> 1.10D;
            case VERAO -> 1.05D;
            case OUTONO -> 0.80D;
            case INVERNO -> 0.60D;
        };
    }

    @Override
    public double grassMultiplier(Season season) {
        return switch (Objects.requireNonNull(season, "estação")) {
            case PRIMAVERA -> 1.20D;
            case VERAO -> 0.95D;
            case OUTONO -> 0.75D;
            case INVERNO -> 0.40D;
        };
    }

    public static double clamp(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("O multiplicador ecológico deve ser finito");
        }
        return Math.clamp(value, MIN, MAX);
    }
}
