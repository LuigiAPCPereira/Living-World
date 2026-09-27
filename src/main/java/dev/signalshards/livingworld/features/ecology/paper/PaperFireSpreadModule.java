package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FireSpreadSuitabilityPolicy;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class PaperFireSpreadModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperEcologySettings settings;
    private final PaperLocalClimateResolver climate;
    private final FireSpreadSuitabilityPolicy spreadPolicy;
    private final DoubleSupplier random;

    public PaperFireSpreadModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            FireSpreadSuitabilityPolicy spreadPolicy
    ) {
        this(
                plugin,
                world,
                settings,
                climate,
                spreadPolicy,
                () -> ThreadLocalRandom.current().nextDouble()
        );
    }

    PaperFireSpreadModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            FireSpreadSuitabilityPolicy spreadPolicy,
            DoubleSupplier random
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração ecológica");
        this.climate = Objects.requireNonNull(climate, "clima local");
        this.spreadPolicy = Objects.requireNonNull(
                spreadPolicy,
                "política de propagação"
        );
        this.random = Objects.requireNonNull(random, "fonte aleatória");
    }

    @Override
    public void enable() {
        if (settings.fireSpreadEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (settings.fireSpreadEnabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        if (event.getBlock().getWorld() != world
                || event.getCause() != BlockIgniteEvent.IgniteCause.SPREAD) {
            return;
        }

        double acceptanceChance = spreadPolicy.acceptanceChance(
                climate.snapshotAt(event.getBlock()),
                settings.fireSpreadStrength()
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
