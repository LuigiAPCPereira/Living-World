package dev.signalshards.livingworld.features.hud.domain;

public final class HeadingPolicy {
    private static final CardinalDirection[] YAW_ORDER = {
            CardinalDirection.SUL,
            CardinalDirection.SUDOESTE,
            CardinalDirection.OESTE,
            CardinalDirection.NOROESTE,
            CardinalDirection.NORTE,
            CardinalDirection.NORDESTE,
            CardinalDirection.LESTE,
            CardinalDirection.SUDESTE
    };

    public CardinalDirection directionFor(float yaw) {
        if (!Float.isFinite(yaw)) {
            throw new IllegalArgumentException("O yaw deve ser finito");
        }

        int index = Math.floorMod((int) Math.floor((yaw + 22.5F) / 45.0F), 8);
        return YAW_ORDER[index];
    }
}
