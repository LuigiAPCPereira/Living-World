package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.WinterSnowTarget;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PaperWinterSnowTargetResolverTest {
    private final Snow placementData = snowData(1);
    private final PaperWinterSnowTargetResolver resolver =
            new PaperWinterSnowTargetResolver(placementData);

    @Test
    void reconheceSnowOwnedEPreservaLayers() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        WinterSurfacePosition position =
                WinterSurfacePosition.fromWorld(3, 65, -2);
        ownership.claim(position, WinterSurfaceKind.SNOW);
        Block snow = block(
                Material.SNOW,
                snowData(3),
                3,
                65,
                -2,
                null
        );

        var observation = resolver.resolve(snow, ownership);

        assertEquals(WinterSnowTarget.OWNED_SNOW, observation.target());
        assertEquals(3, observation.currentLayers());
        assertSame(snow, observation.mutationBlock());
    }

    @Test
    void snowSemOwnershipFalhaFechado() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        Block snow = block(
                Material.SNOW,
                snowData(2),
                3,
                65,
                -2,
                null
        );

        var observation = resolver.resolve(snow, ownership);

        assertEquals(WinterSnowTarget.OTHER, observation.target());
        assertSame(snow, observation.mutationBlock());
    }

    @Test
    void superficieValidaRetornaBlocoAcimaParaPlacement() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        Block above = above(true, true);
        Block support = block(
                Material.STONE,
                null,
                0,
                64,
                0,
                above
        );

        var observation = resolver.resolve(support, ownership);

        assertEquals(
                WinterSnowTarget.EXPOSED_SUPPORT,
                observation.target()
        );
        assertEquals(0, observation.currentLayers());
        assertSame(above, observation.mutationBlock());
    }

    private Snow snowData(int layers) {
        return (Snow) Proxy.newProxyInstance(
                Snow.class.getClassLoader(),
                new Class<?>[]{Snow.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLayers" -> layers;
                    default -> null;
                }
        );
    }

    private Block above(boolean empty, boolean canPlace) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEmpty" -> empty;
                    case "canPlace" -> canPlace;
                    default -> null;
                }
        );
    }

    private Block block(
            Material material,
            Snow snow,
            int x,
            int y,
            int z,
            Block above
    ) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> material;
                    case "getBlockData" -> snow;
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "getRelative" -> above;
                    default -> null;
                }
        );
    }
}
