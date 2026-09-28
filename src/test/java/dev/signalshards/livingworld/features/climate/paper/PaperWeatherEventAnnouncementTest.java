package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlan;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperWeatherEventAnnouncementTest {
    @Test
    void toggleDesligadoNaoEnviaMensagem() {
        AtomicInteger messages = new AtomicInteger();
        Player player = player(messages);
        World world = world(player);

        new PaperWeatherEventAnnouncement(
                MessageCatalog.fromLanguageTag("pt-BR"),
                false
        ).announce(
                world,
                new WeatherEventPlan(
                        WeatherTendency.PRECIPITACAO,
                        Duration.ofMinutes(5)
                )
        );

        assertEquals(0, messages.get());
    }

    private Player player(AtomicInteger messages) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> {
                        messages.incrementAndGet();
                        yield null;
                    }
                    case "toString" -> "PlayerFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private World world(Player player) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayers" -> List.of(player);
                    case "toString" -> "WorldFake";
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
