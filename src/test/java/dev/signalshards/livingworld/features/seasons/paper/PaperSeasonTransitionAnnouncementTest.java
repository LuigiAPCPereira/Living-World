package dev.signalshards.livingworld.features.seasons.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import dev.signalshards.livingworld.features.seasons.domain.SeasonTransition;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperSeasonTransitionAnnouncementTest {
    @Test
    void anunciaUmaVezParaCadaJogadorQuandoHaTransicao() {
        AtomicInteger titles = new AtomicInteger();
        Player first = player(titles);
        Player second = player(titles);
        Server server = server(java.util.List.of(first, second));
        PaperSeasonTransitionAnnouncement announcement =
                new PaperSeasonTransitionAnnouncement(
                        server,
                        MessageCatalog.fromLanguageTag("pt-BR"),
                        true
                );

        announcement.onProgress(new CalendarProgress(
                1,
                new CalendarDate(1, 6, 8),
                new CalendarDate(1, 7, 1),
                Optional.of(new SeasonTransition(
                        Season.VERAO,
                        Season.OUTONO
                ))
        ));

        assertEquals(2, titles.get());
    }

    @Test
    void naoAnunciaSemTransicaoOuQuandoDesligado() {
        AtomicInteger titles = new AtomicInteger();
        Server server = server(java.util.List.of(player(titles)));

        new PaperSeasonTransitionAnnouncement(
                server,
                MessageCatalog.fromLanguageTag("pt-BR"),
                true
        ).onProgress(new CalendarProgress(
                1,
                new CalendarDate(1, 6, 4),
                new CalendarDate(1, 6, 5),
                Optional.empty()
        ));

        new PaperSeasonTransitionAnnouncement(
                server,
                MessageCatalog.fromLanguageTag("pt-BR"),
                false
        ).onProgress(new CalendarProgress(
                1,
                new CalendarDate(1, 6, 8),
                new CalendarDate(1, 7, 1),
                Optional.of(new SeasonTransition(
                        Season.VERAO,
                        Season.OUTONO
                ))
        ));

        assertEquals(0, titles.get());
    }

    private Player player(AtomicInteger titles) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "showTitle" -> {
                        if (args[0] instanceof Title) {
                            titles.incrementAndGet();
                        }
                        yield null;
                    }
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private Server server(Collection<? extends Player> players) {
        return (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getOnlinePlayers" -> players;
                    case "toString" -> "ServerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        if (type == double.class) {
            return 0.0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
