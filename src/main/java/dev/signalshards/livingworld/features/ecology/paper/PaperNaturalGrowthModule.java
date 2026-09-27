package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateState;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class PaperNaturalGrowthModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperEcologySettings settings;
    private final CalendarView calendar;
    private final ClimateProfileClassifier classifier;
    private final ClimatePolicy climatePolicy;
    private final NaturalGrowthSuitabilityPolicy growthPolicy;
    private final DoubleSupplier random;

    public PaperNaturalGrowthModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            CalendarView calendar,
            ClimateProfileClassifier classifier,
            ClimatePolicy climatePolicy,
            NaturalGrowthSuitabilityPolicy growthPolicy
    ) {
        this(
                plugin,
                world,
                settings,
                calendar,
                classifier,
                climatePolicy,
                growthPolicy,
                () -> ThreadLocalRandom.current().nextDouble()
        );
    }

    PaperNaturalGrowthModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            CalendarView calendar,
            ClimateProfileClassifier classifier,
            ClimatePolicy climatePolicy,
            NaturalGrowthSuitabilityPolicy growthPolicy,
            DoubleSupplier random
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração ecológica");
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.classifier = Objects.requireNonNull(classifier, "classificador climático");
        this.climatePolicy = Objects.requireNonNull(climatePolicy, "política climática");
        this.growthPolicy = Objects.requireNonNull(growthPolicy, "política de crescimento");
        this.random = Objects.requireNonNull(random, "fonte aleatória");
    }

    @Override
    public void enable() {
        if (settings.naturalGrowthEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (settings.naturalGrowthEnabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNaturalGrowth(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (block.getWorld() != world
                || !(block.getBlockData() instanceof Ageable current)
                || !(event.getNewState().getBlockData() instanceof Ageable next)
                || next.getAge() <= current.getAge()) {
            return;
        }

        if (rejectNaturalGrowth(block, settings.cropGrowthStrength())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNaturalTreeGrowth(StructureGrowEvent event) {
        Block origin = event.getLocation().getBlock();
        if (event.getWorld() != world
                || event.isFromBonemeal()
                || !(origin.getBlockData() instanceof Sapling)) {
            return;
        }

        if (rejectNaturalGrowth(origin, settings.treeGrowthStrength())) {
            event.setCancelled(true);
        }
    }

    private boolean rejectNaturalGrowth(Block block, double strength) {
        double temperature = world.getTemperature(
                block.getX(),
                block.getY(),
                block.getZ()
        );
        double humidity = world.getHumidity(
                block.getX(),
                block.getY(),
                block.getZ()
        );
        var profile = classifier.classify(temperature, humidity);
        var climate = climatePolicy.evaluate(
                profile,
                calendar.currentSeason(),
                ClimateState.stable()
        );
        double acceptanceChance = growthPolicy.acceptanceChance(
                climate,
                strength
        );
        return nextRandomSample() >= acceptanceChance;
    }

    private double nextRandomSample() {
        double sample = random.getAsDouble();
        if (!Double.isFinite(sample) || sample < 0.0D || sample >= 1.0D) {
            throw new IllegalStateException(
                    "A fonte aleatória ecológica deve retornar valor em [0, 1)"
            );
        }
        return sample;
    }
}
