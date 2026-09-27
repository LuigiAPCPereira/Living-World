package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.TreeType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperNaturalGrowthModuleTest {
    @Test
    void cancelaCrescimentoAgeableQuandoClimaExtremoFalhaNaChance() {
        World world = world(-0.5D, 0.0D);
        PaperNaturalGrowthModule module = module(world, 0.90D);
        BlockGrowEvent event = event(world, 1, 2);

        module.onNaturalGrowth(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void mantemCrescimentoAgeableEmClimaIdeal() {
        World world = world(0.8D, 0.5D);
        PaperNaturalGrowthModule module = module(world, 0.99D);
        BlockGrowEvent event = event(world, 1, 2);

        module.onNaturalGrowth(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void ignoraEventoQueNaoAumentaIdade() {
        World world = world(-0.5D, 0.0D);
        PaperNaturalGrowthModule module = module(world, 0.99D);
        BlockGrowEvent event = event(world, 2, 2);

        module.onNaturalGrowth(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void cancelaArvoreNaturalDeSaplingQuandoClimaExtremoFalhaNaChance() {
        World world = world(-0.5D, 0.0D);
        PaperNaturalGrowthModule module = module(world, 0.90D);
        StructureGrowEvent event = treeEvent(world, false, true);

        module.onNaturalTreeGrowth(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void boneMealEmSaplingNaoEhModuladoPelaEcologia() {
        World world = world(-0.5D, 0.0D);
        PaperNaturalGrowthModule module = module(world, 0.99D);
        StructureGrowEvent event = treeEvent(world, true, true);

        module.onNaturalTreeGrowth(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void estruturaOrganicaQueNaoParteDeSaplingEhIgnorada() {
        World world = world(-0.5D, 0.0D);
        PaperNaturalGrowthModule module = module(world, 0.99D);
        StructureGrowEvent event = treeEvent(world, false, false);

        module.onNaturalTreeGrowth(event);

        assertFalse(event.isCancelled());
    }

    private PaperNaturalGrowthModule module(World world, double randomValue) {
        return new PaperNaturalGrowthModule(
                plugin(),
                world,
                new PaperEcologySettings(true, 1.0D, 1.0D, true),
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

    private BlockGrowEvent event(World world, int currentAge, int nextAge) {
        Block block = (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "getBlockData" -> ageable(currentAge);
                    case "toString" -> "BlockFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );

        BlockState state = (BlockState) Proxy.newProxyInstance(
                BlockState.class.getClassLoader(),
                new Class<?>[]{BlockState.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBlockData" -> ageable(nextAge);
                    case "toString" -> "BlockStateFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );

        return new BlockGrowEvent(block, state);
    }

    private StructureGrowEvent treeEvent(
            World world,
            boolean bonemeal,
            boolean sapling
    ) {
        Block block = (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "getBlockData" -> sapling ? sapling() : ageable(1);
                    case "toString" -> "TreeOriginFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
        Location location = new Location(world, 10, 64, -20) {
            @Override
            public Block getBlock() {
                return block;
            }
        };
        return new StructureGrowEvent(
                location,
                TreeType.TREE,
                bonemeal,
                null,
                java.util.List.of()
        );
    }

    private Ageable ageable(int age) {
        return (Ageable) Proxy.newProxyInstance(
                Ageable.class.getClassLoader(),
                new Class<?>[]{Ageable.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAge" -> age;
                    case "getMaximumAge" -> 7;
                    case "toString" -> "AgeableFake(" + age + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Sapling sapling() {
        return (Sapling) Proxy.newProxyInstance(
                Sapling.class.getClassLoader(),
                new Class<?>[]{Sapling.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getStage" -> 1;
                    case "getMaximumStage" -> 1;
                    case "toString" -> "SaplingFake";
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
}
