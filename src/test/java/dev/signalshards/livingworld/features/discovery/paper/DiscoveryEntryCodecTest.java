package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscoveryEntryCodecTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");

    @Test
    void gravaCabecalhoDeVersaoAntesDosPares() {
        UUID owner = UUID.randomUUID();
        DiscoveryRecord record = record(owner);

        List<String> encoded = DiscoveryEntryCodec.encode(List.of(record));

        assertEquals(
                List.of(
                        DiscoveryEntryCodec.CURRENT_SCHEMA_MARKER,
                        "waystone",
                        "obelisco"
                ),
                encoded
        );
    }

    @Test
    void lePayloadLegadoSemCabecalhoComoV1() {
        UUID owner = UUID.randomUUID();

        Set<DiscoveryRecord> decoded = DiscoveryEntryCodec.decode(
                List.of("waystone", "obelisco"),
                DiscoveryScope.PERSONAL,
                owner
        );

        assertEquals(Set.of(record(owner)), decoded);
    }

    @Test
    void rejeitaVersaoFuturaDesconhecidaSemTentarInterpretar() {
        UUID owner = UUID.randomUUID();

        assertThrows(
                IllegalStateException.class,
                () -> DiscoveryEntryCodec.decode(
                        List.of("@livingworld-discovery:v2", "waystone", "obelisco"),
                        DiscoveryScope.PERSONAL,
                        owner
                )
        );
    }

    @Test
    void rejeitaPayloadVersionadoComParIncompleto() {
        UUID owner = UUID.randomUUID();

        assertThrows(
                IllegalStateException.class,
                () -> DiscoveryEntryCodec.decode(
                        List.of(DiscoveryEntryCodec.CURRENT_SCHEMA_MARKER, "waystone"),
                        DiscoveryScope.PERSONAL,
                        owner
                )
        );
    }

    private DiscoveryRecord record(UUID owner) {
        return new DiscoveryRecord(
                WAYSTONE,
                new DiscoveryId("obelisco"),
                DiscoveryScope.PERSONAL,
                owner
        );
    }
}
