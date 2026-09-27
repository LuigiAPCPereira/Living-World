package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPlayerWaystoneAccessStoreTest {
    @Test
    void persisteDescobertasNoJogadorOnline() {
        UUID playerId = UUID.randomUUID();
        Map<NamespacedKey, Object> data = new HashMap<>();
        PersistentDataContainer pdc = PersistentDataContainerTestDouble.create(data);
        Player player = player(playerId, pdc);
        Server server = server(playerId, player);
        NamespacedKey key = new NamespacedKey("livingworld", "waystone_activations");
        PaperPlayerWaystoneAccessStore store = new PaperPlayerWaystoneAccessStore(server, key);
        WaystoneId first = WaystoneId.random();
        WaystoneId second = WaystoneId.random();

        assertTrue(store.add(playerId, first));
        assertTrue(store.add(playerId, second));
        assertFalse(store.add(playerId, first));
        assertEquals(2, store.load(playerId).size());
        assertTrue(store.load(playerId).contains(first));
        assertTrue(store.load(playerId).contains(second));
    }

    private Server server(UUID playerId, Player player) {
        return (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayer" -> playerId.equals(args[0]) ? player : null;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Player player(UUID id, PersistentDataContainer pdc) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> id;
                    case "isOnline" -> true;
                    case "getPersistentDataContainer" -> pdc;
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
