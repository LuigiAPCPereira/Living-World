package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Resolve atividade física do jogador usando apenas estado local do próprio Player.
 */
public final class PaperPlayerActivityResolver {
    private static final double WALKING_HORIZONTAL_SPEED_SQUARED = 0.0025D;

    private PaperPlayerActivityResolver() {
    }

    public static PlayerActivity forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        Vector velocity = player.getVelocity();
        double horizontalSpeedSquared =
                (velocity.getX() * velocity.getX()) + (velocity.getZ() * velocity.getZ());
        return resolve(
                player.isSwimming(),
                player.isGliding(),
                player.isClimbing(),
                player.isSprinting(),
                horizontalSpeedSquared
        );
    }

    static PlayerActivity resolve(
            boolean swimming,
            boolean gliding,
            boolean climbing,
            boolean sprinting,
            double horizontalSpeedSquared
    ) {
        if (!Double.isFinite(horizontalSpeedSquared) || horizontalSpeedSquared < 0.0D) {
            throw new IllegalArgumentException("A velocidade horizontal deve ser finita e não negativa");
        }
        if (swimming) {
            return PlayerActivity.SWIMMING;
        }
        if (gliding) {
            return PlayerActivity.GLIDING;
        }
        if (climbing) {
            return PlayerActivity.CLIMBING;
        }
        if (sprinting) {
            return PlayerActivity.SPRINTING;
        }
        if (horizontalSpeedSquared >= WALKING_HORIZONTAL_SPEED_SQUARED) {
            return PlayerActivity.WALKING;
        }
        return PlayerActivity.RESTING;
    }
}
