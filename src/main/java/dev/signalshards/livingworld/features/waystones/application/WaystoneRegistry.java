package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaystoneRegistry {
    void register(Waystone waystone);

    Optional<Waystone> find(WaystoneId id);

    Optional<Waystone> findAt(UUID worldId, int x, int y, int z);

    List<Waystone> all();

    boolean remove(WaystoneId id);
}
