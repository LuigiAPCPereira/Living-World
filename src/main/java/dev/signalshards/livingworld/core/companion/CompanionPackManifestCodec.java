package dev.signalshards.livingworld.core.companion;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Codec dependency-free para living-world-pack.properties.
 */
public final class CompanionPackManifestCodec {
    private static final String KIND = "kind";
    private static final String VERSION = "version";
    private static final String SCHEMA = "schema";
    private static final String SHA256 = "sha256";

    public CompanionPackManifest read(Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader do manifest");
        Properties properties = new Properties();
        properties.load(reader);

        String kind = required(properties, KIND);
        String version = required(properties, VERSION);
        String schema = required(properties, SCHEMA);
        Optional<String> sha256 = Optional.ofNullable(
                properties.getProperty(SHA256)
        );

        final int schemaNumber;
        try {
            schemaNumber = Integer.parseInt(schema);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Schema inválido no manifest: " + schema,
                    exception
            );
        }

        return new CompanionPackManifest(
                CompanionPackKind.fromManifestValue(kind),
                version,
                schemaNumber,
                sha256
        );
    }

    public void write(
            CompanionPackManifest manifest,
            Writer writer
    ) throws IOException {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(writer, "writer");
        Properties properties = new Properties();
        properties.setProperty(KIND, manifest.kind().manifestValue());
        properties.setProperty(VERSION, manifest.version());
        properties.setProperty(SCHEMA, Integer.toString(manifest.schema()));
        manifest.sha256().ifPresent(
                hash -> properties.setProperty(SHA256, hash)
        );
        properties.store(writer, "Living World companion pack");
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Chave obrigatória ausente no manifest: " + key
            );
        }
        return value.trim();
    }
}
