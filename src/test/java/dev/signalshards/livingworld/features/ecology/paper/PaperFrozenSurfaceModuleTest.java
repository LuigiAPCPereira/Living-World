package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.FrozenSurfacePolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperFrozenSurfaceModuleTest {
    @Test
    void cancelaFormacaoDeGeloQuandoClimaEfetivoNaoEhFrio() {
        World world = world(0.8D, 0.5D);
        PaperFrozenSurfaceModule module = module(world, Season.OUTONO);
        BlockFormEvent event = new BlockFormEvent(
                block(world, Material.WATER),
                state(Material.ICE)
        );

        module.onNaturalFormation(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void permiteFormacaoDeGeloQuandoClimaEfetivoEhFrio() {
        World world = world(0.4D, 0.5D);
        PaperFrozenSurfaceModule module = module(world, Season.OUTONO);
        BlockFormEvent event = new BlockFormEvent(
                block(world, Material.WATER),
                state(Material.ICE)
        );

        module.onNaturalFormation(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void preservaGeloContraFadeEnquantoClimaEhFrio() {
        World world = world(0.4D, 0.5D);
        PaperFrozenSurfaceModule module = module(world, Season.OUTONO);
        BlockFadeEvent event = new BlockFadeEvent(
                block(world, Material.ICE),
                state(Material.WATER)
        );

        module.onNaturalFade(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void ignoraFormacaoCausadaPorEntidade() {
        World world = world(0.8D, 0.5D);
        PaperFrozenSurfaceModule module = module(world, Season.OUTONO);
        EntityBlockFormEvent event = new EntityBlockFormEvent(
                entity(),
                block(world, Material.WATER),
                state(Material.ICE)
        );

        module.onNaturalFormation(event);

        assertFalse(event.isCancelled());
    }

    private PaperFrozenSurfaceModule module(World world, Season season) {
        return new PaperFrozenSurfaceModule(
                plugin(),
                world,
                PaperEcologySettings.defaults(),
                new PaperLocalClimateResolver(
                        world,
                        calendar(season),
                        new ClimateProfileClassifier(
                                ClimateProfileThresholds.livingWorldDefaults()
                        ),
                        new ClimatePolicy()
                ),
                new FrozenSurfacePolicy()
        );
    }

    private CalendarView calendar(Season season) {
        return new CalendarView() {
            @Override
            public CalendarDate currentDate() {
                return new CalendarDate(1, 1, 1);
            }

            @Override
            public Season currentSeason() {
                return season;
            }

            @Override
            public int daysPerMonth() {
                return 8;
            }
        };
    }

    private Plugin plugin() {
        return proxy(Plugin.class, "PluginFake");
    }

    private Entity entity() {
        return proxy(Entity.class, "EntityFake");
    }

    private Block block(World world, Material material) {
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[]{Block.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> 10;
                    case "getY" -> 64;
                    case "getZ" -> -20;
                    case "getType" -> material;
                    case "toString" -> "BlockFake(" + material + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private BlockState state(Material material) {
        return (BlockState) Proxy.newProxyInstance(
                BlockState.class.getClassLoader(),
                new Class<?>[]{BlockState.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getType" -> material;
                    case "toString" -> "BlockStateFake(" + material + ")";
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

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, String name) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> name;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
