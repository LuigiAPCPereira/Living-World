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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class PaperGroundCoverSpreadModule
        implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperEcologySettings settings;
    private final PaperLocalClimateResolver climate;
    private final NaturalGrowthSuitabilityPolicy growthPolicy;
    private final CalendarView calendar;
    private final SeasonalEcologyModifier seasonalModifier;
    private final DoubleSupplier random;

    public PaperGroundCoverSpreadModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            NaturalGrowthSuitabilityPolicy growthPolicy
    ) {
        this(
                plugin,
                world,
                settings,
                climate,
                growthPolicy,
                new CalendarView() {
                    public CalendarDate currentDate() {
                        return new CalendarDate(1, 1, 1);
                    }

                    public Season currentSeason() {
                        return Season.PRIMAVERA;
                    }

                    public int daysPerMonth() {
                        return 8;
                    }
                },
                new DefaultSeasonalEcologyModifier()
        );
    }

    PaperGroundCoverSpreadModule(
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
                new CalendarView() {
                    public CalendarDate currentDate() {
                        return new CalendarDate(1, 1, 1);
                    }

                    public Season currentSeason() {
                        return Season.PRIMAVERA;
                    }

                    public int daysPerMonth() {
                        return 8;
                    }
                },
                new DefaultSeasonalEcologyModifier(),
                random
        );
    }

    public PaperGroundCoverSpreadModule(
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

    PaperGroundCoverSpreadModule(
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
        this.growthPolicy = Objects.requireNonNull(
                growthPolicy,
                "política de crescimento"
        );
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.seasonalModifier = Objects.requireNonNull(
                seasonalModifier,
                "modificador sazonal"
        );
        this.random = Objects.requireNonNull(random, "fonte aleatória");
    }

    @Override
    public void enable() {
        if (settings.groundCoverSpreadEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (settings.groundCoverSpreadEnabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGroundCoverSpread(BlockSpreadEvent event) {
        if (event.getBlock().getWorld() != world
                || event.getSource().getType() != Material.GRASS_BLOCK
                || event.getBlock().getType() != Material.DIRT
                || event.getNewState().getType() != Material.GRASS_BLOCK) {
            return;
        }

        double acceptanceChance = growthPolicy.acceptanceChance(
                climate.snapshotAt(event.getBlock()),
                calendar.currentSeason(),
                GrowthCategory.GROUND_COVER,
                settings.groundCoverSpreadStrength()
                ,
                seasonalModifier
        );
        if (nextRandomSample() >= acceptanceChance) {
            event.setCancelled(true);
        }
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
