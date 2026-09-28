package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ApparentTemperaturePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import dev.signalshards.livingworld.features.ecology.domain.FarmlandMoistureRetentionPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FireSpreadSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FrozenSurfacePolicy;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.paper.PaperEcologySettings;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperLocalClimateReadoutProviderTest {
    @Test
    void quenteESecoExplicaAsMesmasRegrasEcologicas() {
        World world = world(1.20D, 0.20D);
        var readout = provider(world).snapshot(player(world));

        assertEquals(23, readout.apparentCelsius());
        assertEquals(ThermalBand.QUENTE, readout.thermalBand());
        assertEquals(MoistureBand.SECO, readout.moistureBand());
        assertEquals(WeatherTendency.TEMPO_LIMPO, readout.weatherTendency());
        assertEquals(79, readout.cropGrowth().percent());
        assertEquals(89, readout.treeGrowth().percent());
        assertEquals(84, readout.grassSpread().percent());
        assertEquals(0, readout.farmlandRetention().percent());
        assertEquals(100, readout.fireSpread().percent());
        assertFalse(readout.frozenSurfacesPersist());
    }

    @Test
    void frioEUmidoMostraRetencaoEFrozenSurface() {
        World world = world(0.40D, 0.80D);
        var readout = provider(world).snapshot(player(world));

        assertEquals(7, readout.apparentCelsius());
        assertEquals(ThermalBand.FRIO, readout.thermalBand());
        assertEquals(MoistureBand.UMIDO, readout.moistureBand());
        assertEquals(WeatherTendency.PRECIPITACAO, readout.weatherTendency());
        assertEquals(81, readout.cropGrowth().percent());
        assertEquals(90, readout.treeGrowth().percent());
        assertEquals(85, readout.grassSpread().percent());
        assertEquals(39, readout.farmlandRetention().percent());
        assertEquals(58, readout.fireSpread().percent());
        assertTrue(readout.frozenSurfacesPersist());
    }

    private PaperLocalClimateReadoutProvider provider(World world) {
        CalendarView calendar = new CalendarView() {
            @Override
            public CalendarDate currentDate() {
                return new CalendarDate(1, 7, 1);
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
        return new PaperLocalClimateReadoutProvider(
                calendar,
                new PaperLocalClimateResolver(
                        world,
                        calendar,
                        new ClimateProfileClassifier(
                                ClimateProfileThresholds.livingWorldDefaults()
                        ),
                        new ClimatePolicy()
                ),
                PaperEcologySettings.defaults(),
                new ApparentTemperaturePolicy(),
                new NaturalGrowthSuitabilityPolicy(),
                new FarmlandMoistureRetentionPolicy(),
                new FireSpreadSuitabilityPolicy(),
                new FrozenSurfacePolicy()
        );
    }

    private Player player(World world) {
        Location location = new Location(world, 10.5D, 64.0D, -20.5D);
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> location;
                    case "getWorld" -> world;
                    case "isInLava", "isInWater" -> false;
                    case "getFireTicks" -> 0;
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private World world(double temperature, double humidity) {
        final World[] holder = new World[1];
        World world = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTemperature" -> temperature;
                    case "getHumidity" -> humidity;
                    case "getBlockAt" -> {
                        if (args.length == 1 && args[0] instanceof Location location) {
                            yield block(
                                    holder[0],
                                    location.getBlockX(),
                                    location.getBlockY(),
                                    location.getBlockZ()
                            );
                        }
                        yield block(
                                holder[0],
                                (int) args[0],
                                (int) args[1],
                                (int) args[2]
                        );
                    }
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
        holder[0] = world;
        return world;
    }

    private Block block(World world, int x, int y, int z) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "toString" -> "BlockFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        if (type == double.class) {
            return 0.0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
