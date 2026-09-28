package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.SeasonalLeafVisualPolicy;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/** Partículas locais sob copas; não agenda trabalho nem modifica blocos. */
public final class PaperSeasonalLeavesModule implements LivingWorldModule, Listener {
    private static final long INTERVAL_NANOS = 5_000_000_000L;
    // Nove sondas pontuais, não uma varredura de volume ou chunk.
    private static final int[][] OFFSETS = {
            {0, 2, 0}, {0, 3, 0}, {0, 4, 0},
            {2, 3, 0}, {-2, 3, 0}, {0, 3, 2}, {0, 3, -2},
            {0, 5, 0}, {0, 6, 0}
    };
    private final Plugin plugin;
    private final World world;
    private final CalendarView calendar;
    private final PaperLocalClimateResolver climate;
    private final SeasonalLeafVisualPolicy policy;
    private final LongSupplier clock;
    private final DoubleSupplier random;
    private final Map<UUID, Long> lastAttempt = new HashMap<>();
    private boolean active;

    public PaperSeasonalLeavesModule(Plugin plugin, World world, CalendarView calendar,
                                    PaperLocalClimateResolver climate, SeasonalLeafVisualPolicy policy) {
        this(plugin, world, calendar, climate, policy, System::nanoTime,
                () -> ThreadLocalRandom.current().nextDouble());
    }

    PaperSeasonalLeavesModule(Plugin plugin, World world, CalendarView calendar,
                              PaperLocalClimateResolver climate, SeasonalLeafVisualPolicy policy,
                              LongSupplier clock, DoubleSupplier random) {
        this.plugin = plugin;
        this.world = world;
        this.calendar = calendar;
        this.climate = climate;
        this.policy = policy;
        this.clock = clock;
        this.random = random;
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
    }

    @Override
    public void disable() {
        active = false;
        HandlerList.unregisterAll(this);
        lastAttempt.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!active || event.isCancelled() || event instanceof PlayerTeleportEvent) {
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null || to.getWorld() != world || from.getWorld() != world
                || (from.getX() == to.getX() && from.getY() == to.getY()
                && from.getZ() == to.getZ())) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isOnline() || player.getWorld() != world) {
            return;
        }
        long now = clock.getAsLong();
        Long previous = lastAttempt.get(player.getUniqueId());
        if (previous != null && now - previous < INTERVAL_NANOS) {
            return;
        }
        lastAttempt.put(player.getUniqueId(), now);
        for (int[] offset : OFFSETS) {
            int x = to.getBlockX() + offset[0];
            int y = to.getBlockY() + offset[1];
            int z = to.getBlockZ() + offset[2];
            if (y < world.getMinHeight() || y >= world.getMaxHeight()
                    || !world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            if (!(block.getBlockData() instanceof Leaves)) {
                continue;
            }
            var effect = policy.select(climate.snapshotAt(block), calendar.currentSeason(),
                    random.getAsDouble());
            Particle particle = switch (effect) {
                case NONE -> null;
                case SPRING -> Particle.CHERRY_LEAVES;
                case AUTUMN -> Particle.PALE_OAK_LEAVES;
                case SNOW -> Particle.SNOWFLAKE;
            };
            if (particle != null) {
                player.spawnParticle(particle, x + 0.5D, y - 0.15D, z + 0.5D,
                        3, 0.35D, 0.1D, 0.35D, 0.01D);
            }
            return; // Uma única tentativa ecológica por janela, mesmo se rejeitada.
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastAttempt.remove(event.getPlayer().getUniqueId());
    }
}
