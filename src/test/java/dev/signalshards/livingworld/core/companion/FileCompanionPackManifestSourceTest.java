package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileCompanionPackManifestSourceTest {
    @TempDir
    Path temp;

    @Test
    void arquivoAusenteRetornaEmpty() throws Exception {
        assertTrue(new FileCompanionPackManifestSource(
                temp.resolve("missing.properties")
        ).load().isEmpty());
    }

    @Test
    void leManifestUtf8() throws Exception {
        Path manifest = temp.resolve("living-world-pack.properties");
        Files.writeString(manifest, """
                kind=resourcepack
                version=prévia-1
                schema=1
                """);

        assertEquals(
                "prévia-1",
                new FileCompanionPackManifestSource(manifest)
                        .load()
                        .orElseThrow()
                        .version()
        );
    }
}
