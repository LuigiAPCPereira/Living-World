package dev.signalshards.livingworld.features.landmarks.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.landmarks.application.LandmarkDiscovery;
import dev.signalshards.livingworld.features.landmarks.application.LandmarkEntryTracker;
import dev.signalshards.livingworld.features.landmarks.application.ResolvedLandmark;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Detector bounded de entrada em landmarks gerados.
 *
 * <p>Somente movimentos que realmente mudaram de bloco são observados. O
 * detector consulta exclusivamente as estruturas que intersectam o chunk de
 * destino já carregado; não procura estruturas por raio e não carrega chunks.</p>
 */
public final class PaperLandmarkDiscoveryModule
        implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final DiscoveryService discovery;
    private final PaperLandmarkResolver resolver;
    private final LandmarkEntryTracker tracker;

    public PaperLandmarkDiscoveryModule(
            JavaPlugin plugin,
            DiscoveryService discovery,
            PaperLandmarkResolver resolver,
            LandmarkEntryTracker tracker
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.discovery = Objects.requireNonNull(
                discovery,
                "serviço de descobertas"
        );
        this.resolver = Objects.requireNonNull(
                resolver,
                "resolver de landmarks"
        );
        this.tracker = Objects.requireNonNull(
                tracker,
                "tracker de landmarks"
        );
    }

    @Override
    public void enable() {
        if (resolver.eligibleStructureCount() == 0) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        tracker.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) {
            return;
        }

        Location destination = event.getTo();
        List<ResolvedLandmark> current = resolveAt(destination);
        var player = event.getPlayer();

        for (ResolvedLandmark landmark : tracker.observe(
                player.getUniqueId(),
                current
        )) {
            LandmarkDiscovery.recordEncounter(
                    discovery,
                    destination.getWorld().getUID(),
                    player.getUniqueId(),
                    landmark.id(),
                    landmark.structureKey()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        tracker.forget(event.getPlayer().getUniqueId());
    }

    private List<ResolvedLandmark> resolveAt(Location location) {
        Chunk destinationChunk = location.getChunk();
        List<ResolvedLandmark> current = new ArrayList<>();

        for (GeneratedStructure generated : destinationChunk.getStructures()) {
            if (!containsBlock(generated.getBoundingBox(), location)) {
                continue;
            }
            resolver.resolve(generated).ifPresent(current::add);
        }
        return List.copyOf(current);
    }

    /**
     * StructureStart usa máximos inclusivos. CraftGeneratedStructure copia
     * esses inteiros diretamente para o BoundingBox Bukkit, cujo contains()
     * usa máximo exclusivo; por isso o teste de bloco precisa ser explícito.
     */
    private boolean containsBlock(BoundingBox box, Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= box.getMinX() && x <= box.getMaxX()
                && y >= box.getMinY() && y <= box.getMaxY()
                && z >= box.getMinZ() && z <= box.getMaxZ();
    }
}
