package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimension;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.PhysicalSnowSettings;
import dev.signalshards.livingworld.features.ecology.domain.WinterSnowMutationPolicy;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import dev.signalshards.livingworld.features.ecology.domain.WinterThermalPhase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWinterSnowMutatorTest {
    @Test
    void freezingComPrecipitacaoColocaSnowEGanhaOwnership() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        AtomicReference<Material> aboveType =
                new AtomicReference<>(Material.AIR);
        Block above = mutableBlock(aboveType, 4, 65, 7);
        Block support = supportBlock(above);
        PaperWinterSnowMutator mutator = new PaperWinterSnowMutator(
                new WinterSnowMutationPolicy(
                        PhysicalSnowSettings.defaults()
                ),
                new PaperWinterSnowTargetResolver(snowData(1))
        );

        boolean changed = mutator.mutate(
                support,
                ownership,
                WinterThermalPhase.FREEZING,
                new EnvironmentalDimensionPolicy().profileFor(
                        EnvironmentalDimension.OVERWORLD
                ),
                true
        );

        assertTrue(changed);
        assertEquals(Material.SNOW, aboveType.get());
        assertEquals(
                WinterSurfaceKind.SNOW,
                ownership.kindAt(
                        WinterSurfacePosition.fromWorld(4, 65, 7)
                ).orElseThrow()
        );
    }

    @Test
    void freezingComPrecipitacaoAcumulaLayerEmSnowOwned() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        WinterSurfacePosition position =
                WinterSurfacePosition.fromWorld(4, 65, 7);
        ownership.claim(position, WinterSurfaceKind.SNOW);
        AtomicInteger layers = new AtomicInteger(2);
        Snow snowData = mutableSnowData(layers);
        Block snow = ownedSnowBlock(snowData, 4, 65, 7);
        PaperWinterSnowMutator mutator = new PaperWinterSnowMutator(
                new WinterSnowMutationPolicy(
                        PhysicalSnowSettings.defaults()
                ),
                new PaperWinterSnowTargetResolver(snowData(1))
        );

        boolean changed = mutator.mutate(
                snow,
                ownership,
                WinterThermalPhase.FREEZING,
                new EnvironmentalDimensionPolicy().profileFor(
                        EnvironmentalDimension.OVERWORLD
                ),
                true
        );

        assertTrue(changed);
        assertEquals(3, layers.get());
        assertEquals(
                WinterSurfaceKind.SNOW,
                ownership.kindAt(position).orElseThrow()
        );
    }

    @Test
    void thawDerreteUmaLayerPorMutacaoERemoveOwnershipNoFinal() {
        WinterSurfaceOwnershipLedger ownership =
                WinterSurfaceOwnershipLedger.empty(8);
        WinterSurfacePosition position =
                WinterSurfacePosition.fromWorld(4, 65, 7);
        ownership.claim(position, WinterSurfaceKind.SNOW);
        AtomicInteger layers = new AtomicInteger(2);
        AtomicReference<Material> type =
                new AtomicReference<>(Material.SNOW);
        Snow snowData = mutableSnowData(layers);
        Block snow = mutableOwnedSnowBlock(
                snowData,
                type,
                4,
                65,
                7
        );
        PaperWinterSnowMutator mutator = new PaperWinterSnowMutator(
                new WinterSnowMutationPolicy(
                        PhysicalSnowSettings.defaults()
                ),
                new PaperWinterSnowTargetResolver(snowData(1))
        );
        var overworld = new EnvironmentalDimensionPolicy().profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertTrue(mutator.mutate(
                snow,
                ownership,
                WinterThermalPhase.THAWING,
                overworld,
                false
        ));
        assertEquals(1, layers.get());
        assertEquals(Material.SNOW, type.get());
        assertTrue(ownership.kindAt(position).isPresent());

        assertTrue(mutator.mutate(
                snow,
                ownership,
                WinterThermalPhase.THAWING,
                overworld,
                false
        ));
        assertEquals(Material.AIR, type.get());
        assertTrue(ownership.kindAt(position).isEmpty());
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

    private Snow mutableSnowData(AtomicInteger layers) {
        return (Snow) Proxy.newProxyInstance(
                Snow.class.getClassLoader(),
                new Class<?>[]{Snow.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLayers" -> layers.get();
                    case "setLayers" -> {
                        layers.set((Integer) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }

    private Block ownedSnowBlock(
            Snow snow,
            int x,
            int y,
            int z
    ) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> Material.SNOW;
                    case "getBlockData" -> snow;
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "setBlockData" -> null;
                    default -> null;
                }
        );
    }

    private Block mutableOwnedSnowBlock(
            Snow snow,
            AtomicReference<Material> type,
            int x,
            int y,
            int z
    ) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> type.get();
                    case "getBlockData" -> snow;
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "setBlockData" -> null;
                    case "setType" -> {
                        type.set((Material) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }

    private Block supportBlock(Block above) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> Material.STONE;
                    case "getRelative" -> above;
                    default -> null;
                }
        );
    }

    private Block mutableBlock(
            AtomicReference<Material> type,
            int x,
            int y,
            int z
    ) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEmpty" -> type.get() == Material.AIR;
                    case "canPlace" -> true;
                    case "getType" -> type.get();
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "setType" -> {
                        type.set((Material) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }
}
