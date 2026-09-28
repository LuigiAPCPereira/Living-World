package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperWorldgenAmbientTemperatureResolverTest {
    private final CalendarView calendar = new CalendarView() {
        @Override
        public CalendarDate currentDate() {
            return new CalendarDate(1, 4, 4);
        }

        @Override
        public Season currentSeason() {
            return Season.VERAO;
        }

        @Override
        public double currentSeasonProgress() {
            return 0.5D;
        }

        @Override
        public int daysPerMonth() {
            return 8;
        }
    };
    private final PaperWorldgenAmbientTemperatureResolver resolver =
            new PaperWorldgenAmbientTemperatureResolver(calendar);

    @Test
    void overworldMantemSeasonDiaEChuva() {
        Block block = block(
                world(World.Environment.NORMAL),
                10,
                80,
                -10,
                15
        );

        assertEquals(
                22.0D,
                resolver.ambientTemperatureAt(block).degreesCelsius(),
                1.0E-9D
        );
    }

    @Test
    void netherIgnoraSeasonDiaEWeatherTerrestres() {
        Block block = block(
                world(World.Environment.NETHER),
                10,
                80,
                -10,
                15
        );

        assertEquals(
                15.0D,
                resolver.ambientTemperatureAt(block).degreesCelsius(),
                1.0E-9D
        );
    }

    @Test
    void customUsaFallbackConservadorSemConsultarNomeDeBiome() {
        Block block = block(
                world(World.Environment.CUSTOM),
                10,
                80,
                -10,
                15
        );

        assertEquals(
                15.0D,
                resolver.ambientTemperatureAt(block).degreesCelsius(),
                1.0E-9D
        );
    }

    private World world(World.Environment environment) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getEnvironment" -> environment;
                    case "getTemperature" -> 0.8D;
                    case "getHumidity" -> 0.5D;
                    case "getTime" -> 6_000L;
                    case "hasStorm" -> true;
                    case "isThundering" -> false;
                    case "toString" -> "FakeWorld";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new AssertionError(
                            "Chamada inesperada: " + method.getName()
                    );
                }
        );
    }

    private Block block(
            World world,
            int x,
            int y,
            int z,
            int skyLight
    ) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> x;
                    case "getY" -> y;
                    case "getZ" -> z;
                    case "getLightFromSky" -> (byte) skyLight;
                    default -> throw new AssertionError(
                            "Chamada inesperada: " + method.getName()
                    );
                }
        );
    }
}
