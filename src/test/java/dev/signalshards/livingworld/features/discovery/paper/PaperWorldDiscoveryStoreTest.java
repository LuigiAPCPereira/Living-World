package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.testsupport.PersistentDataContainerTestDouble;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWorldDiscoveryStoreTest {
    private static final DiscoveryType LANDMARK = new DiscoveryType("landmark");
    private static final NamespacedKey KEY = new NamespacedKey("livingworld", "discovery_learned");

    @Test
    void persistEDescobertasDoDonoDoMundo() {
        UUID worldId = UUID.randomUUID();
        PaperWorldDiscoveryStore store = store(worldId, worldId, new HashMap<>());
        DiscoveryRecord record = record(worldId, "vila-antiga");

        assertTrue(store.add(worldId, record));
        assertFalse(store.add(worldId, record));

        DiscoveryRecord loaded = store.load(worldId).iterator().next();
        assertEquals(LANDMARK, loaded.type());
        assertEquals(DiscoveryScope.WORLD, loaded.scope());
        assertEquals(worldId, loaded.owner());
    }

    @Test
    void cadaMundoTemAMemoriaDele() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        PaperWorldDiscoveryStore store = store(first, first, new HashMap<>());
        store.add(first, record(first, "vila-antiga"));

        assertEquals(1, store.load(first).size());
        assertTrue(store.load(second).isEmpty());
    }

    @Test
    void mundoNaoCarregadoNaoAceitaGravacao() {
        UUID worldId = UUID.randomUUID();
        PaperWorldDiscoveryStore store = store(worldId, null, new HashMap<>());

        assertThrows(
                IllegalStateException.class,
                () -> store.add(worldId, record(worldId, "vila-antiga"))
        );
    }

    @Test
    void leituraDeMundoNaoCarregadoDevolveVazio() {
        UUID worldId = UUID.randomUUID();
        PaperWorldDiscoveryStore store = store(worldId, null, new HashMap<>());

        assertTrue(store.load(worldId).isEmpty());
    }

    @Test
    void descobertaPessoalEhRecusadaPeloArmazenamentoDoMundo() {
        UUID worldId = UUID.randomUUID();
        PaperWorldDiscoveryStore store = store(worldId, worldId, new HashMap<>());
        DiscoveryRecord personal = new DiscoveryRecord(
                LANDMARK,
                new DiscoveryId("vila-antiga"),
                DiscoveryScope.PERSONAL,
                worldId
        );

        assertThrows(IllegalArgumentException.class, () -> store.add(worldId, personal));
        assertTrue(store.load(worldId).isEmpty());
    }

    private DiscoveryRecord record(UUID worldId, String id) {
        return new DiscoveryRecord(
                LANDMARK,
                new DiscoveryId(id),
                DiscoveryScope.WORLD,
                worldId
        );
    }

    private PaperWorldDiscoveryStore store(
            UUID requested,
            UUID loaded,
            Map<NamespacedKey, Object> data
    ) {
        World world = loaded == null ? null : world(loaded, PersistentDataContainerTestDouble.create(data));
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world != null && world.getUID().equals(args[0]) ? world : null;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
        assertTrue(requested != null);
        return new PaperWorldDiscoveryStore(server, KEY);
    }

    private World world(UUID id, PersistentDataContainer container) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> id;
                    case "getPersistentDataContainer" -> container;
                    case "toString" -> "WorldFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
