package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaystoneServiceTest {
    @Test
    void ativaSomenteWaystoneExistenteEListaOrdenada() {
        InMemoryRegistry registry = new InMemoryRegistry();
        InMemoryAccessStore access = new InMemoryAccessStore();
        WaystoneService service = new WaystoneService(registry, access);
        UUID player = UUID.randomUUID();
        UUID world = UUID.randomUUID();
        Waystone zeta = waystone("Zeta", world);
        Waystone alfa = waystone("Alfa", world);

        service.register(zeta);
        service.register(alfa);

        assertTrue(service.activate(player, zeta.id()));
        assertTrue(service.activate(player, alfa.id()));
        assertFalse(service.activate(player, new WaystoneId(UUID.randomUUID())));
        assertEquals(List.of(alfa, zeta), service.activatedWaystones(player));
    }

    @Test
    void segundaAtivacaoEhIdempotente() {
        InMemoryRegistry registry = new InMemoryRegistry();
        InMemoryAccessStore access = new InMemoryAccessStore();
        WaystoneService service = new WaystoneService(registry, access);
        UUID player = UUID.randomUUID();
        Waystone waystone = waystone("Casa", UUID.randomUUID());
        service.register(waystone);

        assertTrue(service.activate(player, waystone.id()));
        assertFalse(service.activate(player, waystone.id()));
    }

    @Test
    void encontraERemoveWaystonePelaAncora() {
        InMemoryRegistry registry = new InMemoryRegistry();
        WaystoneService service = new WaystoneService(registry, new InMemoryAccessStore());
        UUID world = UUID.randomUUID();
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Casa",
                world,
                10,
                64,
                10
        );
        service.register(waystone);

        assertEquals(
                Optional.of(waystone),
                service.findAt(world, 10, 64, 10)
        );
        assertTrue(service.remove(waystone.id()));
        assertTrue(service.findAt(world, 10, 64, 10).isEmpty());
    }

    @Test
    void renomeiaWaystoneAtivadaSemTrocarIdentidade() {
        InMemoryRegistry registry = new InMemoryRegistry();
        InMemoryAccessStore access = new InMemoryAccessStore();
        WaystoneService service = new WaystoneService(registry, access);
        UUID player = UUID.randomUUID();
        Waystone original = waystone("Casa", UUID.randomUUID());
        service.register(original);
        service.activate(player, original.id());

        Waystone renamed = service.renameActivated(
                player,
                original.id(),
                "Casa da Montanha"
        ).orElseThrow();

        assertEquals(original.id(), renamed.id());
        assertEquals("Casa da Montanha", renamed.name());
        assertEquals(
                Optional.of(renamed),
                service.find(original.id())
        );
        assertTrue(service.findActivated(player, original.id()).isPresent());
    }

    @Test
    void naoRenomeiaWaystoneSemAtivacao() {
        InMemoryRegistry registry = new InMemoryRegistry();
        WaystoneService service = new WaystoneService(
                registry,
                new InMemoryAccessStore()
        );
        Waystone waystone = waystone("Casa", UUID.randomUUID());
        service.register(waystone);

        assertTrue(service.renameActivated(
                UUID.randomUUID(),
                waystone.id(),
                "Outro nome"
        ).isEmpty());
        assertEquals(Optional.of(waystone), service.find(waystone.id()));
    }

    private Waystone waystone(String name, UUID world) {
        return new Waystone(WaystoneId.random(), name, world, 10, 64, 10);
    }

    private static final class InMemoryRegistry implements WaystoneRegistry {
        private final Map<WaystoneId, Waystone> values = new HashMap<>();

        @Override
        public void register(Waystone waystone) {
            values.put(waystone.id(), waystone);
        }

        @Override
        public Optional<Waystone> find(WaystoneId id) {
            return Optional.ofNullable(values.get(id));
        }

        @Override
        public Optional<Waystone> findAt(UUID worldId, int x, int y, int z) {
            return values.values().stream()
                    .filter(waystone -> waystone.worldId().equals(worldId))
                    .filter(waystone -> waystone.x() == x)
                    .filter(waystone -> waystone.y() == y)
                    .filter(waystone -> waystone.z() == z)
                    .findFirst();
        }

        @Override
        public List<Waystone> all() {
            return new ArrayList<>(values.values());
        }

        @Override
        public boolean remove(WaystoneId id) {
            return values.remove(id) != null;
        }
    }

    private static final class InMemoryAccessStore implements WaystoneAccessStore {
        private final Map<UUID, Set<WaystoneId>> values = new HashMap<>();

        @Override
        public Set<WaystoneId> load(UUID playerId) {
            return Set.copyOf(values.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean add(UUID playerId, WaystoneId waystoneId) {
            return values.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(waystoneId);
        }
    }
}
