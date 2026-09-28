package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryRegistryTest {
    private static final DiscoveryType WAYSTONE = new DiscoveryType("waystone");
    private static final DiscoveryType LANDMARK = new DiscoveryType("landmark");

    @Test
    void tipoDeclaradoResolveEscopoERotulo() {
        DiscoveryRegistry registry = new DiscoveryRegistry(List.of(
                new DiscoveryTypeDefinition(WAYSTONE, DiscoveryScope.PERSONAL, "discovery.type.waystone")
        ));

        assertTrue(registry.declares(WAYSTONE));
        assertEquals(DiscoveryScope.PERSONAL, registry.require(WAYSTONE).scope());
        assertEquals(
                "discovery.type.waystone",
                registry.require(WAYSTONE).labelMessageKey()
        );
    }

    @Test
    void tipoNaoDeclaradoFalhaFechado() {
        DiscoveryRegistry registry = new DiscoveryRegistry(List.of());

        assertFalse(registry.declares(LANDMARK));
        assertThrows(IllegalStateException.class, () -> registry.require(LANDMARK));
    }

    @Test
    void declararOMesmoTipoDuasVezesFalhaNaConstrucao() {
        List<DiscoveryTypeDefinition> duplicated = List.of(
                new DiscoveryTypeDefinition(WAYSTONE, DiscoveryScope.PERSONAL, "discovery.type.waystone"),
                new DiscoveryTypeDefinition(WAYSTONE, DiscoveryScope.WORLD, "discovery.type.waystone")
        );

        assertThrows(IllegalArgumentException.class, () -> new DiscoveryRegistry(duplicated));
    }
}
