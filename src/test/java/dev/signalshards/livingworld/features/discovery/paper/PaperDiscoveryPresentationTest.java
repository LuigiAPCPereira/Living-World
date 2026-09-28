package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryTypeDefinition;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.features.discovery.presentation.DiscoveryPresentationPolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperDiscoveryPresentationTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");

    @Test
    void mostraOTituloLocalizadoParaQuemDescobriu() {
        UUID playerId = UUID.randomUUID();
        List<Title> titles = new ArrayList<>();
        PaperDiscoveryPresentation presentation = presentation(playerId, titles);

        presentation.onDiscovery(unlocked(playerId));

        assertEquals(1, titles.size());
        assertEquals(
                Component.text("Descoberto", NamedTextColor.GOLD),
                titles.get(0).title()
        );
        assertEquals(
                Component.text("Portal da Savana • Waystone", NamedTextColor.GRAY),
                titles.get(0).subtitle()
        );
    }

    @Test
    void naoMostraNadaParaQuemNaoEstaMaisNoServidor() {
        List<Title> titles = new ArrayList<>();
        PaperDiscoveryPresentation presentation = presentation(null, titles);

        presentation.onDiscovery(unlocked(UUID.randomUUID()));

        assertTrue(titles.isEmpty());
    }

    private PaperDiscoveryPresentation presentation(UUID online, List<Title> titles) {
        return new PaperDiscoveryPresentation(
                server(online, titles),
                new DiscoveryPresentationPolicy(
                        MessageCatalog.fromLanguageTag("pt-BR"),
                        new DiscoveryRegistry(List.of(
                                new DiscoveryTypeDefinition(
                                        WAYSTONE,
                                        DiscoveryScope.PERSONAL,
                                        "discovery.type.waystone"
                                )
                        ))
                )
        );
    }

    private DiscoveryUnlocked unlocked(UUID playerId) {
        return new DiscoveryUnlocked(
                new DiscoveryRecord(
                        WAYSTONE,
                        new DiscoveryId("waystone-1"),
                        DiscoveryScope.PERSONAL,
                        playerId
                ),
                playerId,
                "Portal da Savana"
        );
    }

    private Server server(UUID online, List<Title> titles) {
        Player player = online == null ? null : player(online, titles);
        return (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayer" -> player != null && player.getUniqueId().equals(args[0]) ? player : null;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private Player player(UUID id, List<Title> titles) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> id;
                    case "showTitle" -> {
                        titles.add((Title) args[0]);
                        yield null;
                    }
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
