package dev.signalshards.livingworld.core.companion;

import java.io.IOException;
import java.util.Optional;

@FunctionalInterface
public interface CompanionPackManifestSource {
    Optional<CompanionPackManifest> load() throws IOException;
}
