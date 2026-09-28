package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimension;
import org.bukkit.World;

import java.util.Objects;

public final class PaperEnvironmentalDimensionMapper {
    private PaperEnvironmentalDimensionMapper() {
    }

    public static EnvironmentalDimension fromPaper(
            World.Environment environment
    ) {
        Objects.requireNonNull(environment, "ambiente Paper");
        return switch (environment) {
            case NORMAL -> EnvironmentalDimension.OVERWORLD;
            case NETHER -> EnvironmentalDimension.NETHER;
            case THE_END -> EnvironmentalDimension.END;
            case CUSTOM -> EnvironmentalDimension.CUSTOM;
        };
    }
}
