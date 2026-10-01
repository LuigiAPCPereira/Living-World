package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperColdBreathPresenterTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void intensidadeControlaContagemBounded() {
        assertEquals(2, PaperColdBreathPresenter.particleCount(0.20D));
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

        assertEquals(3, particles.get());
    }

    @Test
    void bocaSegueReferencialLocalDaCabeca() {
        float[] yaws = {0.0F, 90.0F, 180.0F, -90.0F};
        float[] pitches = {-75.0F, 0.0F, 75.0F};

        for (float yaw : yaws) {
            for (float pitch : pitches) {
                Location eye = new Location(
                        null,
                        10.0D,
                        64.0D,
                        -3.0D,
                        yaw,
                        pitch
                );
                var frame = PaperColdBreathPresenter.mouthFrame(eye);
                Vector offset = frame.mouth()
                        .toVector()
                        .subtract(eye.toVector());

                assertEquals(0.27D, offset.dot(frame.forward()), EPSILON);
                assertEquals(-0.30D, offset.dot(frame.up()), EPSILON);
                assertEquals(0.0D, offset.dot(frame.lateral()), EPSILON);
            }
        }
    }

    @Test
    void coneAbreNaVelocidadeSemMoverOrigem() {
        Location eye = new Location(
                null,
                2.0D,
                70.0D,
                5.0D,
                137.0F,
                -42.0F
        );
        var frame = PaperColdBreathPresenter.mouthFrame(eye);
        Vector velocity = PaperColdBreathPresenter.coneVelocity(
                frame,
                0.10D,
                -0.05D,
                0.06D
        );

        assertEquals(0.06D, velocity.length(), EPSILON);
        assertTrue(velocity.dot(frame.forward()) > 0.05D);
        assertTrue(velocity.dot(frame.lateral()) > 0.0D);
        assertTrue(velocity.dot(frame.up()) < 0.0D);
    }

    @Test
    void todasParticulasNascemNoMesmoPontoDaBocaEModoDirecional() {
        AtomicInteger particles = new AtomicInteger();
        List<Object[]> calls = new ArrayList<>();
        Location eye = new Location(
                null,
                0.0D,
                64.0D,
                0.0D,
                35.0F,
                -20.0F
        );
        Player subject = player(false, particles, Set.of(), eye, calls);

        new PaperColdBreathPresenter().emitBreath(subject, 0.95D);

        Location expected = PaperColdBreathPresenter.mouthFrame(eye).mouth();
        assertEquals(3, calls.size());
        for (Object[] call : calls) {
            assertSame(Particle.DUST, call[0]);
            Location actual = (Location) call[1];
            assertEquals(expected.getX(), actual.getX(), EPSILON);
            assertEquals(expected.getY(), actual.getY(), EPSILON);
            assertEquals(expected.getZ(), actual.getZ(), EPSILON);
            assertEquals(0, call[2]);
            assertEquals(1.0D, (double) call[6], EPSILON);
            assertInstanceOf(Particle.DustOptions.class, call[7]);
        }
    }

    @Test
    void limitaQuantidadeDeViewersPorEmissao() {
        AtomicInteger particles = new AtomicInteger();
        Set<Player> viewers = new LinkedHashSet<>();
        for (int index = 0; index < 40; index++) {
            viewers.add(player(false, particles));
        }
        Player subjectWithViewers = player(false, particles, viewers);

        new PaperColdBreathPresenter().emitBreath(subjectWithViewers, 0.80D);

        assertEquals(
                PaperColdBreathPresenter.MAX_VIEWERS * 3,
                particles.get()
        );
    }

    private Player player(boolean invisible, AtomicInteger particles) {
        return player(invisible, particles, Set.of());
    }

    private Player player(
            boolean invisible,
            AtomicInteger particles,
            Set<Player> trackedBy
    ) {
        return player(
                invisible,
                particles,
                trackedBy,
                new Location(null, 0.0D, 64.0D, 0.0D, 0.0F, 0.0F),
                new ArrayList<>()
        );
    }

    private Player player(
            boolean invisible,
            AtomicInteger particles,
            Set<Player> trackedBy,
            Location eye,
            List<Object[]> particleCalls
    ) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isInvisible" -> invisible;
                    case "isOnline", "canSee" -> true;
                    case "getEyeLocation" -> eye.clone();
                    case "getTrackedPlayers", "getTrackedBy" -> trackedBy;
                    case "spawnParticle" -> {
                        particles.incrementAndGet();
                        particleCalls.add(args.clone());
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
