package dev.signalshards.livingworld.features.waystones.domain;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public record WaystoneId(UUID value) {
    public WaystoneId {
        Objects.requireNonNull(value, "identificador da waystone");
    }

    public static WaystoneId random() {
        return new WaystoneId(UUID.randomUUID());
    }

    public static WaystoneId fromAnchor(UUID worldId, int x, int y, int z) {
        Objects.requireNonNull(worldId, "identificador do mundo");
        String source = "livingworld:waystone:" + worldId + ":" + x + ":" + y + ":" + z;
        return new WaystoneId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
    }
}
