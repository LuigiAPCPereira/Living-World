package dev.signalshards.livingworld.features.biomes.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryEventPublisher;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeEntryTrackerTest {
    @Test
    void suprimeObservacaoEnquantoJogadorPermaneceNoMesmoBiome() {
        Fixture fixture = new Fixture();
        UUID player = UUID.randomUUID();

        assertTrue(fixture.tracker.observe(player, "minecraft:plains").isPresent());
        assertTrue(fixture.tracker.observe(player, "minecraft:plains").isEmpty());

        assertEquals(1, fixture.events.size());
        assertEquals(1, fixture.playerStore.addAttempts);
    }

    @Test
    void transicaoParaNovoBiomeProduzNovaDescoberta() {
        Fixture fixture = new Fixture();
        UUID player = UUID.randomUUID();

        var plains = fixture.tracker.observe(player, "minecraft:plains");
        var forest = fixture.tracker.observe(player, "minecraft:forest");

        assertTrue(plains.orElseThrow().firstDiscovery());
        assertTrue(forest.orElseThrow().firstDiscovery());
        assertEquals(2, fixture.events.size());
        assertEquals("minecraft:forest", fixture.events.getLast().record().id().value());
    }

    @Test
    void retornoABiomeConhecidoEhObservadoMasPermaneceSilencioso() {
        Fixture fixture = new Fixture();
        UUID player = UUID.randomUUID();

        fixture.tracker.observe(player, "minecraft:plains");
        fixture.tracker.observe(player, "minecraft:forest");
        var returnToPlains = fixture.tracker.observe(player, "minecraft:plains");

        assertTrue(returnToPlains.isPresent());
        assertFalse(returnToPlains.orElseThrow().firstDiscovery());
        assertEquals(2, fixture.events.size());
        assertEquals(3, fixture.playerStore.addAttempts);
    }

    @Test
    void cacheEhPorJogadorELimpaNoQuit() {
        Fixture fixture = new Fixture();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        fixture.tracker.observe(first, "minecraft:plains");
        fixture.tracker.observe(second, "minecraft:plains");
        assertEquals(2, fixture.tracker.trackedPlayerCount());

        fixture.tracker.forget(first);
        assertEquals(1, fixture.tracker.trackedPlayerCount());

        var afterReconnect = fixture.tracker.observe(first, "minecraft:plains");
        assertTrue(afterReconnect.isPresent());
        assertFalse(afterReconnect.orElseThrow().firstDiscovery());
        assertEquals(2, fixture.events.size());
    }

    @Test
    void disableLimpaTodoCache() {
        Fixture fixture = new Fixture();
        fixture.tracker.observe(UUID.randomUUID(), "minecraft:plains");
        fixture.tracker.observe(UUID.randomUUID(), "custom:scarlet_forest");

        fixture.tracker.clear();

        assertEquals(0, fixture.tracker.trackedPlayerCount());
    }

    private static final class Fixture {
        private final CountingPlayerStore playerStore = new CountingPlayerStore();
        private final List<DiscoveryUnlocked> events = new ArrayList<>();
        private final BiomeEntryTracker tracker;

        private Fixture() {
            DiscoveryEventPublisher publisher = new DiscoveryEventPublisher(
                    failure -> {
                        throw new AssertionError(failure);
                    }
            );
            publisher.subscribe(events::add);
            DiscoveryService discovery = new DiscoveryService(
                    new DiscoveryRegistry(List.of(BiomeDiscovery.DEFINITION)),
                    publisher,
                    playerStore,
                    new EmptyWorldStore()
            );
            tracker = new BiomeEntryTracker(discovery);
        }
    }

    private static final class CountingPlayerStore implements PlayerDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> values = new HashMap<>();
        private int addAttempts;

        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            return Set.copyOf(values.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
            addAttempts++;
            return values.computeIfAbsent(playerId, ignored -> new HashSet<>())
                    .add(record);
        }
    }

    private static final class EmptyWorldStore implements WorldDiscoveryStore {
        @Override
        public Set<DiscoveryRecord> load(UUID worldId) {
            return Set.of();
        }

        @Override
        public boolean add(UUID worldId, DiscoveryRecord record) {
            throw new AssertionError("M15 não deve gravar descoberta mundial");
        }
    }
}
