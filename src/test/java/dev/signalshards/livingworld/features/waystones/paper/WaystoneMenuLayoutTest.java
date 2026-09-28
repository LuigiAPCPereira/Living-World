package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaystoneMenuLayoutTest {
    @Test
    void arredondaQuantidadeParaLinhasDeNove() {
        assertEquals(9, WaystoneMenuLayout.inventorySize(1));
        assertEquals(9, WaystoneMenuLayout.inventorySize(9));
        assertEquals(18, WaystoneMenuLayout.inventorySize(10));
        assertEquals(54, WaystoneMenuLayout.inventorySize(54));
    }

    @Test
    void naoTruncaMaisDeCinquentaEQuatroDestinos() {
        assertTrue(WaystoneMenuLayout.canDisplay(54));
        assertFalse(WaystoneMenuLayout.canDisplay(55));
        assertThrows(
                IllegalArgumentException.class,
                () -> WaystoneMenuLayout.inventorySize(55)
        );
    }

    @Test
    void priorizaMesmoMundoPorDistanciaEOutrosMundosPorNome() {
        UUID currentWorld = UUID.randomUUID();
        UUID otherWorld = UUID.randomUUID();
        Waystone near = waystone("Zeta perto", currentWorld, 2, 64, 0);
        Waystone far = waystone("Alfa longe", currentWorld, 20, 64, 0);
        Waystone otherBeta = waystone("Beta", otherWorld, 0, 64, 0);
        Waystone otherAlfa = waystone("Alfa", otherWorld, 0, 64, 0);

        assertEquals(
                List.of(near, far, otherAlfa, otherBeta),
                WaystoneMenuLayout.orderDestinations(
                        currentWorld,
                        0.5D,
                        64.0D,
                        0.5D,
                        List.of(otherBeta, far, otherAlfa, near)
                )
        );
    }

    @Test
    void calculaDistanciaAteOCentroDaAncora() {
        UUID world = UUID.randomUUID();
        Waystone target = waystone("Casa", world, 3, 64, 4);

        assertEquals(
                5L,
                WaystoneMenuLayout.distanceBlocks(
                        0.5D,
                        64.0D,
                        0.5D,
                        target
                )
        );
    }

    private Waystone waystone(
            String name,
            UUID world,
            int x,
            int y,
            int z
    ) {
        return new Waystone(
                WaystoneId.random(),
                name,
                world,
                x,
                y,
                z
        );
    }
}
