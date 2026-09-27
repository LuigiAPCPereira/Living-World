package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperSafeWaystoneDestinationTest {
    private final PaperSafeWaystoneDestination resolver = new PaperSafeWaystoneDestination();

    @Test
    void aceitaAncoraSolidaComPesECabecaLivres() {
        UUID worldId = UUID.randomUUID();
        Map<Integer, Block> blocks = new HashMap<>();
        blocks.put(64, block(false, false));
        blocks.put(65, block(true, false));
        blocks.put(66, block(true, false));
        World world = world(worldId, blocks, true);
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Casa",
                worldId,
                10,
                64,
                10
        );

        Location destination = resolver.resolveLoaded(world, waystone).orElseThrow();

        assertEquals(10.5D, destination.getX());
        assertEquals(65D, destination.getY());
        assertEquals(10.5D, destination.getZ());
    }

    @Test
    void rejeitaDestinoBloqueadoLiquidoOuForaDaBorda() {
        UUID worldId = UUID.randomUUID();
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Casa",
                worldId,
                10,
                64,
                10
        );

        Map<Integer, Block> blocked = new HashMap<>();
        blocked.put(64, block(false, false));
        blocked.put(65, block(false, false));
        blocked.put(66, block(true, false));
        assertTrue(resolver.resolveLoaded(world(worldId, blocked, true), waystone).isEmpty());

        Map<Integer, Block> liquid = new HashMap<>();
        liquid.put(64, block(false, false));
        liquid.put(65, block(true, true));
        liquid.put(66, block(true, false));
        assertTrue(resolver.resolveLoaded(world(worldId, liquid, true), waystone).isEmpty());

        Map<Integer, Block> safe = new HashMap<>();
        safe.put(64, block(false, false));
        safe.put(65, block(true, false));
        safe.put(66, block(true, false));
        assertTrue(resolver.resolveLoaded(world(worldId, safe, false), waystone).isEmpty());
    }

    private World world(UUID id, Map<Integer, Block> blocks, boolean insideBorder) {
        WorldBorder border = (WorldBorder) Proxy.newProxyInstance(
                WorldBorder.class.getClassLoader(),
                new Class<?>[]{WorldBorder.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isInside" -> insideBorder;
                    case "toString" -> "WorldBorderFake";
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
                    case "getUID" -> id;
                    case "getMinHeight" -> -64;
                    case "getMaxHeight" -> 320;
                    case "getWorldBorder" -> border;
                    case "getBlockAt" -> blocks.get((Integer) args[1]);
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Block block(boolean passable, boolean liquid) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isPassable" -> passable;
                    case "isLiquid" -> liquid;
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
