package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityThermalPolicyTest {
    private final ActivityThermalPolicy policy = new ActivityThermalPolicy();

    @Test
    void descansoNaoAdicionaCalorAcimaDoBaseline() {
        assertEquals(
                0.0D,
                policy.exchangeRate(PlayerActivity.RESTING).loadPerSecond()
        );
    }

    @Test
    void esforcoMaiorGeraMaisCalorMetabolico() {
        double walking = policy.exchangeRate(PlayerActivity.WALKING).loadPerSecond();
        double sprinting = policy.exchangeRate(PlayerActivity.SPRINTING).loadPerSecond();
        double swimming = policy.exchangeRate(PlayerActivity.SWIMMING).loadPerSecond();

        assertTrue(walking > 0.0D);
        assertTrue(sprinting > walking);
        assertTrue(swimming > sprinting);
    }

    @Test
    void elytraTemPoucoCalorMetabolico() {
        double gliding = policy.exchangeRate(PlayerActivity.GLIDING).loadPerSecond();
        double walking = policy.exchangeRate(PlayerActivity.WALKING).loadPerSecond();

        assertTrue(gliding > 0.0D);
        assertTrue(gliding < walking);
    }
}
