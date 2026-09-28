package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.ResourcePackClientStatus;
import dev.signalshards.livingworld.core.companion.ResourcePackSessionStore;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperResourcePackStatusModuleTest {
    @Test
    void eventoConhecidoAtualizaSessaoEQuitLimpa() {
        AtomicInteger registrations = new AtomicInteger();
        Plugin plugin = plugin(registrations);
        ResourcePackSessionStore store = new ResourcePackSessionStore();
        PaperResourcePackStatusModule module =
                new PaperResourcePackStatusModule(plugin, store);
        UUID playerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        Player player = player(playerId);
        store.requested(playerId, requestId, false);

        module.enable();
        module.onResourcePackStatus(new PlayerResourcePackStatusEvent(
                player,
                requestId,
                PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED
        ));

        assertEquals(1, registrations.get());
        assertEquals(
                ResourcePackClientStatus.LOADED,
                store.session(playerId).orElseThrow().status()
        );

        module.onQuit(new PlayerQuitEvent(
                player,
                (net.kyori.adventure.text.Component) null,
                PlayerQuitEvent.QuitReason.DISCONNECTED
        ));
        assertTrue(store.session(playerId).isEmpty());
    }

    @Test
    void disableLimpaSessoes() {
        ResourcePackSessionStore store = new ResourcePackSessionStore();
        PaperResourcePackStatusModule module =
                new PaperResourcePackStatusModule(
                        plugin(new AtomicInteger()),
                        store
                );
        store.requested(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false
        );

        module.enable();
        module.disable();

        assertEquals(0, store.trackedPlayers());
    }

    private Plugin plugin(AtomicInteger registrations) {
        PluginManager manager = (PluginManager) Proxy.newProxyInstance(
                PluginManager.class.getClassLoader(),
                new Class<?>[]{PluginManager.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("registerEvents")) {
                        registrations.incrementAndGet();
                        return null;
                    }
                    return null;
                }
        );
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPluginManager" -> manager;
                    default -> null;
                }
        );
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServer" -> server;
                    default -> null;
                }
        );
    }

    private Player player(UUID playerId) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "toString" -> "FakePlayer";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );
    }
}
