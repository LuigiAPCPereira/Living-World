package dev.signalshards.livingworld.features.discovery.presentation;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryTypeDefinition;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscoveryPresentationPolicyTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");

    @Test
    void montaTituloELegendaNoIdiomaPadrao() {
        DiscoveryPresentationRequest request = policy("pt-BR").requestFor(unlocked());

        assertEquals("Descoberto", request.title());
        assertEquals("Portal da Savana • Waystone", request.subtitle());
        assertEquals(DiscoveryScope.PERSONAL, request.scope());
        assertEquals(WAYSTONE, request.type());
    }

    @Test
    void idiomaSecundarioUsaOMesmoContrato() {
        DiscoveryPresentationRequest request = policy("en-US").requestFor(unlocked());

        assertEquals("Discovered", request.title());
        assertEquals("Portal da Savana • Waystone", request.subtitle());
    }

    private DiscoveryPresentationPolicy policy(String languageTag) {
        return new DiscoveryPresentationPolicy(
                MessageCatalog.fromLanguageTag(languageTag),
                new DiscoveryRegistry(List.of(
                        new DiscoveryTypeDefinition(
                                WAYSTONE,
                                DiscoveryScope.PERSONAL,
                                "discovery.type.waystone"
                        )
                ))
        );
    }

    private DiscoveryUnlocked unlocked() {
        UUID owner = UUID.randomUUID();
        return new DiscoveryUnlocked(
                new DiscoveryRecord(
                        WAYSTONE,
                        new DiscoveryId("waystone-1"),
                        DiscoveryScope.PERSONAL,
                        owner
                ),
                owner,
                "Portal da Savana"
        );
    }
}
