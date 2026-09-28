package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceTarget;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperWinterSurfaceTargetResolverTest {
    private final PaperWinterSurfaceTargetResolver resolver =
            new PaperWinterSurfaceTargetResolver();

    @Test
    void reconheceSomenteWaterSourceExposta() {
        var ledger = WinterSurfaceOwnershipLedger.empty(8);

        assertEquals(
                WinterSurfaceTarget.EXPOSED_SOURCE_WATER,
                resolver.resolve(
                        block(Material.WATER, 0, 15, true),
                        ledger
                )
        );
        assertEquals(
                WinterSurfaceTarget.OTHER,
                resolver.resolve(
                        block(Material.WATER, 1, 15, true),
                        ledger
                )
        );
        assertEquals(
                WinterSurfaceTarget.OTHER,
                resolver.resolve(
                        block(Material.WATER, 0, 14, true),
                        ledger
                )
        );
        assertEquals(
                WinterSurfaceTarget.OTHER,
                resolver.resolve(
                        block(Material.WATER, 0, 15, false),
                        ledger
                )
        );
    }

    @Test
    void iceSoEhOwnedQuandoLedgerConfirma() {
        var ledger = WinterSurfaceOwnershipLedger.empty(8);
        WinterSurfacePosition position =
                WinterSurfacePosition.fromWorld(10, 64, -5);

        assertEquals(
                WinterSurfaceTarget.OTHER,
                resolver.resolve(block(Material.ICE, 0, 15, true), ledger)
        );
        ledger.claim(position, WinterSurfaceKind.ICE);
        assertEquals(
                WinterSurfaceTarget.OWNED_ICE,
                resolver.resolve(block(Material.ICE, 0, 15, true), ledger)
        );
    }

    private Block block(
            Material material,
            int level,
            int skyLight,
            boolean aboveEmpty
    ) {
        Block above = (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEmpty" -> aboveEmpty;
                    default -> throw new AssertionError(
                            "Chamada inesperada no bloco acima: "
                                    + method.getName()
                    );
                }
        );
        Levelled levelled = (Levelled) Proxy.newProxyInstance(
                Levelled.class.getClassLoader(),
                new Class<?>[]{Levelled.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLevel" -> level;
                    default -> throw new AssertionError(
                            "Chamada inesperada no Levelled: "
                                    + method.getName()
                    );
                }
        );
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> material;
                    case "getBlockData" -> material == Material.WATER
                            ? levelled
                            : null;
                    case "getLightFromSky" -> (byte) skyLight;
                    case "getRelative" -> above;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -5;
                    default -> throw new AssertionError(
                            "Chamada inesperada no bloco: " + method.getName()
                    );
                }
        );
    }
}
