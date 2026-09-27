package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.application.WaystoneAccessStore;
import dev.signalshards.livingworld.features.waystones.application.WaystoneRegistry;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.application.WaystoneTravelResult;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWaystoneTravelServiceTest {
    @Test
    void carregaChunkAntesDeValidarETeleportaAssincronamente() {
        UUID playerId = UUID.randomUUID();
        UUID worldId = UUID.randomUUID();
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Praça",
                worldId,
                10,
                64,
                10
        );
        AtomicBoolean chunkRequested = new AtomicBoolean();
        AtomicReference<Location> teleportedTo = new AtomicReference<>();
        World world = safeWorld(worldId, chunkRequested);
        Player player = player(playerId, teleportedTo);
        WaystoneService service = serviceWithAccess(playerId, waystone);
        PaperWaystoneTravelService travel = new PaperWaystoneTravelService(
                server(worldId, world),
                service,
                new PaperSafeWaystoneDestination()
        );

        WaystoneTravelResult result = travel.travel(player, waystone.id()).join();

        assertEquals(WaystoneTravelResult.SUCCESS, result);
        assertTrue(chunkRequested.get());
        assertEquals(10.5D, teleportedTo.get().getX());
        assertEquals(65D, teleportedTo.get().getY());
    }

    @Test
    void naoCarregaChunkQuandoJogadorNaoAtivouDestino() {
        UUID playerId = UUID.randomUUID();
        UUID worldId = UUID.randomUUID();
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Praça",
                worldId,
                10,
                64,
                10
        );
        AtomicBoolean chunkRequested = new AtomicBoolean();
        World world = safeWorld(worldId, chunkRequested);
        WaystoneRegistry registry = registry(waystone);
        WaystoneAccessStore access = new WaystoneAccessStore() {
            @Override
            public Set<WaystoneId> load(UUID id) {
                return Set.of();
            }

            @Override
            public boolean add(UUID id, WaystoneId waystoneId) {
                return false;
            }
        };
        PaperWaystoneTravelService travel = new PaperWaystoneTravelService(
                server(worldId, world),
                new WaystoneService(registry, access),
                new PaperSafeWaystoneDestination()
        );

        WaystoneTravelResult result = travel.travel(
                player(playerId, new AtomicReference<>()),
                waystone.id()
        ).join();

        assertEquals(WaystoneTravelResult.NOT_ACTIVATED, result);
        assertFalse(chunkRequested.get());
    }

    private WaystoneService serviceWithAccess(UUID playerId, Waystone waystone) {
        WaystoneAccessStore access = new WaystoneAccessStore() {
            @Override
            public Set<WaystoneId> load(UUID id) {
                return playerId.equals(id) ? Set.of(waystone.id()) : Set.of();
            }

            @Override
            public boolean add(UUID id, WaystoneId waystoneId) {
                return false;
            }
        };
        return new WaystoneService(registry(waystone), access);
    }

    private WaystoneRegistry registry(Waystone waystone) {
        return new WaystoneRegistry() {
            @Override
            public void register(Waystone value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<Waystone> find(WaystoneId id) {
                return waystone.id().equals(id) ? Optional.of(waystone) : Optional.empty();
            }

            @Override
            public List<Waystone> all() {
                return List.of(waystone);
            }

            @Override
            public boolean remove(WaystoneId id) {
                return false;
            }
        };
    }

    private Server server(UUID worldId, World world) {
        return (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> worldId.equals(args[0]) ? world : null;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private World safeWorld(UUID worldId, AtomicBoolean chunkRequested) {
        Block support = block(false);
        Block air = block(true);
        WorldBorder border = (WorldBorder) Proxy.newProxyInstance(
                WorldBorder.class.getClassLoader(),
                new Class<?>[]{WorldBorder.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isInside" -> true;
                    case "toString" -> "BorderFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
        Chunk chunk = (Chunk) Proxy.newProxyInstance(
                Chunk.class.getClassLoader(),
                new Class<?>[]{Chunk.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> "ChunkFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );

        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> worldId;
                    case "getMinHeight" -> -64;
                    case "getMaxHeight" -> 320;
                    case "getWorldBorder" -> border;
                    case "getChunkAtAsync" -> {
                        chunkRequested.set(true);
                        yield CompletableFuture.completedFuture(chunk);
                    }
                    case "getBlockAt" -> ((Integer) args[1]) == 64 ? support : air;
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Player player(UUID playerId, AtomicReference<Location> teleportedTo) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isOnline" -> true;
                    case "getUniqueId" -> playerId;
                    case "teleportAsync" -> {
                        teleportedTo.set((Location) args[0]);
                        yield CompletableFuture.completedFuture(true);
                    }
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Block block(boolean passable) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isPassable" -> passable;
                    case "isLiquid" -> false;
                    case "toString" -> "BlockFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
