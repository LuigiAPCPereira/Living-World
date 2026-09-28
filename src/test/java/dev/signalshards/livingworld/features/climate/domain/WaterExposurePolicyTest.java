package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterExposurePolicyTest {
    private final WaterExposurePolicy policy = new WaterExposurePolicy();

    @Test
    void semContatoNaoMolhaNemTransfereCalor() {
        WaterExposureEffect effect = policy.evaluate(WaterExposure.none());

        assertEquals(0.0D, effect.wetnessRate().levelPerSecond());
        assertEquals(0.0D, effect.thermalTransferFactor().value());
    }

    @Test
    void maisSubmersaoAumentaMolhamentoETransferencia() {
        WaterExposureEffect feet = policy.evaluate(new WaterExposure(0.15D, 0.0D));
        WaterExposureEffect waist = policy.evaluate(new WaterExposure(0.50D, 0.0D));
        WaterExposureEffect full = policy.evaluate(new WaterExposure(1.0D, 0.0D));

        assertTrue(feet.wetnessRate().levelPerSecond() < waist.wetnessRate().levelPerSecond());
        assertTrue(waist.wetnessRate().levelPerSecond() < full.wetnessRate().levelPerSecond());
        assertTrue(feet.thermalTransferFactor().value() < waist.thermalTransferFactor().value());
        assertTrue(waist.thermalTransferFactor().value() < full.thermalTransferFactor().value());
        assertEquals(0.12D, full.wetnessRate().levelPerSecond(), 1.0E-9D);
        assertEquals(1.0D, full.thermalTransferFactor().value(), 1.0E-9D);
    }

    @Test
    void profundidadeAumentaTransferenciaMasNaoVelocidadeDeMolhamento() {
        WaterExposureEffect surface = policy.evaluate(new WaterExposure(1.0D, 0.0D));
        WaterExposureEffect deep = policy.evaluate(new WaterExposure(1.0D, 16.0D));

        assertEquals(
                surface.wetnessRate().levelPerSecond(),
                deep.wetnessRate().levelPerSecond(),
                1.0E-9D
        );
        assertTrue(
                deep.thermalTransferFactor().value()
                        > surface.thermalTransferFactor().value()
        );
    }

    @Test
    void bonusDeProfundidadeESaturado() {
        WaterExposureEffect reference = policy.evaluate(new WaterExposure(1.0D, 32.0D));
        WaterExposureEffect abyssal = policy.evaluate(new WaterExposure(1.0D, 512.0D));

        assertEquals(1.35D, reference.thermalTransferFactor().value(), 1.0E-9D);
        assertEquals(
                reference.thermalTransferFactor().value(),
                abyssal.thermalTransferFactor().value(),
                1.0E-9D
        );
    }
}
