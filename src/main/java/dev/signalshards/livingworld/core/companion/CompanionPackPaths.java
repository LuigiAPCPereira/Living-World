package dev.signalshards.livingworld.core.companion;

import java.nio.file.Path;
import java.util.Objects;

public final class CompanionPackPaths {
    public static final String MANIFEST_FILE = "living-world-pack.properties";

    private CompanionPackPaths() {
    }

    public static Path manifest(
            Path pluginDataFolder,
            CompanionPackKind kind
    ) {
        Objects.requireNonNull(pluginDataFolder, "pasta de dados do plugin");
        Objects.requireNonNull(kind, "tipo do companion pack");
        return pluginDataFolder
                .resolve("companions")
                .resolve(kind.manifestValue())
                .resolve(MANIFEST_FILE);
    }
}
