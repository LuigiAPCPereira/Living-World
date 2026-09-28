package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryServiceTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");
    private static final DiscoveryType LANDMARK = new DiscoveryType("landmark");

    @Test
    void registraPrimeiraDescobertaPessoalNoJogadorEPublica() {
        UUID player = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        InMemoryWorldStore worlds = new InMemoryWorldStore();
        List<DiscoveryUnlocked> published = new ArrayList<>();
        DiscoveryService service = service(players, worlds, List.of(), List.of(published::add));

        DiscoveryOutcome outcome = service.discover(request(WAYSTONE, player, player, "Portal da Savana"));

        assertTrue(outcome.firstDiscovery());
        assertEquals(DiscoveryScope.PERSONAL, outcome.record().scope());
        assertEquals(player, outcome.record().owner());
        assertEquals(1, players.recordsFor(player).size());
        assertTrue(worlds.records.isEmpty());
        assertEquals(1, published.size());
        assertEquals("Portal da Savana", published.get(0).label());
    }

    @Test
    void reencontrarOMesmoLugarEhIdempotenteESilencioso() {
        UUID player = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        List<DiscoveryUnlocked> published = new ArrayList<>();
        DiscoveryService service = service(players, new InMemoryWorldStore(), List.of(), List.of(published::add));
        DiscoveryRequest request = request(WAYSTONE, player, player, "Portal da Savana");

        service.discover(request);
        DiscoveryOutcome second = service.discover(request);

        assertFalse(second.firstDiscovery());
        assertEquals(1, players.recordsFor(player).size());
        assertEquals(1, published.size());
    }

    @Test
    void tipoMundialVaiParaOMundoComODonoDoMundo() {
        UUID player = UUID.randomUUID();
        UUID world = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        InMemoryWorldStore worlds = new InMemoryWorldStore();
        DiscoveryService service = service(
                players,
                worlds,
                List.of(definition(LANDMARK, DiscoveryScope.WORLD)),
                List.of()
        );

        DiscoveryOutcome outcome = service.discover(request(LANDMARK, world, player, "Vila antiga"));

        assertEquals(DiscoveryScope.WORLD, outcome.record().scope());
        assertEquals(world, outcome.record().owner());
        assertEquals(1, worlds.recordsFor(world).size());
        assertTrue(players.recordsFor(player).isEmpty());
    }

    @Test
    void tipoNaoDeclaradoFalhaFechado() {
        DiscoveryService service = service(
                new InMemoryPlayerStore(),
                new InMemoryWorldStore(),
                List.of(),
                List.of()
        );
        UUID player = UUID.randomUUID();

        assertThrows(
                IllegalStateException.class,
                () -> service.discover(request(LANDMARK, player, player, "Vila antiga"))
        );
    }

    @Test
    void descobertaPessoalDeOutroJogadorFalhaFechado() {
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        DiscoveryService service = service(players, new InMemoryWorldStore(), List.of(), List.of());
        UUID player = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.discover(request(WAYSTONE, other, player, "Portal da Savana"))
        );
        assertTrue(players.recordsFor(other).isEmpty());
        assertTrue(players.recordsFor(player).isEmpty());
    }

    @Test
    void falhaDeUmListenerNaoDesfazGravacaoNemImpedeOsDemais() {
        UUID player = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        List<DiscoveryUnlocked> published = new ArrayList<>();
        List<RuntimeException> failures = new ArrayList<>();
        List<DiscoveryListener> listeners = List.of(
                unlocked -> {
                    throw new IllegalStateException("apresentação quebrada");
                },
                published::add
        );
        DiscoveryService service = service(
                players,
                new InMemoryWorldStore(),
                List.of(),
                listeners,
                failures
        );

        DiscoveryOutcome outcome = service.discover(request(WAYSTONE, player, player, "Portal da Savana"));

        assertTrue(outcome.firstDiscovery());
        assertEquals(1, players.recordsFor(player).size());
        assertEquals(1, published.size());
        assertEquals(1, failures.size());
        assertEquals("apresentação quebrada", failures.get(0).getMessage());
    }

    @Test
    void listaDescobertasDoDonoNaOrdemEmQueForamDescobertas() {
        UUID player = UUID.randomUUID();
        InMemoryPlayerStore players = new InMemoryPlayerStore();
        DiscoveryService service = service(players, new InMemoryWorldStore(), List.of(), List.of());
        service.discover(request(WAYSTONE, "primeira", player, player, "Primeiro"));
        service.discover(request(WAYSTONE, "segunda", player, player, "Segundo"));

        Set<DiscoveryRecord> discovered = service.discoveries(player, DiscoveryScope.PERSONAL);

        assertEquals(
                List.of("primeira", "segunda"),
                discovered.stream().map(record -> record.id().value()).toList()
        );
    }

    private DiscoveryService service(
            PlayerDiscoveryStore players,
            WorldDiscoveryStore worlds,
            List<DiscoveryTypeDefinition> definitions,
            List<DiscoveryListener> listeners
    ) {
        return service(players, worlds, definitions, listeners, new ArrayList<>());
    }

    private DiscoveryService service(
            PlayerDiscoveryStore players,
            WorldDiscoveryStore worlds,
            List<DiscoveryTypeDefinition> definitions,
            List<DiscoveryListener> listeners,
            List<RuntimeException> failures
    ) {
        List<DiscoveryTypeDefinition> declared = definitions.isEmpty()
                ? List.of(definition(WAYSTONE, DiscoveryScope.PERSONAL))
                : definitions;
        DiscoveryEventPublisher publisher = new DiscoveryEventPublisher(failures::add);
        listeners.forEach(publisher::subscribe);
        return new DiscoveryService(new DiscoveryRegistry(declared), publisher, players, worlds);
    }

    private DiscoveryTypeDefinition definition(DiscoveryType type, DiscoveryScope scope) {
        return new DiscoveryTypeDefinition(type, scope, "discovery.type." + type.value());
    }

    private DiscoveryRequest request(
            DiscoveryType type,
            UUID owner,
            UUID discoveredBy,
            String label
    ) {
        return request(type, UUID.randomUUID().toString(), owner, discoveredBy, label);
    }

    private DiscoveryRequest request(
            DiscoveryType type,
            String id,
            UUID owner,
            UUID discoveredBy,
            String label
    ) {
        return new DiscoveryRequest(
                type,
                new DiscoveryId(id),
                owner,
                discoveredBy,
                label
        );
    }

    private static final class InMemoryPlayerStore implements PlayerDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> records = new LinkedHashMap<>();

        @Override
        public Set<DiscoveryRecord> load(UUID playerId) {
            return records.getOrDefault(playerId, Set.of());
        }

        @Override
        public boolean add(UUID playerId, DiscoveryRecord record) {
            return records.computeIfAbsent(playerId, key -> new LinkedHashSet<>()).add(record);
        }

        Set<DiscoveryRecord> recordsFor(UUID playerId) {
            return records.getOrDefault(playerId, Set.of());
        }
    }

    private static final class InMemoryWorldStore implements WorldDiscoveryStore {
        private final Map<UUID, Set<DiscoveryRecord>> records = new LinkedHashMap<>();

        @Override
        public Set<DiscoveryRecord> load(UUID worldId) {
            return records.getOrDefault(worldId, Set.of());
        }

        @Override
        public boolean add(UUID worldId, DiscoveryRecord record) {
            return records.computeIfAbsent(worldId, key -> new LinkedHashSet<>()).add(record);
        }

        Set<DiscoveryRecord> recordsFor(UUID worldId) {
            return records.getOrDefault(worldId, Set.of());
        }
    }
}
