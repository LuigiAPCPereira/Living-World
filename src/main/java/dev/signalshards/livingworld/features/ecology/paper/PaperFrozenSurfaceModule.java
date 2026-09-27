package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FrozenSurfacePolicy;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class PaperFrozenSurfaceModule implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final World world;
    private final PaperEcologySettings settings;
    private final PaperLocalClimateResolver climate;
    private final FrozenSurfacePolicy policy;

    public PaperFrozenSurfaceModule(
            Plugin plugin,
            World world,
            PaperEcologySettings settings,
            PaperLocalClimateResolver climate,
            FrozenSurfacePolicy policy
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração ecológica");
        this.climate = Objects.requireNonNull(climate, "clima local");
        this.policy = Objects.requireNonNull(policy, "política de superfícies congeladas");
    }

    @Override
    public void enable() {
        if (settings.frozenSurfacesEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void disable() {
        if (settings.frozenSurfacesEnabled()) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNaturalFormation(BlockFormEvent event) {
        if (event.getBlock().getWorld() != world
                || event instanceof EntityBlockFormEvent
                || event instanceof BlockSpreadEvent
                || !isTrackedFormation(event)) {
            return;
        }

        if (!policy.allowFormation(climate.snapshotAt(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNaturalFade(BlockFadeEvent event) {
        if (event.getBlock().getWorld() != world
                || !isTrackedFrozenMaterial(event.getBlock().getType())) {
            return;
        }

        if (policy.preserveFromFade(climate.snapshotAt(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    private boolean isTrackedFormation(BlockFormEvent event) {
        Material current = event.getBlock().getType();
        Material next = event.getNewState().getType();

        if (next == Material.ICE) {
            return current == Material.WATER;
        }
        if (next == Material.SNOW) {
            return current == Material.AIR || current == Material.SNOW;
        }
        return false;
    }

    private boolean isTrackedFrozenMaterial(Material material) {
        return material == Material.ICE || material == Material.SNOW;
    }
}
