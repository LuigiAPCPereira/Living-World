package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.List;
import java.util.Optional;

public interface WaystoneRegistry {
    void register(Waystone waystone);

    Optional<Waystone> find(WaystoneId id);

    List<Waystone> all();

    boolean remove(WaystoneId id);
}
