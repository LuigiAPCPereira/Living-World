package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryEventPublisher;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryOutcome;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaystoneDiscoveryTest {
    @Test
    void acessoLegadoSemDiscoveryRecebeBackfillUmaUnicaVez() {
        UUID player = UUID.randomUUID();
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Obelisco de Khnor",
                UUID.randomUUID(),
                12,
                70,
                -8
        );
        InMemoryWaystoneRegistry registry = new InMemoryWaystoneRegistry();
        InMemoryWaystoneAccessStore access = new InMemoryWaystoneAccessStore();
        WaystoneService waystones = new WaystoneService(registry, access);
        waystones.register(waystone);

        assertTrue(waystones.activate(player, waystone.id()));
        assertFalse(waystones.activate(player, waystone.id()));

        InMemoryPlayerDiscoveryStore discoveries = new InMemoryPlayerDiscoveryStore();
        DiscoveryService discovery = new DiscoveryService(
                new DiscoveryRegistry(List.of(WaystoneDiscovery.DEFINITION)),
                new DiscoveryEventPublisher(ignored -> {
                }),
                discoveries,
                new EmptyWorldDiscoveryStore()
        );

        DiscoveryOutcome backfill = WaystoneDiscovery.recordInteraction(
                discovery,
                player,
                waystone
        );
        DiscoveryOutcome repeated = WaystoneDiscovery.recordInteraction(
                discovery,
                player,
                waystone
        );

        assertTrue(backfill.firstDiscovery());
        assertFalse(repeated.firstDiscovery());
    }

    private static final class InMemoryWaystoneRegistry implements WaystoneRegistry {
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
            return List.copyOf(values.values());
        }

        @Override
        public boolean remove(WaystoneId id) {
            return values.remove(id) != null;
        }
    }

    private static final class InMemoryWaystoneAccessStore implements WaystoneAccessStore {
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

    private static final class InMemoryPlayerDiscoveryStore implements PlayerDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> values = new HashMap<>();

        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            return Set.copyOf(values.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
            return values.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(record);
        }
    }

    private static final class EmptyWorldDiscoveryStore implements WorldDiscoveryStore {
        @Override
        public Set<DiscoveryRecord> load(UUID worldId) {
            return Set.of();
        }

        @Override
        public boolean add(UUID worldId, DiscoveryRecord record) {
            throw new AssertionError("O teste não deveria gravar descoberta mundial");
        }
    }
}
