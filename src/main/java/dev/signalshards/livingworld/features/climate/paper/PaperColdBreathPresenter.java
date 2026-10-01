package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fallback vanilla mínimo para cold breath.
 */
public final class PaperColdBreathPresenter implements PaperThermalFeedbackPresenter {
    static final int MAX_VIEWERS = 16;
    private static final double MOUTH_FORWARD_BLOCKS = 0.27D;
    private static final double MOUTH_DOWN_BLOCKS = 0.18D;
    private static final double CONE_LATERAL = 0.10D;
    private static final double CONE_VERTICAL = 0.055D;
    private static final double BASE_SPEED = 0.040D;
    private static final double INTENSITY_SPEED = 0.025D;

    @Override
    public void emitBreath(Player subject, double intensity) {
        Objects.requireNonNull(subject, "jogador");
        if (!Double.isFinite(intensity) || intensity <= 0.0D || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade de breath deve ficar em (0,1]"
            );
        }
        if (subject.isInvisible()) {
            return;
        }

        MouthFrame frame = mouthFrame(subject.getEyeLocation());
        int count = particleCount(intensity);
        double speed = BASE_SPEED + (INTENSITY_SPEED * intensity);

        int viewers = 0;
        if (viewerCanSee(subject, subject)) {
            spawn(subject, frame, count, speed);
            viewers++;
        }
        for (Player viewer : subject.getTrackedBy()) {
            if (viewers >= MAX_VIEWERS) {
                break;
            }
            if (viewer.equals(subject) || !viewerCanSee(viewer, subject)) {
                continue;
            }
            spawn(viewer, frame, count, speed);
            viewers++;
        }
    }

    static int particleCount(double intensity) {
        if (!Double.isFinite(intensity) || intensity <= 0.0D || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade deve ficar em (0,1]"
            );
        }
        if (intensity < 0.45D) {
            return 1;
        }
        return intensity < 0.80D ? 2 : 3;
    }

    static boolean viewerCanSee(Player viewer, Player subject) {
        return viewer.isOnline() && viewer.canSee(subject);
    }

    static MouthFrame mouthFrame(Location eye) {
        Objects.requireNonNull(eye, "posição dos olhos");

        Vector forward = eye.getDirection().normalize();
        double yawRadians = Math.toRadians(eye.getYaw());
        Vector lateral = new Vector(
                -Math.cos(yawRadians),
                0.0D,
                -Math.sin(yawRadians)
        ).normalize();
        Vector up = lateral.clone()
                .crossProduct(forward)
                .normalize();

        Location mouth = eye.clone()
                .add(forward.clone().multiply(MOUTH_FORWARD_BLOCKS))
                .add(up.clone().multiply(-MOUTH_DOWN_BLOCKS));

        return new MouthFrame(mouth, forward, lateral, up);
    }

    static Vector coneVelocity(
            MouthFrame frame,
            double lateral,
            double vertical,
            double speed
    ) {
        Objects.requireNonNull(frame, "referencial da boca");
        if (!Double.isFinite(lateral)
                || !Double.isFinite(vertical)
                || !Double.isFinite(speed)
                || speed <= 0.0D) {
            throw new IllegalArgumentException("vetor de cone inválido");
        }

        return frame.forward().clone()
                .add(frame.lateral().clone().multiply(lateral))
                .add(frame.up().clone().multiply(vertical))
                .normalize()
                .multiply(speed);
    }

    private void spawn(
            Player viewer,
            MouthFrame frame,
            int count,
            double speed
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < count; index++) {
            Vector velocity = coneVelocity(
                    frame,
                    random.nextDouble(-CONE_LATERAL, CONE_LATERAL),
                    random.nextDouble(-CONE_VERTICAL, CONE_VERTICAL),
                    speed
            );
            viewer.spawnParticle(
                    Particle.CLOUD,
                    frame.mouth(),
                    0,
                    velocity.getX(),
                    velocity.getY(),
                    velocity.getZ(),
                    1.0D
            );
        }
    }

    record MouthFrame(
            Location mouth,
            Vector forward,
            Vector lateral,
            Vector up
    ) {
        MouthFrame {
            Objects.requireNonNull(mouth, "boca");
            Objects.requireNonNull(forward, "forward");
            Objects.requireNonNull(lateral, "lateral");
            Objects.requireNonNull(up, "up");
        }
    }
}
