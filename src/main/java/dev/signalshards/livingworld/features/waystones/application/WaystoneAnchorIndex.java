package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class WaystoneAnchorIndex {
    private final Map<ChunkKey, List<Waystone>> byChunk = new HashMap<>();

    public void rebuild(Collection<Waystone> waystones) {
        Objects.requireNonNull(waystones, "waystones");
        byChunk.clear();
        waystones.forEach(this::add);
    }

    public void add(Waystone waystone) {
        Objects.requireNonNull(waystone, "waystone");
        byChunk.computeIfAbsent(keyFor(waystone), ignored -> new ArrayList<>())
                .add(waystone);
    }

    public boolean remove(WaystoneId id) {
        Objects.requireNonNull(id, "waystone");
        boolean removed = false;

        for (var iterator = byChunk.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            if (entry.getValue().removeIf(waystone -> waystone.id().equals(id))) {
                removed = true;
            }
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    public List<Waystone> inChunk(UUID worldId, int chunkX, int chunkZ) {
        Objects.requireNonNull(worldId, "mundo");
        return List.copyOf(byChunk.getOrDefault(
                new ChunkKey(worldId, chunkX, chunkZ),
                List.of()
        ));
    }

    public static int chunkCoordinate(int blockCoordinate) {
        return Math.floorDiv(blockCoordinate, 16);
    }

    private ChunkKey keyFor(Waystone waystone) {
        return new ChunkKey(
                waystone.worldId(),
                chunkCoordinate(waystone.x()),
                chunkCoordinate(waystone.z())
        );
    }

    private record ChunkKey(UUID worldId, int chunkX, int chunkZ) {
    }
}
