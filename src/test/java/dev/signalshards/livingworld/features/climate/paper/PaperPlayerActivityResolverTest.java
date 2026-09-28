package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperPlayerActivityResolverTest {
    @Test
    void prioridadePreservaAtividadesEspecificas() {
        assertEquals(
                PlayerActivity.SWIMMING,
                PaperPlayerActivityResolver.resolve(true, true, true, true, 1.0D)
        );
        assertEquals(
                PlayerActivity.GLIDING,
                PaperPlayerActivityResolver.resolve(false, true, true, true, 1.0D)
        );
        assertEquals(
                PlayerActivity.CLIMBING,
                PaperPlayerActivityResolver.resolve(false, false, true, true, 1.0D)
        );
        assertEquals(
                PlayerActivity.SPRINTING,
                PaperPlayerActivityResolver.resolve(false, false, false, true, 1.0D)
        );
    }

    @Test
    void movimentoHorizontalNoChaoEhWalking() {
        assertEquals(
                PlayerActivity.WALKING,
                PaperPlayerActivityResolver.resolve(false, false, false, false, 0.01D)
        );
        assertEquals(
                PlayerActivity.RESTING,
                PaperPlayerActivityResolver.resolve(false, false, false, false, 0.0D)
        );
    }

    @Test
    void velocidadeInvalidaFalha() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PaperPlayerActivityResolver.resolve(
                        false, false, false, false, Double.NaN
                )
        );
    }
}
