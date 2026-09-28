package dev.signalshards.livingworld.features.waystones.paper;
import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.LivingWorldStatusProvider;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRequest;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.waystones.application.WaystoneAnchorIndex;
import dev.signalshards.livingworld.features.waystones.application.WaystoneDiscovery;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Objects;

public final class PaperWaystoneModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final PaperWaystoneSettings settings;
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final DiscoveryService discovery;
    private final MessageCatalog messages;
    private final LivingWorldStatusProvider statusProvider;
    private final WaystoneAnchorIndex anchorIndex = new WaystoneAnchorIndex();
    private final PaperWaystoneMenu menu;

    public PaperWaystoneModule(
            JavaPlugin plugin,
            PaperWaystoneSettings settings,
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            DiscoveryService discovery,
            MessageCatalog messages,
            LivingWorldStatusProvider statusProvider
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração de waystones");
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.travel = Objects.requireNonNull(travel, "viagem de waystones");
        this.discovery = Objects.requireNonNull(discovery, "serviço de descobertas");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.statusProvider = Objects.requireNonNull(
                statusProvider,
                "diagnóstico do Living World"
        );
        this.menu = new PaperWaystoneMenu(
                waystones,
                travel,
                messages,
                settings.anchorMaterial()
        );
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(menu, plugin);
        rebuildAnchorIndex();
        cleanAlreadyLoadedAnchors();
        plugin.registerCommand(
                "livingworld",
                "Comandos do Living World",
                List.of("lw"),
                new PaperWaystoneCommand(
                        waystones,
                        travel,
                        messages,
                        statusProvider,
                        settings.renameMaxDistance(),
                        menu
                )
        );
    }

    @Override
    public void disable() {
        if (settings.enabled()) {
            HandlerList.unregisterAll(this);
            HandlerList.unregisterAll(menu);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnchorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != settings.anchorMaterial()) {
            return;
        }

        Waystone waystone = waystones.findAt(
                block.getWorld().getUID(),
                block.getX(),
                block.getY(),
                block.getZ()
        ).orElse(null);
        boolean created = false;
        if (waystone == null) {
            waystone = new Waystone(
                    WaystoneId.random(),
                    defaultName(block),
                    block.getWorld().getUID(),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
            waystones.register(waystone);
            anchorIndex.add(waystone);
            created = true;
        }

        boolean activated = waystones.activate(
                event.getPlayer().getUniqueId(),
                waystone.id()
        );
        if (created) {
            event.getPlayer().sendMessage(messages.component(
                    NamedTextColor.GREEN,
                    "waystone.created-and-activated",
                    waystone.name()
            ));
        } else if (activated) {
            event.getPlayer().sendMessage(messages.component(
                    NamedTextColor.GREEN,
                    "waystone.activated",
                    waystone.name()
            ));
        } else {
            event.getPlayer().sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.already-activated",
                    waystone.name()
            ));
        }

        if (activated) {
            discovery.discover(new DiscoveryRequest(
                    WaystoneDiscovery.TYPE,
                    WaystoneDiscovery.idFor(waystone.id()),
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getUniqueId(),
                    waystone.name()
            ));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnchorBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != settings.anchorMaterial()) {
            return;
        }

        Waystone waystone = waystones.findAt(
                block.getWorld().getUID(),
                block.getX(),
                block.getY(),
                block.getZ()
        ).orElse(null);
        if (waystone == null) {
            return;
        }

        if (waystones.remove(waystone.id())) {
            anchorIndex.remove(waystone.id());
            event.getPlayer().sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.destroyed",
                    waystone.name()
            ));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        cleanChunk(event.getChunk());
    }

    private void rebuildAnchorIndex() {
        anchorIndex.rebuild(waystones.allWaystones());
    }

    private void cleanAlreadyLoadedAnchors() {
        int removed = 0;
        for (Waystone waystone : waystones.allWaystones()) {
            World world = plugin.getServer().getWorld(waystone.worldId());
            if (world == null) {
                continue;
            }

            int chunkX = WaystoneAnchorIndex.chunkCoordinate(waystone.x());
            int chunkZ = WaystoneAnchorIndex.chunkCoordinate(waystone.z());
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }

            if (removeIfAnchorMissing(waystone, world)) {
                removed++;
            }
        }

        if (removed > 0) {
            int removedCount = removed;
            plugin.getLogger().info(() -> messages.text(
                    "waystone.legacy-cleanup-summary",
                    removedCount
            ));
        }
    }

    private void cleanChunk(Chunk chunk) {
        for (Waystone waystone : anchorIndex.inChunk(
                chunk.getWorld().getUID(),
                chunk.getX(),
                chunk.getZ()
        )) {
            if (removeIfAnchorMissing(waystone, chunk.getWorld())) {
                plugin.getLogger().info(messages.text(
                        "waystone.legacy-orphan-removed",
                        waystone.name()
                ));
            }
        }
    }

    private boolean removeIfAnchorMissing(Waystone waystone, World world) {
        Block anchor = world.getBlockAt(waystone.x(), waystone.y(), waystone.z());
        if (anchor.getType() == settings.anchorMaterial()) {
            return false;
        }

        boolean removed = waystones.remove(waystone.id());
        if (removed) {
            anchorIndex.remove(waystone.id());
        }
        return removed;
    }

    private String defaultName(Block block) {
        return block.getWorld().getName()
                + " (" + block.getX() + ", " + block.getZ() + ")";
    }
}
