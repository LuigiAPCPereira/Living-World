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
        assertEquals(DiscoveryScope.WORLD, LandmarkDiscovery.DEFINITION.scope());
        assertEquals(
                "discovery.type.landmark",
                LandmarkDiscovery.DEFINITION.labelMessageKey()
        );
    }

    @Test
    void identidadePreservaNamespaceCompleto() {
        assertEquals(
                "terralith:fortified_village",
                LandmarkDiscovery.idFor("terralith:fortified_village").value()
        );
    }

    @Test
    void labelHumanizaSomenteOCaminhoSemAlterarIdentidade() {
        assertEquals(
                "Village Plains",
                LandmarkDiscovery.labelFor("minecraft:village_plains")
        );
        assertEquals(
                "Ancient Ruins Tower",
                LandmarkDiscovery.labelFor("custom:ancient-ruins/tower")
        );
    }

    @Test
    void rejeitaChaveQueNaoEhNamespacedValida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LandmarkDiscovery.idFor("village_plains")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> LandmarkDiscovery.idFor("Minecraft:village_plains")
        );
    }

    @Test
    void encontroUsaStoreMundialEPermaneceIdempotenteNoMesmoMundo() {
        UUID worldId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
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
                new RejectingPlayerStore(),
                worlds
        );

        var first = LandmarkDiscovery.recordEncounter(
                discovery,
                worldId,
                playerId,
                "minecraft:village_plains"
        );
        var repeated = LandmarkDiscovery.recordEncounter(
                discovery,
                worldId,
                UUID.randomUUID(),
                "minecraft:village_plains"
        );

        assertTrue(first.firstDiscovery());
        assertFalse(repeated.firstDiscovery());
        assertEquals(1, events.size());
        assertEquals(worldId, events.getFirst().record().owner());
        assertEquals(playerId, events.getFirst().discoveredBy());
        assertEquals("minecraft:village_plains", events.getFirst().record().id().value());
        assertEquals("Village Plains", events.getFirst().label());
        assertEquals(2, worlds.addAttempts);
    }

    @Test
    void mesmoTipoPodeSerDescobertoUmaVezEmCadaMundo() {
        UUID firstWorld = UUID.randomUUID();
        UUID secondWorld = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        InMemoryWorldStore worlds = new InMemoryWorldStore();
        DiscoveryService discovery = new DiscoveryService(
                new DiscoveryRegistry(List.of(LandmarkDiscovery.DEFINITION)),
                new DiscoveryEventPublisher(failure -> {
                    throw new AssertionError(failure);
                }),
                new RejectingPlayerStore(),
                worlds
        );

        var first = LandmarkDiscovery.recordEncounter(
                discovery,
                firstWorld,
                playerId,
                "minecraft:desert_pyramid"
        );
        var second = LandmarkDiscovery.recordEncounter(
                discovery,
                secondWorld,
                playerId,
                "minecraft:desert_pyramid"
        );

        assertTrue(first.firstDiscovery());
        assertTrue(second.firstDiscovery());
        assertEquals(2, worlds.addAttempts);
    }

    private static final class RejectingPlayerStore implements PlayerDiscoveryStore {
        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            throw new AssertionError("M16 não deve ler descoberta pessoal");
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
            throw new AssertionError("M16 não deve gravar descoberta pessoal");
        }
    }

    private static final class InMemoryWorldStore implements WorldDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> values = new HashMap<>();
        private int addAttempts;

        @Override
        public Set<DiscoveryRecord> load(UUID worldId) {
            return Set.copyOf(values.getOrDefault(worldId, Set.of()));
        }

        @Override
        public boolean add(UUID worldId, DiscoveryRecord record) {
            addAttempts++;
            return values.computeIfAbsent(worldId, ignored -> new HashSet<>())
                    .add(record);
        }
    }
}
