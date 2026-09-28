package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Fallback vanilla mínimo para cold breath.
 */
public final class PaperColdBreathPresenter implements PaperThermalFeedbackPresenter {
    static final int MAX_VIEWERS = 16;

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

        Location origin = mouthLocation(subject);
        int count = particleCount(intensity);
        double spread = 0.012D + (0.018D * intensity);
        double speed = 0.002D + (0.006D * intensity);

        int viewers = 0;
        if (viewerCanSee(subject, subject)) {
            spawn(subject, origin, count, spread, speed);
            viewers++;
        }
        for (Player viewer : subject.getTrackedBy()) {
            if (viewers >= MAX_VIEWERS) {
                break;
            }
            if (viewer.equals(subject) || !viewerCanSee(viewer, subject)) {
                continue;
            }
            spawn(viewer, origin, count, spread, speed);
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

    private Location mouthLocation(Player player) {
        Location eye = player.getEyeLocation().clone();
        Vector forward = eye.getDirection().multiply(0.34D);
        return eye.add(forward).add(0.0D, -0.12D, 0.0D);
    }

    private void spawn(
            Player viewer,
            Location origin,
            int count,
            double spread,
            double speed
    ) {
        viewer.spawnParticle(
                Particle.CLOUD,
                origin,
                count,
                spread,
                spread * 0.55D,
                spread,
                speed
        );
    }
}
