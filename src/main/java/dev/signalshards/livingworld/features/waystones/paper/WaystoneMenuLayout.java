package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class WaystoneMenuLayout {
    static final int MAX_ENTRIES = 54;

    private WaystoneMenuLayout() {
    }

    static boolean canDisplay(int count) {
        return count >= 0 && count <= MAX_ENTRIES;
    }

    static int inventorySize(int count) {
        if (count < 1 || count > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "O menu suporta entre 1 e " + MAX_ENTRIES + " waystones"
            );
        }
        return ((count + 8) / 9) * 9;
    }

    static List<Waystone> orderDestinations(
            UUID currentWorldId,
            double playerX,
            double playerY,
            double playerZ,
            List<Waystone> waystones
    ) {
        Objects.requireNonNull(currentWorldId, "mundo atual");
        Objects.requireNonNull(waystones, "waystones");

        return waystones.stream()
                .sorted((left, right) -> compareDestinations(
                        currentWorldId,
                        playerX,
                        playerY,
                        playerZ,
                        left,
                        right
                ))
                .toList();
    }

    static long distanceBlocks(
            double playerX,
            double playerY,
            double playerZ,
            Waystone waystone
    ) {
        Objects.requireNonNull(waystone, "waystone");
        return Math.round(Math.sqrt(distanceSquared(
                playerX,
                playerY,
                playerZ,
                waystone
        )));
    }

    private static int compareDestinations(
            UUID currentWorldId,
            double playerX,
            double playerY,
            double playerZ,
            Waystone left,
            Waystone right
    ) {
        boolean leftCurrentWorld = left.worldId().equals(currentWorldId);
        boolean rightCurrentWorld = right.worldId().equals(currentWorldId);
        if (leftCurrentWorld != rightCurrentWorld) {
            return leftCurrentWorld ? -1 : 1;
        }

        if (leftCurrentWorld) {
            int distance = Double.compare(
                    distanceSquared(playerX, playerY, playerZ, left),
                    distanceSquared(playerX, playerY, playerZ, right)
            );
            if (distance != 0) {
                return distance;
            }
        }

        int name = String.CASE_INSENSITIVE_ORDER.compare(
                left.name(),
                right.name()
        );
        if (name != 0) {
            return name;
        }
        return left.id().value().compareTo(right.id().value());
    }

    private static double distanceSquared(
            double playerX,
            double playerY,
            double playerZ,
            Waystone waystone
    ) {
        double dx = playerX - (waystone.x() + 0.5D);
        double dy = playerY - waystone.y();
        double dz = playerZ - (waystone.z() + 0.5D);
        return (dx * dx) + (dy * dy) + (dz * dz);
    }
}
