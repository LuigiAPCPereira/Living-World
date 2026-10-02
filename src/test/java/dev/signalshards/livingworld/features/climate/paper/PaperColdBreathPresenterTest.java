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
    void respiracaoUsaTresMicroPuffsEmZeroDoisQuatroTicks() {
        RecordingScheduler scheduler = new RecordingScheduler();
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(false, particles);

        new PaperColdBreathPresenter(scheduler).emitBreath(subject, 0.60D);

        assertEquals(List.of(0L, 2L, 4L), scheduler.delays());
        assertEquals(1, particles.get());

        scheduler.runDelay(2L);
        assertEquals(2, particles.get());

        scheduler.runDelay(4L);
        assertEquals(3, particles.get());
    }

    @Test
    void jogadorInvisivelNaoEmiteParticulas() {
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(true, particles);

        new PaperColdBreathPresenter(new RecordingScheduler())
                .emitBreath(subject, 0.80D);

        assertEquals(0, particles.get());
    }

    @Test
    void jogadorVisivelRecebeProprioBreath() {
        AtomicInteger particles = new AtomicInteger();
        Player subject = player(false, particles);

        RecordingScheduler scheduler = new RecordingScheduler();
        new PaperColdBreathPresenter(scheduler).emitBreath(subject, 0.80D);
        scheduler.runAllDelayed();

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
    void microPuffsAvancamSomenteParaFrenteECrescem() {
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
        RecordingScheduler scheduler = new RecordingScheduler();

        new PaperColdBreathPresenter(scheduler).emitBreath(subject, 0.95D);
        scheduler.runAllDelayed();

        var frame = PaperColdBreathPresenter.mouthFrame(eye);
        Location mouth = frame.mouth();
        assertEquals(3, calls.size());
        double[] forwardOffsets = {0.0D, 0.08D, 0.16D};
        float[] dustSizes = {0.8F, 1.1F, 1.4F};
        for (int index = 0; index < calls.size(); index++) {
            Object[] call = calls.get(index);
            assertSame(Particle.DUST, call[0]);
            Location actual = (Location) call[1];
            Vector offset = actual.toVector().subtract(mouth.toVector());
            assertEquals(
                    forwardOffsets[index],
                    offset.dot(frame.forward()),
                    EPSILON
            );
            assertEquals(0.0D, offset.dot(frame.lateral()), EPSILON);
            assertEquals(0.0D, offset.dot(frame.up()), EPSILON);
            assertEquals(0, call[2]);
            assertEquals(1.0D, (double) call[6], EPSILON);
            Particle.DustOptions dust = assertInstanceOf(
                    Particle.DustOptions.class,
                    call[7]
            );
            assertEquals(dustSizes[index], dust.getSize(), 1.0E-6F);
        }
    }

    @Test
    void microPuffsGanhamSubidaProgressivaSemPerderForward() {
        Location eye = new Location(
                null,
                2.0D,
                70.0D,
                5.0D,
                0.0F,
                0.0F
        );
        var frame = PaperColdBreathPresenter.mouthFrame(eye);
        var plans = PaperColdBreathPresenter.puffPlans();

        Vector first = PaperColdBreathPresenter.puffVelocity(
                frame,
                plans.get(0),
                0.0D,
                0.0D,
                0.06D
        );
        Vector second = PaperColdBreathPresenter.puffVelocity(
                frame,
                plans.get(1),
                0.0D,
                0.0D,
                0.06D
        );
        Vector third = PaperColdBreathPresenter.puffVelocity(
                frame,
                plans.get(2),
                0.0D,
                0.0D,
                0.06D
        );

        assertTrue(first.dot(frame.forward()) > 0.0D);
        assertTrue(second.dot(frame.forward()) > 0.0D);
        assertTrue(third.dot(frame.forward()) > 0.0D);
        assertTrue(first.dot(frame.up()) < second.dot(frame.up()));
        assertTrue(second.dot(frame.up()) < third.dot(frame.up()));
    }

    @Test
    void limitaQuantidadeDeViewersPorEmissao() {
        AtomicInteger particles = new AtomicInteger();
        Set<Player> viewers = new LinkedHashSet<>();
        for (int index = 0; index < 40; index++) {
            viewers.add(player(false, particles));
        }
        Player subjectWithViewers = player(false, particles, viewers);

        RecordingScheduler scheduler = new RecordingScheduler();
        new PaperColdBreathPresenter(scheduler)
                .emitBreath(subjectWithViewers, 0.80D);
        scheduler.runAllDelayed();

        assertEquals(
                PaperColdBreathPresenter.MAX_VIEWERS * 3,
                particles.get()
        );
    }

    private static final class RecordingScheduler
            implements PaperColdBreathScheduler {
        private final List<ScheduledAction> actions = new ArrayList<>();

        @Override
        public void schedule(long delayTicks, Runnable action) {
            actions.add(new ScheduledAction(delayTicks, action));
            if (delayTicks == 0L) {
                action.run();
            }
        }

        List<Long> delays() {
            return actions.stream()
                    .map(ScheduledAction::delayTicks)
                    .toList();
        }

        void runDelay(long delayTicks) {
            actions.stream()
                    .filter(action -> action.delayTicks() == delayTicks)
                    .forEach(action -> action.action().run());
        }

        void runAllDelayed() {
            actions.stream()
                    .filter(action -> action.delayTicks() > 0L)
                    .forEach(action -> action.action().run());
        }
    }

    private record ScheduledAction(long delayTicks, Runnable action) {
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
