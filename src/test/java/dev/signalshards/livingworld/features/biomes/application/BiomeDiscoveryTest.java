package dev.signalshards.livingworld.features.biomes.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryEventPublisher;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeDiscoveryTest {
    @Test
    void declaraTipoPessoalComChaveDeMensagemPropria() {
        assertEquals("biome", BiomeDiscovery.TYPE.value());
        assertEquals(DiscoveryScope.PERSONAL, BiomeDiscovery.DEFINITION.scope());
        assertEquals(
                "discovery.type.biome",
                BiomeDiscovery.DEFINITION.labelMessageKey()
        );
    }

    @Test
    void identidadePreservaNamespaceCompleto() {
        assertEquals(
                "terralith:yellowstone",
                BiomeDiscovery.idFor("terralith:yellowstone").value()
        );
    }

    @Test
    void labelHumanizaSomenteOCaminhoSemAlterarIdentidade() {
        assertEquals(
                "Snowy Plains",
                BiomeDiscovery.labelFor("minecraft:snowy_plains")
        );
        assertEquals(
                "Scarlet Mountains Peaks",
                BiomeDiscovery.labelFor("custom:scarlet-mountains/peaks")
        );
    }

    @Test
    void rejeitaChaveQueNaoEhNamespacedValida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BiomeDiscovery.idFor("plains")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> BiomeDiscovery.idFor("Minecraft:plains")
        );
    }

    @Test
    void encontroUsaDiscoveryExistenteEPermaneceIdempotente() {
        UUID playerId = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        List<DiscoveryUnlocked> events = new ArrayList<>();
        DiscoveryEventPublisher publisher = new DiscoveryEventPublisher(
                failure -> {
                    throw new AssertionError(failure);
                }
        );
        publisher.subscribe(events::add);
        DiscoveryService discovery = new DiscoveryService(
                new DiscoveryRegistry(List.of(BiomeDiscovery.DEFINITION)),
                publisher,
                players,
                new EmptyWorldStore()
        );

        var first = BiomeDiscovery.recordEncounter(
                discovery,
                playerId,
                "minecraft:snowy_plains"
        );
        var repeated = BiomeDiscovery.recordEncounter(
                discovery,
                playerId,
                "minecraft:snowy_plains"
        );

        assertTrue(first.firstDiscovery());
        assertFalse(repeated.firstDiscovery());
        assertEquals(1, events.size());
        assertEquals("minecraft:snowy_plains", events.getFirst().record().id().value());
        assertEquals("Snowy Plains", events.getFirst().label());
    }

    private static final class InMemoryPlayerStore implements PlayerDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> values = new HashMap<>();

        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            return Set.copyOf(values.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
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
