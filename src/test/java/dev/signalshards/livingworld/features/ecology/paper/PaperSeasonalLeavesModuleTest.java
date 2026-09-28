package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.domain.*;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.ecology.domain.*;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaperSeasonalLeavesModuleTest {
    @Test
    void semMovimentoNaoAgendaNemConsultaMundo() {
        Fixture f = new Fixture();
        f.module.enable();
        assertEquals(0, f.reads);
        assertEquals(0, f.samples);
        assertEquals(1, f.registrations);
    }

    @Test
    void movimentoEmiteSomenteAoJogadorEAplicaIntervalo() {
        Fixture f = new Fixture();
        f.module.enable();
        f.module.onMove(f.move());
        assertEquals(Particle.CHERRY_LEAVES, f.particle);
        assertEquals(1, f.emissions);
        for (int i = 0; i < 100; i++) f.module.onMove(f.move());
        assertEquals(1, f.reads);
        f.now += 5_000_000_000L;
        f.module.onMove(f.move());
        assertEquals(2, f.emissions);
    }

    @Test
    void rejeicaoEcologicaNaoTentaOutraFolhaNaMesmaJanela() {
        Fixture f = new Fixture();
        f.sample = 0.99;
        f.module.enable();
        f.module.onMove(f.move());
        f.module.onMove(f.move());
        assertEquals(1, f.samples);
        assertEquals(1, f.reads);
        assertEquals(0, f.emissions);
    }

    @Test
    void semFolhasLimitaNoveSondasESemClima() {
        Fixture f = new Fixture();
        f.leaves = false;
        f.module.enable();
        f.module.onMove(f.move());
        assertEquals(9, f.reads);
        assertEquals(0, f.climateReads);
        assertEquals(0, f.samples);
    }

    @Test
    void naoCarregaChunksNemConsultaAlturaInvalida() {
        Fixture f = new Fixture();
        f.loaded = false;
        f.module.enable();
        f.module.onMove(f.move());
        assertEquals(0, f.reads);
        f.loaded = true;
        f.now += 5_000_000_000L;
        f.module.onMove(new PlayerMoveEvent(f.player,
                new Location(f.world, 0, 320, 0), new Location(f.world, 1, 320, 0)));
        assertEquals(0, f.reads);
    }

    @Test
    void ignoraRotacaoCancelamentoTeleportOutroMundoEJogadorOffline() {
        Fixture f = new Fixture();
        f.module.enable();
        Location origin = new Location(f.world, 0, 64, 0);
        f.module.onMove(new PlayerMoveEvent(f.player, origin, origin.clone()));
        PlayerMoveEvent cancelled = f.move();
        cancelled.setCancelled(true);
        f.module.onMove(cancelled);
        f.module.onMove(new PlayerTeleportEvent(f.player, origin, new Location(f.world, 1, 64, 0)));
        World other = stub(World.class, (p,m,a) -> unexpected(m.getName()));
        f.module.onMove(new PlayerMoveEvent(f.player, origin, new Location(other, 1, 64, 0)));
        f.online = false;
        f.module.onMove(f.move());
        assertEquals(0, f.reads);
        assertEquals(0, f.samples);
    }

    @Test
    void saidaEDesativacaoLimpamIntervaloEEncerramProcessamento() {
        Fixture f = new Fixture();
        f.module.enable();
        f.module.onMove(f.move());
        f.module.onQuit(new PlayerQuitEvent(f.player, (net.kyori.adventure.text.Component) null,
                PlayerQuitEvent.QuitReason.DISCONNECTED));
        f.module.onMove(f.move());
        assertEquals(2, f.emissions);
        f.module.disable();
        f.module.onMove(f.move());
        assertEquals(2, f.emissions);
        f.module.enable();
        f.module.onMove(f.move());
        assertEquals(3, f.emissions);
    }

    private static class Fixture {
        long now;
        int reads, samples, climateReads, emissions, registrations;
        boolean loaded = true, leaves = true, online = true;
        double sample;
        Particle particle;
        final UUID id = UUID.randomUUID();
        final CalendarView calendar = new CalendarView() {
            public CalendarDate currentDate() { return new CalendarDate(1, 1, 1); }
            public Season currentSeason() { return Season.PRIMAVERA; }
            public int daysPerMonth() { return 8; }
        };
        final World world = stub(World.class, (p,m,a) -> switch (m.getName()) {
            case "getMinHeight" -> -64;
            case "getMaxHeight" -> 320;
            case "isChunkLoaded" -> loaded;
            case "getTemperature" -> { climateReads++; yield 0.7D; }
            case "getHumidity" -> 0.6D;
            case "getBlockAt" -> { reads++; yield block((int)a[0], (int)a[1], (int)a[2]); }
            default -> unexpected(m.getName());
        });
        final Player player = stub(Player.class, (p,m,a) -> switch (m.getName()) {
            case "getUniqueId" -> id;
            case "getWorld" -> world;
            case "isOnline" -> online;
            case "spawnParticle" -> { emissions++; particle = (Particle)a[0]; assertEquals(3, a[4]); yield null; }
            default -> unexpected(m.getName());
        });
        final PluginManager manager = stub(PluginManager.class, (p,m,a) -> {
            if (m.getName().equals("registerEvents")) { registrations++; return null; }
            return unexpected(m.getName());
        });
        final Server server = stub(Server.class, (p,m,a) ->
                m.getName().equals("getPluginManager") ? manager : unexpected(m.getName()));
        final Plugin plugin = stub(Plugin.class, (p,m,a) ->
                m.getName().equals("getServer") ? server : unexpected(m.getName()));
        final PaperSeasonalLeavesModule module = new PaperSeasonalLeavesModule(
                plugin, world, calendar,
                new PaperLocalClimateResolver(world, calendar,
                        new ClimateProfileClassifier(ClimateProfileThresholds.livingWorldDefaults()), new ClimatePolicy()),
                new SeasonalLeafVisualPolicy(new NaturalGrowthSuitabilityPolicy(), new DefaultSeasonalEcologyModifier()),
                () -> now, () -> { samples++; return sample; });

        PlayerMoveEvent move() {
            return new PlayerMoveEvent(player, new Location(world, 0, 64, 0), new Location(world, 1, 64, 0));
        }

        Block block(int x, int y, int z) {
            return stub(Block.class, (p,m,a) -> switch (m.getName()) {
                case "getWorld" -> world;
                case "getX" -> x;
                case "getY" -> y;
                case "getZ" -> z;
                case "getBlockData" -> leaves
                        ? stub(Leaves.class, (q,n,b) -> unexpected(n.getName()))
                        : stub(BlockData.class, (q,n,b) -> unexpected(n.getName()));
                default -> unexpected(m.getName());
            });
        }
    }

    private static Object unexpected(String method) {
        throw new AssertionError("Chamada não esperada: " + method);
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
