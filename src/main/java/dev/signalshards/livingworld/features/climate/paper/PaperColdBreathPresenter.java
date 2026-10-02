package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cold breath vanilla em três micro-puffs temporais e bounded.
 */
public final class PaperColdBreathPresenter implements PaperThermalFeedbackPresenter {
    static final int MAX_VIEWERS = 16;
    private static final double MOUTH_FORWARD_BLOCKS = 0.27D;
    private static final double MOUTH_DOWN_BLOCKS = 0.30D;
    private static final double CONE_LATERAL = 0.018D;
    private static final double CONE_VERTICAL = 0.008D;
    private static final double BASE_SPEED = 0.040D;
    private static final double INTENSITY_SPEED = 0.025D;
    private static final List<BreathPuffPlan> PUFF_PLANS = List.of(
            new BreathPuffPlan(
                    0L,
                    0.00D,
                    0.80F,
                    1.00D,
                    0.00D
            ),
            new BreathPuffPlan(
                    2L,
                    0.08D,
                    1.10F,
                    0.92D,
                    0.06D
            ),
            new BreathPuffPlan(
                    4L,
                    0.16D,
                    1.40F,
                    0.82D,
                    0.14D
            )
    );

    private final PaperColdBreathScheduler scheduler;

    public PaperColdBreathPresenter(PaperColdBreathScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(
                scheduler,
                "scheduler de cold breath"
        );
    }

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

        for (BreathPuffPlan plan : PUFF_PLANS) {
            scheduler.schedule(
                    plan.delayTicks(),
                    () -> emitPuff(subject, intensity, plan)
            );
        }
    }

    static List<BreathPuffPlan> puffPlans() {
        return PUFF_PLANS;
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

    static Vector puffVelocity(
            MouthFrame frame,
            BreathPuffPlan plan,
            double lateralJitter,
            double verticalJitter,
            double speed
    ) {
        Objects.requireNonNull(plan, "plano do puff");
        return coneVelocity(
                frame,
                lateralJitter,
                plan.upwardBias() + verticalJitter,
                speed * plan.speedMultiplier()
        );
    }

    private void emitPuff(
            Player subject,
            double intensity,
            BreathPuffPlan plan
    ) {
        if (!subject.isOnline() || subject.isInvisible()) {
            return;
        }

        MouthFrame frame = mouthFrame(subject.getEyeLocation());
        Location origin = frame.mouth().clone().add(
                frame.forward().clone().multiply(
                        plan.forwardOriginOffsetBlocks()
                )
        );
        double speed = BASE_SPEED + (INTENSITY_SPEED * intensity);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vector velocity = puffVelocity(
                frame,
                plan,
                random.nextDouble(-CONE_LATERAL, CONE_LATERAL),
                random.nextDouble(-CONE_VERTICAL, CONE_VERTICAL),
                speed
        );
        Particle.DustOptions dust = new Particle.DustOptions(
                Color.fromRGB(255, 255, 255),
                plan.dustSize()
        );

        int viewers = 0;
        if (viewerCanSee(subject, subject)) {
            spawn(subject, origin, velocity, dust);
            viewers++;
        }
        for (Player viewer : subject.getTrackedBy()) {
            if (viewers >= MAX_VIEWERS) {
                break;
            }
            if (viewer.equals(subject) || !viewerCanSee(viewer, subject)) {
                continue;
            }
            spawn(viewer, origin, velocity, dust);
            viewers++;
        }
    }

    private void spawn(
            Player viewer,
            Location origin,
            Vector velocity,
            Particle.DustOptions dust
    ) {
        viewer.spawnParticle(
                Particle.DUST,
                origin,
                0,
                velocity.getX(),
                velocity.getY(),
                velocity.getZ(),
                1.0D,
                dust
        );
    }

    record BreathPuffPlan(
            long delayTicks,
            double forwardOriginOffsetBlocks,
            float dustSize,
            double speedMultiplier,
            double upwardBias
    ) {
        BreathPuffPlan {
            if (delayTicks < 0L
                    || !Double.isFinite(forwardOriginOffsetBlocks)
                    || forwardOriginOffsetBlocks < 0.0D
                    || !Float.isFinite(dustSize)
                    || dustSize <= 0.0F
                    || !Double.isFinite(speedMultiplier)
                    || speedMultiplier <= 0.0D
                    || !Double.isFinite(upwardBias)
                    || upwardBias < 0.0D) {
                throw new IllegalArgumentException(
                        "Plano de micro-puff inválido"
                );
            }
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
