package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperColdBreathPresenterTest {
    @Test
    void intensidadeControlaContagemBounded() {
        assertEquals(1, PaperColdBreathPresenter.particleCount(0.20D));
        assertEquals(2, PaperColdBreathPresenter.particleCount(0.60D));
        assertEquals(3, PaperColdBreathPresenter.particleCount(0.95D));
    }

    @Test
    void jogadorInvisivelNaoEmiteParticulas() {
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(true, particles);

        new PaperColdBreathPresenter().emitBreath(subject, 0.80D);

        assertEquals(0, particles.get());
    }

    @Test
    void jogadorVisivelRecebeProprioBreath() {
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(false, particles);

        new PaperColdBreathPresenter().emitBreath(subject, 0.80D);

        assertEquals(1, particles.get());
    }

    @Test
    void limitaQuantidadeDeViewersPorEmissao() {
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(false, particles);
        Set<Player> viewers = new LinkedHashSet<>();
        for (int index = 0; index < 40; index++) {
            viewers.add(player(false, particles));
        }
        Player subjectWithViewers = player(false, particles, viewers);

        new PaperColdBreathPresenter().emitBreath(subjectWithViewers, 0.80D);

        assertEquals(PaperColdBreathPresenter.MAX_VIEWERS, particles.get());
    }

    private Player player(boolean invisible, AtomicInteger particles) {
        return player(invisible, particles, Set.of());
    }

    private Player player(
            boolean invisible,
            AtomicInteger particles,
            Set<Player> trackedBy
    ) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isInvisible" -> invisible;
                    case "isOnline", "canSee" -> true;
                    case "getEyeLocation" -> new Location(null, 0.0D, 64.0D, 0.0D);
                    case "getTrackedPlayers", "getTrackedBy" -> trackedBy;
                    case "spawnParticle" -> {
                        particles.incrementAndGet();
                        yield null;
                    }
                    case "toString" -> "FakePlayer";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );
    }
}
