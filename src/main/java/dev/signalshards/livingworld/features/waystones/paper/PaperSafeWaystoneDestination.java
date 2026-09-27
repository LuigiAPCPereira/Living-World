package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;

public final class PaperSafeWaystoneDestination {
    public Optional<Location> resolveLoaded(World world, Waystone waystone) {
        int feetY = waystone.y() + 1;
        int headY = feetY + 1;

        if (waystone.y() < world.getMinHeight()
                || headY >= world.getMaxHeight()) {
            return Optional.empty();
        }

        Location destination = new Location(
                world,
                waystone.x() + 0.5D,
                feetY,
                waystone.z() + 0.5D
        );
        if (!world.getWorldBorder().isInside(destination)) {
            return Optional.empty();
        }

        Block support = world.getBlockAt(waystone.x(), waystone.y(), waystone.z());
        Block feet = world.getBlockAt(waystone.x(), feetY, waystone.z());
        Block head = world.getBlockAt(waystone.x(), headY, waystone.z());

        if (support.isPassable() || support.isLiquid()) {
            return Optional.empty();
        }
        if (!feet.isPassable() || feet.isLiquid()) {
            return Optional.empty();
        }
        if (!head.isPassable() || head.isLiquid()) {
            return Optional.empty();
        }

        return Optional.of(destination);
    }
}
