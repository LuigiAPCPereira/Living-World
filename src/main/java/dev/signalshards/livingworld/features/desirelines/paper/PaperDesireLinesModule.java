package dev.signalshards.livingworld.features.desirelines.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgressListener;
import dev.signalshards.livingworld.features.desirelines.application.PathTrafficDecay;
import dev.signalshards.livingworld.features.desirelines.application.PathTrafficLedger;
import dev.signalshards.livingworld.features.desirelines.domain.PathWearPolicy;
import dev.signalshards.livingworld.features.desirelines.domain.PathWearSettings;
import dev.signalshards.livingworld.features.desirelines.domain.PathWearStage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PaperDesireLinesModule implements LivingWorldModule, Listener, CalendarProgressListener {
    private final JavaPlugin plugin;
    private final World world;
    private final PathWearSettings settings;
    private final PathWearPolicy policy;
    private final PaperChunkTrafficStore store;
    private final Map<Long, CachedChunk> cache = new HashMap<>();

    private BukkitTask flushTask;

    public PaperDesireLinesModule(
            JavaPlugin plugin,
            World world,
            PathWearSettings settings
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.settings = Objects.requireNonNull(settings, "configuração de Desire Lines");
        this.policy = new PathWearPolicy(settings);
        this.store = new PaperChunkTrafficStore(plugin, settings);
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        flushTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::flushDirtyChunks,
                settings.flushPeriodTicks(),
                settings.flushPeriodTicks()
        );
    }

    @Override
    public void disable() {
        if (!settings.enabled()) {
            return;
        }

        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }
        flushAll();
        HandlerList.unregisterAll(this);
        cache.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent || !event.hasExplicitlyChangedBlock()) {
            return;
        }

        Location to = event.getTo();
        if (to.getWorld() != world) {
            return;
        }

        int groundY = (int) Math.floor(to.getY() - 0.2D);
        Block ground = world.getBlockAt(to.getBlockX(), groundY, to.getBlockZ());
        Material material = ground.getType();
        if (material != Material.GRASS_BLOCK && material != Material.DIRT) {
            return;
        }

        Chunk chunk = ground.getChunk();
        CachedChunk cached = cache.computeIfAbsent(
                chunkKey(chunk),
                ignored -> new CachedChunk(chunk, store.load(chunk))
        );

        int positionKey = packPosition(ground);
        if (material == Material.DIRT && !cached.ledger.contains(positionKey)) {
            return;
        }

        int visits = cached.ledger.record(positionKey);
        if (visits == 0) {
            return;
        }

        applyWear(ground, material, policy.stageFor(visits));
    }

    @Override
    public void onProgress(CalendarProgress progress) {
        if (!settings.enabled()) {
            return;
        }

        for (CachedChunk cached : cache.values()) {
            for (PathTrafficDecay decay : cached.ledger.decayUntouched(progress.daysAdvanced())) {
                applyRecovery(cached.chunk, decay);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getWorld() != world) {
            return;
        }

        CachedChunk cached = cache.remove(chunkKey(event.getChunk()));
        if (cached != null) {
            flush(cached);
        }
    }

    private void applyWear(Block block, Material material, PathWearStage stage) {
        if (material == Material.GRASS_BLOCK && stage != PathWearStage.NATURAL) {
            block.setType(Material.DIRT);
            return;
        }

        if (material == Material.DIRT && stage == PathWearStage.CAMINHO) {
            block.setType(Material.DIRT_PATH);
        }
    }

    private void applyRecovery(Chunk chunk, PathTrafficDecay decay) {
        int localX = decay.positionKey() & 15;
        int localZ = (decay.positionKey() >>> 4) & 15;
        int relativeY = decay.positionKey() >>> 8;
        int y = world.getMinHeight() + relativeY;
        Block block = chunk.getBlock(localX, y, localZ);
        PathWearStage stage = policy.stageFor(decay.currentScore());

        if (block.getType() == Material.DIRT_PATH && stage != PathWearStage.CAMINHO) {
            block.setType(Material.DIRT);
        }
        if (block.getType() == Material.DIRT && stage == PathWearStage.NATURAL) {
            block.setType(Material.GRASS_BLOCK);
        }
    }

    private void flushDirtyChunks() {
        for (CachedChunk cached : cache.values()) {
            flush(cached);
        }
    }

    private void flushAll() {
        for (CachedChunk cached : cache.values()) {
            flush(cached);
        }
    }

    private void flush(CachedChunk cached) {
        if (cached.ledger.isDirty()) {
            store.save(cached.chunk, cached.ledger);
        }
    }

    private long chunkKey(Chunk chunk) {
        return ((long) chunk.getX() << 32) ^ (chunk.getZ() & 0xffffffffL);
    }

    private int packPosition(Block block) {
        int localX = block.getX() & 15;
        int localZ = block.getZ() & 15;
        int relativeY = block.getY() - world.getMinHeight();
        return (relativeY << 8) | (localZ << 4) | localX;
    }

    private record CachedChunk(Chunk chunk, PathTrafficLedger ledger) {
    }
}
