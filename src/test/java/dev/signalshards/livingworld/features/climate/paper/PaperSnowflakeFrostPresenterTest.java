package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperSnowflakeFrostPresenterTest {
    @Test
    void intensidadeControlaParticulasBounded() {
        assertEquals(1, PaperSnowflakeFrostPresenter.particleCount(0.20D));
        assertEquals(2, PaperSnowflakeFrostPresenter.particleCount(0.60D));
        assertEquals(3, PaperSnowflakeFrostPresenter.particleCount(0.90D));
    }

    @Test
    void apresentaSomenteAoProprioJogador() {
        AtomicInteger emissions = new AtomicInteger();
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getEyeLocation" -> new Location(
                            null,
                            0.0D,
                            64.0D,
                            0.0D
                    );
                    case "spawnParticle" -> {
                        emissions.incrementAndGet();
                        yield null;
                    }
                    case "toString" -> "FakePlayer";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );

        new PaperSnowflakeFrostPresenter().presentFrost(player, 0.80D);

        assertEquals(1, emissions.get());
    }
}
