package dev.signalshards.livingworld.features.waystones.paper;

import org.junit.jupiter.api.Test;

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
}
