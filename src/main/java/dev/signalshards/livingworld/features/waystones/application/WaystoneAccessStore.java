package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.Set;
import java.util.UUID;

public interface WaystoneAccessStore {
    Set<WaystoneId> load(UUID playerId);

    boolean add(UUID playerId, WaystoneId waystoneId);
}
