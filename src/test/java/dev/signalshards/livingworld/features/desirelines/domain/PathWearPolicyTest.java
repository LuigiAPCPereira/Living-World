package dev.signalshards.livingworld.features.desirelines.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathWearPolicyTest {
    private final PathWearPolicy policy = new PathWearPolicy(PathWearSettings.defaults());

    @Test
    void evoluiEmDoisEstagiosConfigurados() {
        assertEquals(PathWearStage.NATURAL, policy.stageFor(11));
        assertEquals(PathWearStage.DESGASTADO, policy.stageFor(12));
        assertEquals(PathWearStage.DESGASTADO, policy.stageFor(23));
        assertEquals(PathWearStage.CAMINHO, policy.stageFor(24));
    }

    @Test
    void rejeitaConfiguracaoComThresholdsInvertidos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PathWearSettings(true, 20, 10, 512, 200)
        );
    }
}
