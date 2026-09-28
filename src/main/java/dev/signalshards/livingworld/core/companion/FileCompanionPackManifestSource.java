package dev.signalshards.livingworld.core.companion;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class FileCompanionPackManifestSource
        implements CompanionPackManifestSource {
    private final Path path;
    private final CompanionPackManifestCodec codec;

    public FileCompanionPackManifestSource(Path path) {
        this(path, new CompanionPackManifestCodec());
    }

    FileCompanionPackManifestSource(
            Path path,
            CompanionPackManifestCodec codec
    ) {
        this.path = Objects.requireNonNull(path, "caminho do manifest");
        this.codec = Objects.requireNonNull(codec, "codec do manifest");
    }

    @Override
    public Optional<CompanionPackManifest> load() throws IOException {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {
            return Optional.of(codec.read(reader));
        }
    }
}
