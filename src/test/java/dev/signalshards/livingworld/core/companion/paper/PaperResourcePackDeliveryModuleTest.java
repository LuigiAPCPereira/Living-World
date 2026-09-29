package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackKind;
import dev.signalshards.livingworld.core.companion.CompanionPackManifest;
import dev.signalshards.livingworld.core.companion.ResourcePackSessionStore;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperResourcePackDeliveryModuleTest {
    private static final String SHA1 = "a".repeat(40);

    @Test
    void manifestCompativelSolicitaPackAOnlineERegistraRequest() {
        Fixture f = new Fixture(false, true);

        f.module.enable();

        assertEquals(1, f.registrations.get());
        assertEquals(1, f.requests.get());
        assertEquals(
                f.requestId,
                f.sessions.session(f.playerId).orElseThrow().requestId()
        );
        assertEquals(
                1L,
                f.performance.snapshot().value(
                        EnvironmentalMetric.RESOURCE_PACK_REQUESTS
                )
        );
    }

    @Test
    void joinPosteriorRecebeMesmoContratoComNovoRequest() {
        Fixture f = new Fixture(false, true);
        f.module.enable();
        UUID secondPlayerId = UUID.randomUUID();
        Player second = f.player(secondPlayerId);

        f.module.onJoin(new PlayerJoinEvent(
                second,
                net.kyori.adventure.text.Component.text("join")
        ));

        assertEquals(2, f.requests.get());
        assertTrue(f.sessions.session(secondPlayerId).isPresent());
    }

    @Test
    void manifestAusenteOpcionalNaoRegistraNemEnvia() {
        Fixture f = new Fixture(false, false);

        f.module.enable();

        assertEquals(0, f.registrations.get());
        assertEquals(0, f.requests.get());
        assertEquals(0, f.sessions.trackedPlayers());
    }

    @Test
    void manifestAusenteObrigatorioFalhaEnable() {
        Fixture f = new Fixture(true, false);

        assertThrows(IllegalStateException.class, f.module::enable);
    }

    private static final class Fixture {
        final AtomicInteger registrations = new AtomicInteger();
        final AtomicInteger requests = new AtomicInteger();
        final UUID playerId = UUID.randomUUID();
        final UUID requestId = UUID.fromString(
                "c3c5bd6b-6528-450d-9435-0710685ab8e1"
        );
        final ResourcePackSessionStore sessions = new ResourcePackSessionStore();
        final EnvironmentalPerformanceMetrics performance =
                new EnvironmentalPerformanceMetrics();
        final Player onlinePlayer = player(playerId);
        final PaperResourcePackDeliveryModule module;

        Fixture(boolean required, boolean manifestPresent) {
            Plugin plugin = plugin(List.of(onlinePlayer));
            PaperResourcePackDeliverySettings settings =
                    new PaperResourcePackDeliverySettings(
                            true,
                            "https://example.invalid/living-world.zip",
                            SHA1,
                            required,
                            "Living World visuals"
                    );
            module = new PaperResourcePackDeliveryModule(
                    plugin,
                    settings,
                    sessions,
                    () -> manifestPresent
                            ? Optional.of(
                            CompanionPackManifest.withoutHash(
                                    CompanionPackKind.RESOURCE_PACK,
                                    "1.0.0",
                                    1
                            )
                    )
                           : Optional.empty(),
                    new dev.signalshards.livingworld.core.companion.CompanionPackContractPolicy(),
                    () -> requestId,
                    performance
            );
        }

        Player player(UUID id) {
            return (Player) Proxy.newProxyInstance(
                    Player.class.getClassLoader(),
                    new Class<?>[]{Player.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getUniqueId" -> id;
                        case "sendResourcePacks" -> {
                            ResourcePackRequest request =
                                    (ResourcePackRequest) args[0];
                            assertEquals(1, request.packs().size());
                            assertEquals(
                                    "https://example.invalid/living-world.zip",
                                    request.packs().getFirst().uri().toString()
                            );
                            requests.incrementAndGet();
                            yield null;
                        }
                        case "toString" -> "FakePlayer";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> null;
                    }
            );
        }

        private Plugin plugin(Collection<? extends Player> players) {
            PluginManager manager = (PluginManager) Proxy.newProxyInstance(
                    PluginManager.class.getClassLoader(),
                    new Class<?>[]{PluginManager.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("registerEvents")) {
                            registrations.incrementAndGet();
                        }
                        return null;
                    }
            );
            Server server = (Server) Proxy.newProxyInstance(
                    Server.class.getClassLoader(),
                    new Class<?>[]{Server.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getPluginManager" -> manager;
                        case "getOnlinePlayers" -> players;
                        default -> null;
                    }
            );
            return (Plugin) Proxy.newProxyInstance(
                    Plugin.class.getClassLoader(),
                    new Class<?>[]{Plugin.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getServer" -> server;
                        case "getLogger" -> Logger.getLogger(
                                "PaperResourcePackDeliveryModuleTest"
                        );
                        default -> null;
                    }
            );
        }
    }
}
