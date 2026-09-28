package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Perfis conservadores por dimensão vanilla/Paper.
 *
 * <p>Worldgen customizado dentro do Overworld continua OVERWORLD; namespace do
 * biome não participa desta decisão.</p>
 */
public final class EnvironmentalDimensionPolicy {
    private static final EnvironmentalDimensionProfile OVERWORLD =
            new EnvironmentalDimensionProfile(
                    1.0D,
                    1.0D,
                    1.0D,
                    true,
                    true
            );
    private static final EnvironmentalDimensionProfile NON_TERRESTRIAL =
            new EnvironmentalDimensionProfile(
                    0.0D,
                    0.0D,
                    0.0D,
                    false,
                    false
            );

    public EnvironmentalDimensionProfile profileFor(
            EnvironmentalDimension dimension
    ) {
        Objects.requireNonNull(dimension, "dimensão ambiental");
        return switch (dimension) {
            case OVERWORLD -> OVERWORLD;
            case NETHER, END, CUSTOM -> NON_TERRESTRIAL;
        };
    }
}
