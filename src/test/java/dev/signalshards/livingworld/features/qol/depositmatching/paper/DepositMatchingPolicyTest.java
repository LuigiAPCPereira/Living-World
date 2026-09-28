package dev.signalshards.livingworld.features.qol.depositmatching.paper;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositMatchingPolicyTest {
    private final DepositMatchingPolicy policy = new DepositMatchingPolicy();

    @Test
    void aceitaSomenteGestoExplicitoEmStorageAbertoSuportado() {
        assertTrue(policy.shouldTrigger(
                true,
                true,
                true,
                true,
                true,
                false
        ));
    }

    @Test
    void rejeitaConflitosComVanillaOuContextoAbertoInvalido() {
        assertFalse(policy.shouldTrigger(false, true, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, false, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, false, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, false, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, false, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, true, true));
    }

    @Test
    void aceitaGestoFechadoSomenteComMaoPrincipalVaziaEAcessoSeguro() {
        assertTrue(policy.shouldTriggerClosedStorage(
                true,
                true,
                true,
                true,
                true,
                true,
                false
        ));
    }

    @Test
    void rejeitaGestoFechadoQuandoQualquerPreCondicaoFalha() {
        assertFalse(policy.shouldTriggerClosedStorage(
                false, true, true, true, true, true, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, false, true, true, true, true, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, true, false, true, true, true, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, true, true, false, true, true, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, true, true, true, false, true, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, true, true, true, true, false, false
        ));
        assertFalse(policy.shouldTriggerClosedStorage(
                true, true, true, true, true, true, true
        ));
    }

    @Test
    void consideraSomenteSlotsDoInventarioPrincipal() {
        Set<Integer> matches = Set.of(0, 8, 9, 14, 35);

        assertEquals(
                List.of(9, 14, 35),
                policy.matchingSourceSlots(36, matches::contains)
        );
    }

    @Test
    void respeitaInventarioCurtoESemCorrespondencias() {
        assertEquals(
                List.of(9, 10),
                policy.matchingSourceSlots(
                        11,
                        slot -> slot >= 9
                )
        );
        assertEquals(
                List.of(),
                policy.matchingSourceSlots(9, slot -> true)
        );
        assertEquals(
                List.of(),
                policy.matchingSourceSlots(36, slot -> false)
        );
    }
}
