package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanionPackContractPolicyTest {
    private static final String HASH_A = "a".repeat(64);
    private static final String HASH_B = "b".repeat(64);
    private final CompanionPackContractPolicy policy =
            new CompanionPackContractPolicy();

    @Test
    void versoesTextuaisDiferentesContinuamCompativeisComMesmoSchema() {
        CompanionPackCompatibility result = policy.evaluate(
                new CompanionPackRequirement(
                        CompanionPackKind.DATAPACK,
                        3,
                        true
                ),
                CompanionPackManifest.withoutHash(
                        CompanionPackKind.DATAPACK,
                        "1.4.0",
                        3
                )
        );

        assertEquals(CompanionPackCompatibility.COMPATIBLE, result);
        assertTrue(result.compatible());
    }

    @Test
    void packOpcionalAusenteTemFallbackSeguro() {
        CompanionPackCompatibility result = policy.evaluateMissing(
                LivingWorldCompanionContracts.resourcePack()
        );

        assertEquals(
                CompanionPackCompatibility.OPTIONAL_MISSING,
                result
        );
        assertTrue(result.compatible());
    }

    @Test
    void packObrigatorioAusenteFalhaContrato() {
        CompanionPackCompatibility result = policy.evaluateMissing(
                new CompanionPackRequirement(
                        CompanionPackKind.DATAPACK,
                        1,
                        true
                )
        );

        assertEquals(
                CompanionPackCompatibility.REQUIRED_MISSING,
                result
        );
        assertFalse(result.compatible());
    }

    @Test
    void tipoESchemaErradosFalhamIndependentementeDaVersao() {
        CompanionPackRequirement requirement =
                LivingWorldCompanionContracts.datapack();

        assertEquals(
                CompanionPackCompatibility.WRONG_KIND,
                policy.evaluate(
                        requirement,
                        CompanionPackManifest.withoutHash(
                                CompanionPackKind.RESOURCE_PACK,
                                "999.999",
                                requirement.schema()
                        )
                )
        );
        assertEquals(
                CompanionPackCompatibility.SCHEMA_MISMATCH,
                policy.evaluate(
                        requirement,
                        CompanionPackManifest.withoutHash(
                                CompanionPackKind.DATAPACK,
                                "1.0.0",
                                requirement.schema() + 1
                        )
                )
        );
    }

    @Test
    void hashDeclaradoPodeSerVerificadoQuandoBytesSaoObservados() {
        CompanionPackManifest manifest = new CompanionPackManifest(
                CompanionPackKind.RESOURCE_PACK,
                "1.0.0",
                1,
                Optional.of(HASH_A)
        );

        assertEquals(
                CompanionPackCompatibility.COMPATIBLE,
                policy.evaluate(
                        LivingWorldCompanionContracts.resourcePack(),
                        manifest,
                        HASH_A
                )
        );
        assertEquals(
                CompanionPackCompatibility.HASH_MISMATCH,
                policy.evaluate(
                        LivingWorldCompanionContracts.resourcePack(),
                        manifest,
                        HASH_B
                )
        );
    }
}
