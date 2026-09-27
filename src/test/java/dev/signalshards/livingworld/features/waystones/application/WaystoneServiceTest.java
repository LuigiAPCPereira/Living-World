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
