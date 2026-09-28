package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompanionPackManifestCodecTest {
    private static final String HASH = "0123456789abcdef".repeat(4);
    private final CompanionPackManifestCodec codec =
            new CompanionPackManifestCodec();

    @Test
    void roundTripPreservaContrato() throws Exception {
        CompanionPackManifest expected = new CompanionPackManifest(
                CompanionPackKind.RESOURCE_PACK,
                "1.4.2",
                5,
                Optional.of(HASH)
        );
        StringWriter writer = new StringWriter();

        codec.write(expected, writer);

        assertEquals(
                expected,
                codec.read(new StringReader(writer.toString()))
        );
    }

    @Test
    void versaoHumanaNaoPrecisaSerSemver() throws Exception {
        CompanionPackManifest manifest = codec.read(new StringReader("""
                kind=datapack
                version=2026.09-preview
                schema=3
                """));

        assertEquals("2026.09-preview", manifest.version());
        assertEquals(3, manifest.schema());
        assertEquals(Optional.empty(), manifest.sha256());
    }

    @Test
    void rejeitaSchemaNaoNumericoEHashInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.read(new StringReader("""
                        kind=datapack
                        version=1.0.0
                        schema=abc
                        """))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.read(new StringReader("""
                        kind=resourcepack
                        version=1.0.0
                        schema=1
                        sha256=not-a-sha
                        """))
        );
    }

    @Test
    void rejeitaTipoDesconhecido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.read(new StringReader("""
                        kind=modpack
                        version=1
                        schema=1
                        """))
        );
    }
}
