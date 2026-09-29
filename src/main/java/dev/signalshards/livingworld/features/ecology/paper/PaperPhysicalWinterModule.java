package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import dev.signalshards.livingworld.features.climate.paper.PaperAmbientTemperatureProvider;
import dev.signalshards.livingworld.features.climate.paper.PaperEnvironmentalDimensionMapper;
import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceMutation;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceMutationPolicy;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import dev.signalshards.livingworld.features.ecology.domain.WinterThermalPhasePolicy;
import dev.signalshards.livingworld.features.ecology.domain.PhysicalSnowSettings;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Owner bounded do inverno físico ativo para ICE.
 *
 * <p>Não varre chunks: cada pulse parte apenas de jogadores online e sonda um
 * número fixo de colunas em chunks que já estão carregados.</p>
 */
public final class PaperPhysicalWinterModule
        implements LivingWorldModule, Listener {
    private static final int[][] PROBE_PATTERN = {
            {0, 0},
            {1, 0}, {0, 1}, {-1, 0}, {0, -1},
            {1, 1}, {-1, 1}, {-1, -1}, {1, -1},
            {2, 0}, {0, 2}, {-2, 0}, {0, -2},
            {2, 2}, {-2, 2}, {-2, -2}, {2, -2}
    };

    private final Plugin plugin;
    private final PaperPhysicalWinterSettings settings;
    private final PaperAmbientTemperatureProvider ambientTemperature;
    private final PaperWinterSurfaceOwnershipStore ownershipStore;
    private final WinterThermalPhasePolicy thermalPhasePolicy;
    private final WinterSurfaceMutationPolicy mutationPolicy;
    private final PaperWinterSurfaceTargetResolver targetResolver;
    private final EnvironmentalDimensionPolicy dimensionPolicy;
    private final PaperWinterSnowMutator snowMutator;
    private final EnvironmentalPerformanceMetrics performanceMetrics;
    private final LongSupplier nanoClock;
    private final Map<UUID, Integer> probeCursors = new HashMap<>();
    private BukkitTask task;
    private boolean active;

    public PaperPhysicalWinterModule(
            Plugin plugin,
            PaperPhysicalWinterSettings settings,
            PaperAmbientTemperatureProvider ambientTemperature,
            PhysicalSnowSettings snowSettings
    ) {
        this(
                plugin,
                settings,
                ambientTemperature,
                new PaperWinterSurfaceOwnershipStore(plugin, settings.domain()),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy(),
                new PaperWinterSnowMutator(snowSettings),
                new EnvironmentalPerformanceMetrics(),
                System::nanoTime
        );
    }

    public PaperPhysicalWinterModule(
            Plugin plugin,
            PaperPhysicalWinterSettings settings,
            PaperAmbientTemperatureProvider ambientTemperature,
            PhysicalSnowSettings snowSettings,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this(
                plugin,
                settings,
                ambientTemperature,
                new PaperWinterSurfaceOwnershipStore(plugin, settings.domain()),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy(),
                new PaperWinterSnowMutator(snowSettings),
                performanceMetrics,
                System::nanoTime
        );
    }

    PaperPhysicalWinterModule(
            Plugin plugin,
            PaperPhysicalWinterSettings settings,
            PaperAmbientTemperatureProvider ambientTemperature,
            PaperWinterSurfaceOwnershipStore ownershipStore,
            WinterThermalPhasePolicy thermalPhasePolicy,
            WinterSurfaceMutationPolicy mutationPolicy,
            PaperWinterSurfaceTargetResolver targetResolver,
            EnvironmentalDimensionPolicy dimensionPolicy,
            PaperWinterSnowMutator snowMutator
    ) {
        this(
                plugin,
                settings,
                ambientTemperature,
                ownershipStore,
                thermalPhasePolicy,
                mutationPolicy,
                targetResolver,
                dimensionPolicy,
                snowMutator,
                new EnvironmentalPerformanceMetrics(),
                System::nanoTime
        );
    }

    PaperPhysicalWinterModule(
            Plugin plugin,
            PaperPhysicalWinterSettings settings,
            PaperAmbientTemperatureProvider ambientTemperature,
            PaperWinterSurfaceOwnershipStore ownershipStore,
            WinterThermalPhasePolicy thermalPhasePolicy,
            WinterSurfaceMutationPolicy mutationPolicy,
            PaperWinterSurfaceTargetResolver targetResolver,
            EnvironmentalDimensionPolicy dimensionPolicy,
            PaperWinterSnowMutator snowMutator,
            EnvironmentalPerformanceMetrics performanceMetrics,
            LongSupplier nanoClock
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de inverno físico"
        );
        this.ambientTemperature = Objects.requireNonNull(
                ambientTemperature,
                "temperatura ambiente"
        );
        this.ownershipStore = Objects.requireNonNull(
                ownershipStore,
                "store de ownership"
        );
        this.thermalPhasePolicy = Objects.requireNonNull(
                thermalPhasePolicy,
                "policy de fase térmica"
        );
        this.mutationPolicy = Objects.requireNonNull(
                mutationPolicy,
                "policy de mutation"
        );
        this.targetResolver = Objects.requireNonNull(
                targetResolver,
                "resolver de alvo"
        );
        this.dimensionPolicy = Objects.requireNonNull(
                dimensionPolicy,
                "policy de dimensão"
        );
        this.snowMutator = Objects.requireNonNull(
                snowMutator,
                "mutator de neve"
        );
        this.performanceMetrics = Objects.requireNonNull(
                performanceMetrics,
                "métricas de performance"
        );
        this.nanoClock = Objects.requireNonNull(
                nanoClock,
                "relógio de nanos"
        );
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
        long period = settings.updatePeriodTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::pulse,
                period,
                period
        );
    }

    @Override
    public void disable() {
        active = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(this);
        probeCursors.clear();
    }

    void pulse() {
        if (!active) {
            return;
        }
        long started = nanoClock.getAsLong();
        try {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.isOnline() || player.isDead()) {
                    continue;
                }
                processPlayer(player);
            }
        } finally {
            performanceMetrics.add(
                    EnvironmentalMetric.WINTER_UPDATE_NANOS,
                    Math.max(0L, nanoClock.getAsLong() - started)
            );
        }
    }

    void processPlayer(Player player) {
        World world = player.getWorld();
        var origin = player.getLocation();
        int cursor = probeCursors.getOrDefault(
                player.getUniqueId(),
                Math.floorMod(
                        player.getUniqueId().hashCode(),
                        PROBE_PATTERN.length
                )
        );
        int mutations = 0;

        for (int probe = 0;
             probe < settings.probesPerPlayer()
                     && mutations < settings.maxMutationsPerPlayer();
             probe++) {
            int[] offset = PROBE_PATTERN[
                    Math.floorMod(cursor + probe, PROBE_PATTERN.length)
            ];
            int x = origin.getBlockX() + scaledOffset(offset[0]);
            int z = origin.getBlockZ() + scaledOffset(offset[1]);
            int chunkX = Math.floorDiv(x, 16);
            int chunkZ = Math.floorDiv(z, 16);
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }

            performanceMetrics.increment(EnvironmentalMetric.WINTER_PROBES);
            Block surface = world.getHighestBlockAt(
                    x,
                    z,
                    HeightMap.WORLD_SURFACE
            );
            if (processCandidate(surface)) {
                mutations++;
                performanceMetrics.increment(
                        EnvironmentalMetric.WINTER_MUTATIONS
                );
            }
        }

        probeCursors.put(
                player.getUniqueId(),
                Math.floorMod(
                        cursor + settings.probesPerPlayer(),
                        PROBE_PATTERN.length
                )
        );
    }

    boolean processCandidate(Block block) {
        var chunk = block.getChunk();
        WinterSurfaceOwnershipLedger ownership = ownershipStore.load(chunk);
        var dimension = dimensionPolicy.profileFor(
                PaperEnvironmentalDimensionMapper.fromPaper(
                        block.getWorld().getEnvironment()
                )
        );
        var phase = thermalPhasePolicy.phaseFor(
                ambientTemperature.ambientTemperatureAt(block)
        );
        if (snowMutator.mutate(
                block,
                ownership,
                phase,
                dimension,
                block.getWorld().hasStorm()
        )) {
            if (ownership.isDirty()) {
                ownershipStore.save(chunk, ownership);
            }
            return true;
        }
        var target = targetResolver.resolve(block, ownership);
        WinterSurfaceMutation mutation = mutationPolicy.decide(
                phase,
                dimension,
                target
        );

        return switch (mutation) {
            case NONE -> false;
            case FREEZE_WATER -> freeze(block, chunk, ownership);
            case THAW_OWNED_ICE -> thaw(block, chunk, ownership);
        };
    }

    private boolean freeze(
            Block block,
            org.bukkit.Chunk chunk,
            WinterSurfaceOwnershipLedger ownership
    ) {
        WinterSurfacePosition position = position(block);
        if (!ownership.claim(position, WinterSurfaceKind.ICE)) {
            return false;
        }
        block.setType(Material.ICE, false);
        ownershipStore.save(chunk, ownership);
        return true;
    }

    private boolean thaw(
            Block block,
            org.bukkit.Chunk chunk,
            WinterSurfaceOwnershipLedger ownership
    ) {
        WinterSurfacePosition position = position(block);
        block.setType(Material.WATER, false);
        ownership.release(position);
        ownershipStore.save(chunk, ownership);
        return true;
    }

    private int scaledOffset(int patternCoordinate) {
        if (patternCoordinate == 0) {
            return 0;
        }
        int halfRadius = Math.max(1, settings.radiusBlocks() / 2);
        return switch (patternCoordinate) {
            case -2 -> -settings.radiusBlocks();
            case -1 -> -halfRadius;
            case 1 -> halfRadius;
            case 2 -> settings.radiusBlocks();
            default -> throw new IllegalStateException(
                    "Offset de probe desconhecido: " + patternCoordinate
            );
        };
    }

    private WinterSurfacePosition position(Block block) {
        return WinterSurfacePosition.fromWorld(
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }

    private void releaseOwnership(Block block) {
        var chunk = block.getChunk();
        var ledger = ownershipStore.load(chunk);
        if (ledger.release(position(block))) {
            ownershipStore.save(chunk, ledger);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        releaseOwnership(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        releaseOwnership(event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        releaseOwnership(event.getBlock());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        probeCursors.remove(event.getPlayer().getUniqueId());
    }
}
