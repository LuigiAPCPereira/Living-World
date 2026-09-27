package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FarmlandMoistureRetentionPolicy;
import org.bukkit.World;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class PaperFarmlandMoistureModule
        implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperEcologySettings settings;
    private final PaperLocalClimateResolver climate;
    private final FarmlandMoistureRetentionPolicy retentionPolicy;
    private final DoubleSupplier random;

    public PaperFarmlandMoistureModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            FarmlandMoistureRetentionPolicy retentionPolicy
    ) {
        this(
                plugin,
                world,
                settings,
                climate,
                retentionPolicy,
                () -> ThreadLocalRandom.current().nextDouble()
        );
    }

    PaperFarmlandMoistureModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            FarmlandMoistureRetentionPolicy retentionPolicy,
            DoubleSupplier random
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração ecológica");
        this.climate = Objects.requireNonNull(climate, "clima local");
        this.retentionPolicy = Objects.requireNonNull(
                retentionPolicy,
                "política de retenção"
        );
        this.random = Objects.requireNonNull(random, "fonte aleatória");
    }

    @Override
    public void enable() {
        if (settings.farmlandMoistureRetentionEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (settings.farmlandMoistureRetentionEnabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        if (event.getBlock().getWorld() != world
                || !(event.getBlock().getBlockData() instanceof Farmland current)
                || !(event.getNewState().getBlockData() instanceof Farmland next)
                || next.getMoisture() >= current.getMoisture()) {
            return;
        }

        double retentionChance = retentionPolicy.retentionChance(
                climate.snapshotAt(event.getBlock()),
                settings.farmlandMoistureRetentionStrength()
        );
        if (nextRandomSample() < retentionChance) {
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
