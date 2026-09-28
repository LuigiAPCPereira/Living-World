package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperaturePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.ClimateState;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public final class PaperLocalClimateResolver {
    private final World world;
    private final CalendarView calendar;
    private final ClimateProfileClassifier classifier;
    private final ClimatePolicy climatePolicy;
    private final AmbientTemperaturePolicy ambientTemperaturePolicy;

    public PaperLocalClimateResolver(
            World world,
            CalendarView calendar,
            ClimateProfileClassifier classifier,
            ClimatePolicy climatePolicy
    ) {
        this(
                world,
                calendar,
                classifier,
                climatePolicy,
                new AmbientTemperaturePolicy()
        );
    }

    PaperLocalClimateResolver(
            World world,
            CalendarView calendar,
            ClimateProfileClassifier classifier,
            ClimatePolicy climatePolicy,
            AmbientTemperaturePolicy ambientTemperaturePolicy
    ) {
        this.world = Objects.requireNonNull(world, "mundo");
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.classifier = Objects.requireNonNull(classifier, "classificador climático");
        this.climatePolicy = Objects.requireNonNull(climatePolicy, "política climática");
        this.ambientTemperaturePolicy = Objects.requireNonNull(
                ambientTemperaturePolicy,
                "política de temperatura ambiente"
        );
    }

    public ClimateSnapshot snapshotAt(Block block) {
        requireConfiguredWorld(block);

        var profile = classifier.classify(
                world.getTemperature(block.getX(), block.getY(), block.getZ()),
                world.getHumidity(block.getX(), block.getY(), block.getZ())
        );
        return climatePolicy.evaluate(
                profile,
                calendar.currentSeason(),
                ClimateState.stable()
        );
    }

    public AmbientTemperature ambientTemperatureAt(Block block) {
        requireConfiguredWorld(block);
        return ambientTemperaturePolicy.temperature(
                world.getTemperature(block.getX(), block.getY(), block.getZ()),
                calendar.currentSeason()
        );
    }

    private void requireConfiguredWorld(Block block) {
        Objects.requireNonNull(block, "bloco");
        if (block.getWorld() != world) {
            throw new IllegalArgumentException(
                    "O bloco precisa pertencer ao mundo climático configurado"
            );
        }
    }
}
