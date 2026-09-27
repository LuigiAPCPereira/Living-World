package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FarmlandMoistureRetentionPolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperFarmlandMoistureModuleTest {
    @Test
    void podeCancelarSecagemEmClimaFrioEUmido() {
        World world = world(0.40D, 0.80D);
        PaperFarmlandMoistureModule module = module(world, 0.10D);
        MoistureChangeEvent event = event(world, 7, 6);

        module.onMoistureChange(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void naoRetemSecagemEmClimaQuenteESeco() {
        World world = world(1.20D, 0.20D);
        PaperFarmlandMoistureModule module = module(world, 0.0D);
        MoistureChangeEvent event = event(world, 7, 6);

        module.onMoistureChange(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void aumentoDeUmidadeContinuaVanilla() {
        World world = world(0.40D, 0.80D);
        PaperFarmlandMoistureModule module = module(world, 0.0D);
        MoistureChangeEvent event = event(world, 3, 4);

        module.onMoistureChange(event);

        assertFalse(event.isCancelled());
    }

    private PaperFarmlandMoistureModule module(
            World world,
            double randomValue
    ) {
        return new PaperFarmlandMoistureModule(
                plugin(),
                world,
                new PaperEcologySettings(
                        true,
                        0.65D,
                        0.35D,
                        true,
                        0.50D,
                        true,
                        1.0D,
                        true,
                        0.65D,
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
                new FarmlandMoistureRetentionPolicy(),
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

    private MoistureChangeEvent event(
            World world,
            int currentMoisture,
            int nextMoisture
    ) {
        Block block = (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "getBlockData" -> farmland(currentMoisture);
                    case "toString" -> "FarmlandBlockFake";
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
                    case "getBlockData" -> farmland(nextMoisture);
                    case "toString" -> "FarmlandStateFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
        return new MoistureChangeEvent(block, state);
    }

    private Farmland farmland(int moisture) {
        return (Farmland) Proxy.newProxyInstance(
                Farmland.class.getClassLoader(),
                new Class<?>[]{Farmland.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMoisture" -> moisture;
                    case "getMaximumMoisture" -> 7;
                    case "toString" -> "FarmlandFake(" + moisture + ")";
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
