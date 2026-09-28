package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperLocalClimateResolverTest {
    @Test
    void resolveTemperaturaAmbienteSemExposicaoDeJogador() {
        World world = world(0.40D, 0.80D);
        PaperLocalClimateResolver resolver = resolver(world);

        assertEquals(
                7.0D,
                resolver.ambientTemperatureAt(block(world, 10, 64, -20)).degreesCelsius()
        );
    }

    @Test
    void rejeitaBlocoDeOutroMundoTambemNaTemperaturaAmbiente() {
        World configured = world(0.80D, 0.50D);
        World other = world(0.80D, 0.50D);
        PaperLocalClimateResolver resolver = resolver(configured);

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.ambientTemperatureAt(block(other, 0, 64, 0))
        );
    }

    private PaperLocalClimateResolver resolver(World world) {
        return new PaperLocalClimateResolver(
                world,
                calendar(),
                new ClimateProfileClassifier(
                        ClimateProfileThresholds.livingWorldDefaults()
                ),
                new ClimatePolicy()
        );
    }

    private CalendarView calendar() {
        return new CalendarView() {
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
                    default -> defaultValue(method.getReturnType());
                }
        );
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
