package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.InMemoryPlayerThermalStateStore;
import dev.signalshards.livingworld.features.climate.application.InMemoryThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalSimulation;
import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.domain.BreathFeedback;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperThermalRuntimeReadoutProviderTest {
    @Test
    void snapshotPublicadoEvitaNovoProbeAmbiental() {
        UUID playerId = UUID.randomUUID();
        AtomicInteger environmentReads = new AtomicInteger();
        PlayerThermalRuntimeService runtime = new PlayerThermalRuntimeService();
        InMemoryThermalRuntimeReadoutStore readouts =
                new InMemoryThermalRuntimeReadoutStore();
        ThermalRuntimeReadout published = new ThermalRuntimeReadout(
                PlayerThermalBand.COLD,
                -0.40D,
                0.25D,
                7.5D,
                PlayerActivity.RESTING,
                0.0D,
                0.0D,
                0.0D,
                DirectThermalExposure.NONE,
                0.0D,
                1.0D,
                0,
                0,
                -0.001D,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                -0.001D,
                0.0D,
                new ThermalFeedbackProfile(BreathFeedback.none(), 0.0D)
        );
        readouts.save(playerId, published);
        PaperThermalEnvironmentProvider environment = ignored -> {
            environmentReads.incrementAndGet();
            throw new AssertionError("snapshot publicado não deve reprovar ambiente");
        };
        PaperThermalRuntimeReadoutProvider provider =
                new PaperThermalRuntimeReadoutProvider(
                        environment,
                        runtime,
                        readouts
                );

        var actual = provider.snapshot(player(playerId));

        assertEquals(published, actual);
        assertEquals(0, environmentReads.get());
    }

    @Test
    void combinaSnapshotPersistidoComAmbienteAtualSemAvancarEstado() {
        UUID playerId = UUID.randomUUID();
        InMemoryPlayerThermalStateStore store = new InMemoryPlayerThermalStateStore();
        PlayerThermalSnapshot body = new PlayerThermalSnapshot(
                new PlayerThermalState(-0.30D),
                new WetnessState(0.40D)
        );
        store.save(playerId, body);
        PlayerThermalRuntimeService runtime = new PlayerThermalRuntimeService(
                store,
                new PlayerThermalSimulation()
        );
        PaperThermalEnvironmentProvider environment = ignored ->
                new ThermalEnvironmentContext(
                        new AmbientTemperature(5.0D),
                        new WaterExposure(0.50D, 0.0D),
                        PlayerActivity.SPRINTING,
                        new WindExposure(0.60D),
                        new ShelterFactor(0.20D),
                        ArmorThermalLoadout.empty(),
                        List.of()
                );
        PaperThermalRuntimeReadoutProvider provider =
                new PaperThermalRuntimeReadoutProvider(environment, runtime);
        Player player = player(playerId);

        var readout = provider.snapshot(player);

        assertEquals(PlayerThermalBand.VERY_COLD, readout.thermalBand());
        assertEquals(-0.30D, readout.thermalLoad(), 1.0E-9D);
        assertEquals(0.40D, readout.wetness(), 1.0E-9D);
        assertEquals(PlayerActivity.SPRINTING, readout.activity());
        assertEquals(0.50D, readout.submergedFraction(), 1.0E-9D);
        assertTrue(readout.netRatePerSecond() < 0.0D);
        assertTrue(readout.feedbackProfile().breath().enabled());
        assertEquals(
                0.0D,
                readout.feedbackProfile().frostIntensity(),
                1.0E-9D
        );
        assertEquals(body, runtime.snapshot(playerId).orElseThrow());
    }

    private Player player(UUID playerId) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "toString" -> "FakePlayer";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );
    }
}
