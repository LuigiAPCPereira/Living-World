package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceMutationPolicy;
import dev.signalshards.livingworld.features.ecology.domain.WinterThermalPhasePolicy;
import dev.signalshards.livingworld.features.ecology.domain.PhysicalWinterSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPhysicalWinterModuleTest {
    @Test
    void disabledNaoRegistraNemAgenda() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                block -> new AmbientTemperature(-5.0D),
                new PaperWinterSurfaceOwnershipStore(
                        new NamespacedKey(
                                "livingworld",
                                "physical_winter_ownership"
                        ),
                        settings.domain()
                ),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        module.enable();

        assertEquals(0, registrations.get());
        assertEquals(0, schedules.get());
    }

    @Test
    void enabledRegistraEAgendaUmaTask() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        true,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(-5.0D),
                new PaperWinterSurfaceOwnershipStore(
                        new NamespacedKey(
                                "livingworld",
                                "physical_winter_ownership"
                        ),
                        settings.domain()
                ),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        module.enable();

        assertEquals(1, registrations.get());
        assertEquals(1, schedules.get());
    }

    @Test
    void sourceWaterExpostaCongelaEGanhaOwnership() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        NamespacedKey key = new NamespacedKey(
                "livingworld",
                "physical_winter_ownership"
        );
        Map<NamespacedKey, Object> data = new HashMap<>();
        Chunk chunk = chunk(pdc(data));
        AtomicReference<Material> type =
                new AtomicReference<>(Material.WATER);
        Block block = sourceWaterBlock(chunk, type);
        PaperWinterSurfaceOwnershipStore store =
                new PaperWinterSurfaceOwnershipStore(key, settings.domain());
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(-5.0D),
                store,
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        assertTrue(module.processCandidate(block));
        assertEquals(Material.ICE, type.get());
        assertTrue(store.load(chunk).kindAt(
                new dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition(
                        1,
                        64,
                        2
                )
        ).isPresent());
    }

    @Test
    void ownedIceDescongelaERemoveOwnership() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        NamespacedKey key = new NamespacedKey(
                "livingworld",
                "physical_winter_ownership"
        );
        Map<NamespacedKey, Object> data = new HashMap<>();
        Chunk chunk = chunk(pdc(data));
        PaperWinterSurfaceOwnershipStore store =
                new PaperWinterSurfaceOwnershipStore(key, settings.domain());
        var position =
                new dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition(
                        1,
                        64,
                        2
                );
        var ledger = store.load(chunk);
        ledger.claim(
                position,
                dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind.ICE
        );
        store.save(chunk, ledger);
        AtomicReference<Material> type =
                new AtomicReference<>(Material.ICE);
        Block block = sourceWaterBlock(chunk, type);
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(5.0D),
                store,
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        assertTrue(module.processCandidate(block));
        assertEquals(Material.WATER, type.get());
        assertTrue(store.load(chunk).kindAt(position).isEmpty());
    }

    @Test
    void mutationBudgetInterrompeProbesDepoisDaPrimeiraMutacao() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        AtomicInteger highestBlockCalls = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        NamespacedKey key = new NamespacedKey(
                "livingworld",
                "physical_winter_ownership"
        );
        Map<NamespacedKey, Object> data = new HashMap<>();
        Chunk chunk = chunk(pdc(data));
        AtomicReference<Material> type =
                new AtomicReference<>(Material.WATER);
        Block surface = sourceWaterBlock(chunk, type);
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isChunkLoaded" -> true;
                    case "getHighestBlockAt" -> {
                        highestBlockCalls.incrementAndGet();
                        yield surface;
                    }
                    default -> null;
                }
        );
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getLocation" -> new Location(world, 0, 64, 0);
                    case "getUniqueId" -> java.util.UUID.fromString(
                            "4c41242d-19b5-44d4-b4c0-468099f2235f"
                    );
                    default -> null;
                }
        );
        PaperWinterSurfaceOwnershipStore store =
                new PaperWinterSurfaceOwnershipStore(key, settings.domain());
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(-5.0D),
                store,
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        module.processPlayer(player);

        assertEquals(1, highestBlockCalls.get());
        assertEquals(Material.ICE, type.get());
    }

    @Test
    void chunkNaoCarregadoNuncaEhSondado() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        AtomicInteger highestBlockCalls = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isChunkLoaded" -> false;
                    case "getHighestBlockAt" -> {
                        highestBlockCalls.incrementAndGet();
                        yield null;
                    }
                    default -> null;
                }
        );
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getLocation" -> new Location(world, 0, 64, 0);
                    case "getUniqueId" -> java.util.UUID.fromString(
                            "494f73ae-38ee-4f60-9d19-5be5378a70e4"
                    );
                    default -> null;
                }
        );
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(-5.0D),
                new PaperWinterSurfaceOwnershipStore(
                        new NamespacedKey(
                                "livingworld",
                                "physical_winter_ownership"
                        ),
                        settings.domain()
                ),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        module.processPlayer(player);

        assertEquals(0, highestBlockCalls.get());
    }

    @Test
    void probeBudgetLimitaColunasSemMutacao() {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger schedules = new AtomicInteger();
        AtomicInteger highestBlockCalls = new AtomicInteger();
        Plugin plugin = plugin(registrations, schedules);
        PaperPhysicalWinterSettings settings =
                new PaperPhysicalWinterSettings(
                        false,
                        40L,
                        4,
                        1,
                        8,
                        PhysicalWinterSettings.defaults()
                );
        NamespacedKey key = new NamespacedKey(
                "livingworld",
                "physical_winter_ownership"
        );
        Map<NamespacedKey, Object> data = new HashMap<>();
        Chunk chunk = chunk(pdc(data));
        AtomicReference<Material> type =
                new AtomicReference<>(Material.STONE);
        Block surface = sourceWaterBlock(chunk, type);
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isChunkLoaded" -> true;
                    case "getHighestBlockAt" -> {
                        highestBlockCalls.incrementAndGet();
                        yield surface;
                    }
                    default -> null;
                }
        );
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getLocation" -> new Location(world, 0, 64, 0);
                    case "getUniqueId" -> java.util.UUID.fromString(
                            "846ae166-5061-4cb8-8f06-88b62531a728"
                    );
                    default -> null;
                }
        );
        PaperPhysicalWinterModule module = new PaperPhysicalWinterModule(
                plugin,
                settings,
                ignored -> new AmbientTemperature(-5.0D),
                new PaperWinterSurfaceOwnershipStore(key, settings.domain()),
                new WinterThermalPhasePolicy(settings.domain()),
                new WinterSurfaceMutationPolicy(),
                new PaperWinterSurfaceTargetResolver(),
                new EnvironmentalDimensionPolicy()
        );

        module.processPlayer(player);

        assertEquals(4, highestBlockCalls.get());
        assertEquals(Material.STONE, type.get());
    }

    private Block sourceWaterBlock(
            Chunk chunk,
            AtomicReference<Material> type
    ) {
        Levelled levelled = (Levelled) Proxy.newProxyInstance(
                Levelled.class.getClassLoader(),
                new Class<?>[]{Levelled.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLevel" -> 0;
                    default -> null;
                }
        );
        Block above = (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEmpty" -> true;
                    default -> null;
                }
        );
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getEnvironment" -> World.Environment.NORMAL;
                    default -> null;
                }
        );
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> type.get();
                    case "getBlockData" -> levelled;
                    case "getLightFromSky" -> (byte) 15;
                    case "getRelative" -> args[0] == BlockFace.UP
                            ? above
                            : null;
                    case "getX" -> 1;
                    case "getY" -> 64;
                    case "getZ" -> 2;
                    case "getChunk" -> chunk;
                    case "getWorld" -> world;
                    case "setType" -> {
                        type.set((Material) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }

    private Chunk chunk(PersistentDataContainer pdc) {
        return (Chunk) Proxy.newProxyInstance(
                Chunk.class.getClassLoader(),
                new Class<?>[]{Chunk.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPersistentDataContainer" -> pdc;
                    default -> null;
                }
        );
    }

    private PersistentDataContainer pdc(Map<NamespacedKey, Object> data) {
        return (PersistentDataContainer) Proxy.newProxyInstance(
                PersistentDataContainer.class.getClassLoader(),
                new Class<?>[]{PersistentDataContainer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "set" -> {
                        data.put((NamespacedKey) args[0], args[2]);
                        yield null;
                    }
                    case "getOrDefault" -> data.getOrDefault(
                            (NamespacedKey) args[0],
                            args[2]
                    );
                    default -> null;
                }
        );
    }

    private Plugin plugin(
            AtomicInteger registrations,
            AtomicInteger schedules
    ) {
        PluginManager manager = (PluginManager) Proxy.newProxyInstance(
                PluginManager.class.getClassLoader(),
                new Class<?>[]{PluginManager.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("registerEvents")) {
                        registrations.incrementAndGet();
                        return null;
                    }
                    return null;
                }
        );
        BukkitScheduler scheduler = (BukkitScheduler) Proxy.newProxyInstance(
                BukkitScheduler.class.getClassLoader(),
                new Class<?>[]{BukkitScheduler.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("runTaskTimer")) {
                        schedules.incrementAndGet();
                    }
                    return null;
                }
        );
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPluginManager" -> manager;
                    case "getScheduler" -> scheduler;
                    default -> null;
                }
        );
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServer" -> server;
                    case "getLogger" -> Logger.getLogger(
                            "PaperPhysicalWinterModuleTest"
                    );
                    case "getName" -> "LivingWorld";
                    default -> null;
                }
        );
    }
}
