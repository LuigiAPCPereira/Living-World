package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Frost visual vanilla baseado em freeze ticks + microtremor corporal.
 *
 * <p>O presenter nunca bloqueia freeze ticks, nunca atinge o máximo vanilla e
 * não assume ownership quando já existe congelamento real/externo.</p>
 */
public final class PaperVanillaFrostPresenter
        implements PaperFrostFeedbackPresenter {
    private static final double BASE_VISUAL_FREEZE_FRACTION = 0.15D;
    private static final double VISUAL_FREEZE_RANGE = 0.50D;
    private static final double SHIVER_START_INTENSITY = 0.45D;
    private static final float MIN_SHIVER_DEGREES = 1.50F;
    private static final float MAX_SHIVER_DEGREES = 4.00F;

    private final Map<UUID, FrostOwnership> ownershipByPlayer = new HashMap<>();

    @Override
    public void presentFrost(Player player, double intensity) {
        Objects.requireNonNull(player, "jogador");
        validateIntensity(intensity);

        UUID playerId = player.getUniqueId();
        FrostOwnership ownership = ownershipByPlayer.get(playerId);

        if (player.isInPowderedSnow()) {
            clearFrost(player);
            return;
        }

        int currentFreezeTicks = player.getFreezeTicks();
        if (ownership == null) {
            if (currentFreezeTicks > 0) {
                return;
            }
            ownership = new FrostOwnership(0, true);
        } else if (currentFreezeTicks > ownership.lastAppliedFreezeTicks()) {
            ownershipByPlayer.remove(playerId);
            player.setBodyYaw(player.getLocation().getYaw());
            return;
        }

        int targetFreezeTicks = visualFreezeTicks(
                player.getMaxFreezeTicks(),
                intensity
        );
        player.setFreezeTicks(targetFreezeTicks);

        float viewYaw = player.getLocation().getYaw();
        float shiver = shiverOffsetDegrees(
                intensity,
                ownership.positivePhase()
        );
        player.setBodyYaw(viewYaw + shiver);

        ownershipByPlayer.put(
                playerId,
                new FrostOwnership(
                        targetFreezeTicks,
                        !ownership.positivePhase()
                )
        );
    }

    @Override
    public void clearFrost(Player player) {
        Objects.requireNonNull(player, "jogador");
        FrostOwnership ownership = ownershipByPlayer.remove(player.getUniqueId());
        if (ownership == null) {
            return;
        }

        if (player.getFreezeTicks() <= ownership.lastAppliedFreezeTicks()) {
            player.setFreezeTicks(0);
        }
        player.setBodyYaw(player.getLocation().getYaw());
    }

    static int visualFreezeTicks(int maxFreezeTicks, double intensity) {
        validateUnitIntensity(intensity);
        if (maxFreezeTicks <= 1 || intensity <= 0.0D) {
            return 0;
        }

        double fraction = BASE_VISUAL_FREEZE_FRACTION
                + (VISUAL_FREEZE_RANGE * intensity);
        int ticks = (int) Math.round(maxFreezeTicks * fraction);
        return Math.clamp(ticks, 1, maxFreezeTicks - 1);
    }

    static float shiverOffsetDegrees(
            double intensity,
            boolean positivePhase
    ) {
        validateUnitIntensity(intensity);
        if (intensity < SHIVER_START_INTENSITY) {
            return 0.0F;
        }

        double normalized = (intensity - SHIVER_START_INTENSITY)
                / (1.0D - SHIVER_START_INTENSITY);
        float amplitude = (float) (
                MIN_SHIVER_DEGREES
                        + ((MAX_SHIVER_DEGREES - MIN_SHIVER_DEGREES)
                        * normalized)
        );
        return positivePhase ? amplitude : -amplitude;
    }

    private static void validateIntensity(double intensity) {
        if (!Double.isFinite(intensity)
                || intensity <= 0.0D
                || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade de frost deve ficar em (0,1]"
            );
        }
    }

    private static void validateUnitIntensity(double intensity) {
        if (!Double.isFinite(intensity)
                || intensity < 0.0D
                || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade deve ficar entre 0 e 1"
            );
        }
    }

    private record FrostOwnership(
            int lastAppliedFreezeTicks,
            boolean positivePhase
    ) {
    }
}
