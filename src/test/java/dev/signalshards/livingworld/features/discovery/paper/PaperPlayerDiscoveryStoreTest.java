package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.testsupport.PersistentDataContainerTestDouble;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPlayerDiscoveryStoreTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");
    private static final NamespacedKey KEY = new NamespacedKey("livingworld", "discovery_learned");

    @Test
    void persisteDescobertasDoJogadorOnlinePreservandoOrdem() {
        UUID playerId = UUID.randomUUID();
        PaperPlayerDiscoveryStore store = store(playerId, playerId, new HashMap<>());
        DiscoveryRecord first = record(playerId, "primeira");
        DiscoveryRecord second = record(playerId, "segunda");

        assertTrue(store.add(playerId, first));
        assertTrue(store.add(playerId, second));
        assertFalse(store.add(playerId, first));

        assertEquals(
                List.of("primeira", "segunda"),
                store.load(playerId).stream().map(entry -> entry.id().value()).toList()
        );
    }

    @Test
    void leituraRecuperaOTipoEOEscopoPersistidos() {
        UUID playerId = UUID.randomUUID();
        PaperPlayerDiscoveryStore store = store(playerId, playerId, new HashMap<>());
        store.add(playerId, record(playerId, "obelisco"));

        DiscoveryRecord loaded = store.load(playerId).iterator().next();

        assertEquals(WAYSTONE, loaded.type());
        assertEquals(DiscoveryScope.PERSONAL, loaded.scope());
        assertEquals(playerId, loaded.owner());
    }

    @Test
    void jogadorOfflineFalhaFechado() {
        UUID playerId = UUID.randomUUID();
        PaperPlayerDiscoveryStore store = store(playerId, null, new HashMap<>());

        assertThrows(
                IllegalStateException.class,
                () -> store.add(playerId, record(playerId, "obelisco"))
        );
    }

    @Test
    void payloadMalformadoFalhaFechado() {
        UUID playerId = UUID.randomUUID();
        Map<NamespacedKey, Object> data = new HashMap<>();
        data.put(KEY, List.of("waystone"));
        PaperPlayerDiscoveryStore store = store(playerId, playerId, data);

        assertThrows(IllegalStateException.class, () -> store.load(playerId));
    }

    @Test
    void descobertaDeOutroEscopoOuDonoEhRecusada() {
        UUID playerId = UUID.randomUUID();
        PaperPlayerDiscoveryStore store = store(playerId, playerId, new HashMap<>());
        DiscoveryRecord otherOwner = new DiscoveryRecord(
                WAYSTONE,
                new DiscoveryId("obelisco"),
                DiscoveryScope.PERSONAL,
                UUID.randomUUID()
        );
        DiscoveryRecord worldScoped = new DiscoveryRecord(
                WAYSTONE,
                new DiscoveryId("obelisco"),
                DiscoveryScope.WORLD,
                playerId
        );

        assertThrows(IllegalArgumentException.class, () -> store.add(playerId, otherOwner));
        assertThrows(IllegalArgumentException.class, () -> store.add(playerId, worldScoped));
        assertTrue(store.load(playerId).isEmpty());
    }

    private DiscoveryRecord record(UUID playerId, String id) {
        return new DiscoveryRecord(
                WAYSTONE,
                new DiscoveryId(id),
                DiscoveryScope.PERSONAL,
                playerId
        );
    }

    private PaperPlayerDiscoveryStore store(
            UUID playerId,
            UUID online,
            Map<NamespacedKey, Object> data
    ) {
        Player player = online == null
                ? null
                : player(playerId, PersistentDataContainerTestDouble.create(data));
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayer" -> online != null && online.equals(args[0]) ? player : null;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
        return new PaperPlayerDiscoveryStore(server, KEY);
    }

    private Player player(UUID id, PersistentDataContainer container) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> id;
                    case "isOnline" -> true;
                    case "getPersistentDataContainer" -> container;
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
