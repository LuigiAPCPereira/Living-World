package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class WaystoneService {
    private final WaystoneRegistry registry;
    private final WaystoneAccessStore accessStore;

    public WaystoneService(WaystoneRegistry registry, WaystoneAccessStore accessStore) {
        this.registry = Objects.requireNonNull(registry, "registro de waystones");
        this.accessStore = Objects.requireNonNull(accessStore, "acessos de waystones");
    }

    public void register(Waystone waystone) {
        registry.register(Objects.requireNonNull(waystone, "waystone"));
    }

    public Optional<Waystone> find(WaystoneId id) {
        return registry.find(Objects.requireNonNull(id, "waystone"));
    }

    public Optional<Waystone> findAt(UUID worldId, int x, int y, int z) {
        Objects.requireNonNull(worldId, "mundo");
        return registry.findAt(worldId, x, y, z);
    }

    public boolean remove(WaystoneId id) {
        return registry.remove(Objects.requireNonNull(id, "waystone"));
    }

    public List<Waystone> allWaystones() {
        return registry.all();
    }

    public boolean activate(UUID playerId, WaystoneId waystoneId) {
        Objects.requireNonNull(playerId, "jogador");
        Objects.requireNonNull(waystoneId, "waystone");

        if (registry.find(waystoneId).isEmpty()) {
            return false;
        }
        return accessStore.add(playerId, waystoneId);
    }

    public Optional<Waystone> findActivated(UUID playerId, WaystoneId waystoneId) {
        Set<WaystoneId> activated = accessStore.load(playerId);
        if (!activated.contains(waystoneId)) {
            return Optional.empty();
        }
        return registry.find(waystoneId);
    }

    public List<Waystone> activatedWaystones(UUID playerId) {
        Set<WaystoneId> activated = accessStore.load(playerId);
        return registry.all().stream()
                .filter(waystone -> activated.contains(waystone.id()))
                .sorted(Comparator.comparing(Waystone::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
