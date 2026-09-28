package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.domain.GrowthCategory;
import dev.signalshards.livingworld.features.ecology.domain.SeasonalEcologyModifier;
import dev.signalshards.livingworld.features.ecology.domain.DefaultSeasonalEcologyModifier;
import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.seasons.domain.Season;
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
    private final PaperLocalClimateResolver climate;
    private final NaturalGrowthSuitabilityPolicy growthPolicy;
    private final CalendarView calendar;
    private final SeasonalEcologyModifier seasonalModifier;
    private final DoubleSupplier random;

    public PaperNaturalGrowthModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            NaturalGrowthSuitabilityPolicy growthPolicy,
            DoubleSupplier random
    ) {
        this(
                plugin,
                world,
                settings,
                climate,
                growthPolicy,
                constantCalendar(),
                new DefaultSeasonalEcologyModifier(),
                random
        );
    }

    public PaperNaturalGrowthModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            NaturalGrowthSuitabilityPolicy growthPolicy,
            CalendarView calendar,
            SeasonalEcologyModifier seasonalModifier
    ) {
        this(
                plugin,
                world,
                settings,
                climate,
                growthPolicy,
                calendar,
                seasonalModifier,
                () -> ThreadLocalRandom.current().nextDouble()
        );
    }

    private static CalendarView constantCalendar() {
        return new CalendarView() {
            @Override
            public CalendarDate currentDate() {
                return new CalendarDate(1, 1, 1);
            }

            @Override
            public Season currentSeason() {
                return Season.PRIMAVERA;
            }

            @Override
            public int daysPerMonth() {
                return 8;
            }
        };
    }

    PaperNaturalGrowthModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            NaturalGrowthSuitabilityPolicy growthPolicy,
            CalendarView calendar,
            SeasonalEcologyModifier seasonalModifier,
            DoubleSupplier random
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração ecológica");
        this.climate = Objects.requireNonNull(climate, "clima local");
        this.growthPolicy = Objects.requireNonNull(growthPolicy, "política de crescimento");
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.seasonalModifier = Objects.requireNonNull(
                seasonalModifier,
                "modificador sazonal"
        );
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

        if (rejectNaturalGrowth(block, GrowthCategory.CROP, settings.cropGrowthStrength())) {
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

        if (rejectNaturalGrowth(origin, GrowthCategory.TREE, settings.treeGrowthStrength())) {
            event.setCancelled(true);
        }
    }

    private boolean rejectNaturalGrowth(Block block, GrowthCategory category, double strength) {
        double acceptanceChance = growthPolicy.acceptanceChance(
                climate.snapshotAt(block),
                calendar.currentSeason(),
                category,
                strength,
                seasonalModifier
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
