package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperGroundCoverSpreadModuleTest {
    @Test
    void cancelaGrassSpreadEmClimaExtremoQuandoChanceFalha() {
        World world = world(-0.5D, 0.0D);
        PaperGroundCoverSpreadModule module = module(world, 0.95D);
        BlockSpreadEvent event = event(
                world,
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.GRASS_BLOCK
        );

        module.onGroundCoverSpread(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void permiteGrassSpreadEmClimaIdeal() {
        World world = world(0.8D, 0.5D);
        PaperGroundCoverSpreadModule module = module(world, 0.99D);
        BlockSpreadEvent event = event(
                world,
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.GRASS_BLOCK
        );

        module.onGroundCoverSpread(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void ignoraOutrosTiposDeSpread() {
        World world = world(-0.5D, 0.0D);
        PaperGroundCoverSpreadModule module = module(world, 0.99D);
        BlockSpreadEvent event = event(
                world,
                Material.FIRE,
                Material.AIR,
                Material.FIRE
        );

        module.onGroundCoverSpread(event);

        assertFalse(event.isCancelled());
    }

    private PaperGroundCoverSpreadModule module(
            World world,
            double randomValue
    ) {
        return new PaperGroundCoverSpreadModule(
                plugin(),
                world,
                new PaperEcologySettings(
                        true,
                        0.65D,
                        0.35D,
                        true,
                        1.0D,
                        true
                ),
                new PaperLocalClimateResolver(
                        world,
                        calendar(),
                        new ClimateProfileClassifier(
                                ClimateProfileThresholds.livingWorldDefaults()
                        ),
                        new ClimatePolicy()
                ),
                new NaturalGrowthSuitabilityPolicy(),
                () -> randomValue
        );
    }

    private CalendarView calendar() {
        return new CalendarView() {
            @Override
            public CalendarDate currentDate() {
                return new CalendarDate(1, 1, 1);
            }

            @Override
            public Season currentSeason() {
                return Season.OUTONO;
            }

            @Override
            public int daysPerMonth() {
                return 8;
            }
        };
    }

    private BlockSpreadEvent event(
            World world,
            Material sourceType,
            Material currentType,
            Material newType
    ) {
        Block destination = block(world, currentType);
        Block source = block(world, sourceType);
        return new BlockSpreadEvent(
                destination,
                source,
                state(newType)
        );
    }

    private Block block(World world, Material type) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "getType" -> type;
                    case "toString" -> "BlockFake(" + type + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private BlockState state(Material type) {
        return (BlockState) Proxy.newProxyInstance(
                BlockState.class.getClassLoader(),
                new Class<?>[]{BlockState.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> type;
                    case "toString" -> "BlockStateFake(" + type + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private World world(double temperature, double humidity) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTemperature" -> temperature;
                    case "getHumidity" -> humidity;
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Plugin plugin() {
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> "PluginFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
