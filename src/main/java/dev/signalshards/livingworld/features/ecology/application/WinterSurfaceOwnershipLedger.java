package dev.signalshards.livingworld.features.ecology.application;

import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ownership persistível de blocos físicos criados pelo Living World.
 */
public final class WinterSurfaceOwnershipLedger {
    private final Map<WinterSurfacePosition, WinterSurfaceKind> owned;
    private final int maxEntries;
    private boolean dirty;

    public WinterSurfaceOwnershipLedger(
            Map<WinterSurfacePosition, WinterSurfaceKind> owned,
            int maxEntries
    ) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException(
                    "O limite de ownership deve ser positivo"
            );
        }
        Objects.requireNonNull(owned, "ownership inicial");
        if (owned.size() > maxEntries) {
            throw new IllegalArgumentException(
                    "Ownership inicial excede o limite do chunk"
            );
        }
        this.owned = new HashMap<>(owned);
        this.maxEntries = maxEntries;
    }

    public static WinterSurfaceOwnershipLedger empty(int maxEntries) {
        return new WinterSurfaceOwnershipLedger(Map.of(), maxEntries);
    }

    public boolean claim(
            WinterSurfacePosition position,
            WinterSurfaceKind kind
    ) {
        Objects.requireNonNull(position, "posição");
        Objects.requireNonNull(kind, "tipo");
        WinterSurfaceKind current = owned.get(position);
        if (current == kind) {
            return false;
        }
        if (current == null && owned.size() >= maxEntries) {
            return false;
        }
        owned.put(position, kind);
        dirty = true;
        return true;
    }

    public boolean release(WinterSurfacePosition position) {
        Objects.requireNonNull(position, "posição");
        if (owned.remove(position) == null) {
            return false;
        }
        dirty = true;
        return true;
    }

    public Optional<WinterSurfaceKind> kindAt(
            WinterSurfacePosition position
    ) {
        return Optional.ofNullable(owned.get(
                Objects.requireNonNull(position, "posição")
        ));
    }

    public Map<WinterSurfacePosition, WinterSurfaceKind> snapshot() {
        return Map.copyOf(owned);
    }

    public int size() {
        return owned.size();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        dirty = false;
    }
}
