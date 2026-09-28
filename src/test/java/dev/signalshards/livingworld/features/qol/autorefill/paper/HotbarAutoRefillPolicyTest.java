package dev.signalshards.livingworld.features.qol.autorefill.paper;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotbarAutoRefillPolicyTest {
    private final HotbarAutoRefillPolicy policy = new HotbarAutoRefillPolicy();

    @Test
    void aceitaEsgotamentoDaStackEmpilhavelNoSlotSelecionado() {
        assertTrue(policy.shouldRefill(
                2,
                2,
                1,
                64,
                false,
                true
        ));
    }

    @Test
    void rejeitaSlotNaoSelecionadoStackAindaPresenteEItemNaoEmpilhavel() {
        assertFalse(policy.shouldRefill(
                1,
                2,
                1,
                64,
                false,
                true
        ));
        assertFalse(policy.shouldRefill(
                2,
                2,
                2,
                64,
                false,
                false
        ));
        assertFalse(policy.shouldRefill(
                2,
                2,
                1,
                1,
                false,
                true
        ));
    }

    @Test
    void rejeitaQuandoVanillaDeixaUmItemSubstitutoNoSlot() {
        assertFalse(policy.shouldRefill(
                0,
                0,
                1,
                16,
                false,
                false
        ));
    }

    @Test
    void procuraSomenteSlotsDoInventarioPrincipalEEscolhePrimeiroMatch() {
        Set<Integer> matchingSlots = Set.of(4, 17, 28);

        assertEquals(
                17,
                policy.findReplacementSlot(
                        36,
                        matchingSlots::contains
                )
        );
    }

    @Test
    void rejeitaHotbarEInventarioSemCorrespondencia() {
        Set<Integer> hotbarOnly = Set.of(1, 4, 8);

        assertEquals(
                -1,
                policy.findReplacementSlot(
                        36,
                        hotbarOnly::contains
                )
        );
        assertEquals(
                -1,
                policy.findReplacementSlot(
                        12,
                        slot -> false
                )
        );
    }
}
