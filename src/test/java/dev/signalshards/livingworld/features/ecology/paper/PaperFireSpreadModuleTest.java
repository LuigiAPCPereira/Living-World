package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FireSpreadSuitabilityPolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperFireSpreadModuleTest {
    @Test
    void podeCancelarApenasSpreadEmClimaFrioEUmido() {
        World world = world(0.40D, 0.80D);
        PaperFireSpreadModule module = module(world, 0.90D);
        BlockIgniteEvent event = event(
                world,
                BlockIgniteEvent.IgniteCause.SPREAD
        );

        module.onIgnite(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void quenteESecoMantemSpreadVanilla() {
        World world = world(1.20D, 0.20D);
        PaperFireSpreadModule module = module(world, 0.99D);
        BlockIgniteEvent event = event(
                world,
                BlockIgniteEvent.IgniteCause.SPREAD
        );

        module.onIgnite(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void pederneiraNaoEhModulada() {
        World world = world(0.40D, 0.80D);
        PaperFireSpreadModule module = module(world, 0.99D);
        BlockIgniteEvent event = event(
                world,
                BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
        );

        module.onIgnite(event);

        assertFalse(event.isCancelled());
    }

    private PaperFireSpreadModule module(World world, double randomValue) {
        return new PaperFireSpreadModule(
                plugin(),
                world,
                new PaperEcologySettings(
                        true,
                        0.65D,
                        0.35D,
                        true,
                        0.50D,
                        true,
                        0.70D,
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
                new FireSpreadSuitabilityPolicy(),
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

    private BlockIgniteEvent event(
            World world,
            BlockIgniteEvent.IgniteCause cause
    ) {
        return new BlockIgniteEvent(
                block(world),
                cause,
                (Entity) null
        );
    }

    private Block block(World world) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "toString" -> "BlockFake";
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
