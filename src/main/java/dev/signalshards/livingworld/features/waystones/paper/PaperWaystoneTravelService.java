package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.application.WaystoneTravelResult;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class PaperWaystoneTravelService {
    private final Server server;
    private final WaystoneService waystones;
    private final PaperSafeWaystoneDestination safeDestination;

    public PaperWaystoneTravelService(
            Server server,
            WaystoneService waystones,
            PaperSafeWaystoneDestination safeDestination
    ) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.safeDestination = Objects.requireNonNull(safeDestination, "destino seguro");
    }

    public CompletableFuture<WaystoneTravelResult> travel(Player player, WaystoneId id) {
        Objects.requireNonNull(player, "jogador");
        Objects.requireNonNull(id, "waystone");

        if (!player.isOnline()) {
            return CompletableFuture.completedFuture(WaystoneTravelResult.PLAYER_OFFLINE);
        }

        Waystone waystone = waystones.findActivated(player.getUniqueId(), id).orElse(null);
        if (waystone == null) {
            return CompletableFuture.completedFuture(WaystoneTravelResult.NOT_ACTIVATED);
        }

        World world = server.getWorld(waystone.worldId());
        if (world == null) {
            return CompletableFuture.completedFuture(WaystoneTravelResult.WORLD_UNAVAILABLE);
        }

        Location anchor = new Location(world, waystone.x(), waystone.y(), waystone.z());
        if (!world.getWorldBorder().isInside(anchor)) {
            return CompletableFuture.completedFuture(WaystoneTravelResult.DESTINATION_UNSAFE);
        }

        return world.getChunkAtAsync(anchor, true).thenCompose(ignored -> {
            if (!player.isOnline()) {
                return CompletableFuture.completedFuture(WaystoneTravelResult.PLAYER_OFFLINE);
            }

            Location destination = safeDestination.resolveLoaded(world, waystone).orElse(null);
            if (destination == null) {
                return CompletableFuture.completedFuture(
                        WaystoneTravelResult.DESTINATION_UNSAFE
                );
            }

            return player.teleportAsync(
                    destination,
                    PlayerTeleportEvent.TeleportCause.PLUGIN
            ).thenApply(success -> success
                    ? WaystoneTravelResult.SUCCESS
                    : WaystoneTravelResult.TELEPORT_REJECTED
            );
        });
    }
}
