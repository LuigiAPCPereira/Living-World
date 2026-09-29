package dev.signalshards.livingworld.features.landmarks.application;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandmarkDiscoveryTest {
    @Test
    void declaraTipoMundialComChaveDeMensagemPropria() {
        assertEquals("landmark", LandmarkDiscovery.TYPE.value());
        assertEquals(
                DiscoveryScope.WORLD,
                LandmarkDiscovery.DEFINITION.scope()
        );
        assertEquals(
                "discovery.type.landmark",
                LandmarkDiscovery.DEFINITION.labelMessageKey()
        );
    }

    @Test
    void identidadeEhUuidDaInstanciaEnaoCoordenada() {
        UUID landmarkId = UUID.randomUUID();

        assertEquals(
                landmarkId.toString(),
                LandmarkDiscovery.idFor(landmarkId).value()
        );
    }

    @Test
    void labelHumanizaSomenteOCaminhoDaChave() {
        assertEquals(
                "Village Plains",
                LandmarkDiscovery.labelFor("minecraft:village_plains")
        );
        assertEquals(
                "Ancient Temple Ruins",
                LandmarkDiscovery.labelFor("custom:ancient-temple/ruins")
        );
    }

    @Test
    void rejeitaChaveQueNaoEhNamespacedValida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LandmarkDiscovery.labelFor("village_plains")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> LandmarkDiscovery.labelFor("Minecraft:village_plains")
        );
    }

    @Test
    void mesmaInstanciaEhCompartilhadaPorTodoOMundo() {
        UUID worldId = UUID.randomUUID();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        UUID landmarkId = UUID.randomUUID();

        InMemoryWorldStore worlds = new InMemoryWorldStore();
        List<DiscoveryUnlocked> events = new ArrayList<>();
        DiscoveryEventPublisher publisher = new DiscoveryEventPublisher(
                failure -> {
                    throw new AssertionError(failure);
                }
        );
        publisher.subscribe(events::add);

        DiscoveryService discovery = new DiscoveryService(
                new DiscoveryRegistry(List.of(LandmarkDiscovery.DEFINITION)),
                publisher,
                new ForbiddenPlayerStore(),
                worlds
        );

        var first = LandmarkDiscovery.recordEncounter(
                discovery,
                worldId,
                firstPlayer,
                landmarkId,
                "minecraft:village_plains"
        );
        var repeatedByOtherPlayer = LandmarkDiscovery.recordEncounter(
                discovery,
                worldId,
                secondPlayer,
                landmarkId,
                "minecraft:village_plains"
        );

        assertTrue(first.firstDiscovery());
        assertFalse(repeatedByOtherPlayer.firstDiscovery());
        assertEquals(1, events.size());
        assertEquals(firstPlayer, events.getFirst().discoveredBy());
        assertEquals(worldId, events.getFirst().record().owner());
        assertEquals(1, worlds.load(worldId).size());
    }

    private static final class ForbiddenPlayerStore
            implements PlayerDiscoveryStore {
        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            throw new AssertionError(
                    "Landmark WORLD não deve ler memória pessoal"
            );
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
            throw new AssertionError(
                    "Landmark WORLD não deve gravar memória pessoal"
            );
        }
    }

    private static final class InMemoryWorldStore
            implements WorldDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> values =
                new HashMap<>();

        @Override
        public Set<DiscoveryRecord> load(UUID worldId) {
            return Set.copyOf(values.getOrDefault(worldId, Set.of()));
        }

        @Override
        public boolean add(UUID worldId, DiscoveryRecord record) {
            return values
                    .computeIfAbsent(worldId, ignored -> new HashSet<>())
                    .add(record);
        }
    }
}
