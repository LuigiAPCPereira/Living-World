package dev.signalshards.livingworld.features.qol.containersort.paper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerSortPolicyTest {
    private final ContainerSortPolicy policy = new ContainerSortPolicy();

    @Test
    void acceptsOnlyExplicitVanillaNoOpGesture() {
        assertTrue(policy.shouldTrigger(
                true,
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
    void rejectsEveryUnsafeOrConflictingCondition() {
        assertFalse(policy.shouldTrigger(false, true, true, true, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, false, true, true, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, false, true, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, false, true, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, false, true, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, true, false, true, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, true, true, false, false));
        assertFalse(policy.shouldTrigger(true, true, true, true, true, true, true, true));
    }
}
