package dev.signalshards.livingworld.features.waystones.domain;

import java.util.Objects;
import java.util.UUID;

public record WaystoneId(UUID value) {
    public WaystoneId {
        Objects.requireNonNull(value, "identificador da waystone");
    }

    public static WaystoneId random() {
        return new WaystoneId(UUID.randomUUID());
    }
}
