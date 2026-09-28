package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.InMemoryPlayerThermalStateStore;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.PlayerThermalSimulation;
import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
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
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperThermalRuntimeReadoutProviderTest {
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
