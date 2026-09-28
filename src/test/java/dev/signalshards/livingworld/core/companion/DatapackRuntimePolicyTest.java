package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatapackRuntimePolicyTest {
    private final DatapackRuntimePolicy policy = new DatapackRuntimePolicy();

    @Test
    void compatibleDistingueEnabledDeDisabled() {
        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_ENABLED,
                policy.statusFor(CompanionPackCompatibility.COMPATIBLE, true)
        );
        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_DISABLED,
                policy.statusFor(CompanionPackCompatibility.COMPATIBLE, false)
        );
    }

    @Test
    void opcionalAusentePermiteFallbackMasObrigatorioNao() {
        DatapackRuntimeStatus optional = policy.statusFor(
                CompanionPackCompatibility.OPTIONAL_MISSING,
                false
        );
        DatapackRuntimeStatus required = policy.statusFor(
                CompanionPackCompatibility.REQUIRED_MISSING,
                false
        );

        assertTrue(optional.usable());
        assertFalse(required.usable());
    }

    @Test
    void qualquerIncompatibilidadeDeContratoFalhaRuntime() {
        assertEquals(
                DatapackRuntimeStatus.INCOMPATIBLE,
                policy.statusFor(
                        CompanionPackCompatibility.SCHEMA_MISMATCH,
                        true
                )
        );
        assertEquals(
                DatapackRuntimeStatus.INCOMPATIBLE,
                policy.statusFor(
                        CompanionPackCompatibility.HASH_MISMATCH,
                        true
                )
        );
    }
}
