package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
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

    public PaperLocalClimateResolver(
            World world,
            CalendarView calendar,
            ClimateProfileClassifier classifier,
            ClimatePolicy climatePolicy
    ) {
        this.world = Objects.requireNonNull(world, "mundo");
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.classifier = Objects.requireNonNull(classifier, "classificador climático");
        this.climatePolicy = Objects.requireNonNull(climatePolicy, "política climática");
    }

    public ClimateSnapshot snapshotAt(Block block) {
        Objects.requireNonNull(block, "bloco");
        if (block.getWorld() != world) {
            throw new IllegalArgumentException(
                    "O bloco precisa pertencer ao mundo climático configurado"
            );
        }

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
}
