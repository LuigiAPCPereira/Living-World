package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperVanillaFrostPresenterTest {

    @Test
    void overlayVisualNuncaAtingeFreezeMaximo() {
        assertEquals(35, PaperVanillaFrostPresenter.visualFreezeTicks(140, 0.20D));
        assertEquals(63, PaperVanillaFrostPresenter.visualFreezeTicks(140, 0.60D));
        assertEquals(91, PaperVanillaFrostPresenter.visualFreezeTicks(140, 1.00D));
        assertTrue(PaperVanillaFrostPresenter.visualFreezeTicks(140, 1.00D) < 140);
    }

    @Test
    void tremorSoComecaEmFrostMaisIntensoEPermaneceBounded() {
        assertEquals(0.0F, PaperVanillaFrostPresenter.shiverOffsetDegrees(0.40D, true));
        float positive = PaperVanillaFrostPresenter.shiverOffsetDegrees(0.80D, true);
        float negative = PaperVanillaFrostPresenter.shiverOffsetDegrees(0.80D, false);

        assertTrue(positive > 0.0F);
        assertEquals(-positive, negative, 1.0E-6F);
        assertTrue(Math.abs(positive) <= 4.00F);
    }

    @Test
    void aplicaOverlaySemLockERestauraQuandoLiberaOwnership() {
        FrostPlayer state = new FrostPlayer();
        Player player = state.player();
        PaperVanillaFrostPresenter presenter = new PaperVanillaFrostPresenter();

        presenter.presentFrost(player, 0.80D);

        assertTrue(state.freezeTicks.get() > 0);
        assertTrue(state.freezeTicks.get() < state.maxFreezeTicks);
        assertEquals(0, state.lockCalls.get());
        assertTrue(Math.abs(state.bodyYaw.get() - state.viewYaw) > 0.0F);

        presenter.clearFrost(player);

        assertEquals(0, state.freezeTicks.get());
        assertEquals(state.viewYaw, state.bodyYaw.get(), 1.0E-6F);
    }

    @Test
    void naoAssumeOwnershipQuandoFreezeVanillaJaExiste() {
        FrostPlayer state = new FrostPlayer();
        state.freezeTicks.set(25);
        Player player = state.player();

        new PaperVanillaFrostPresenter().presentFrost(player, 0.90D);

        assertEquals(25, state.freezeTicks.get());
        assertEquals(0, state.setFreezeCalls.get());
        assertEquals(state.viewYaw, state.bodyYaw.get(), 1.0E-6F);
    }

    @Test
    void powderedSnowTemPrioridadeSobreFrostVisual() {
        FrostPlayer state = new FrostPlayer();
        state.inPowderedSnow = true;
        Player player = state.player();

        new PaperVanillaFrostPresenter().presentFrost(player, 0.90D);

        assertEquals(0, state.setFreezeCalls.get());
        assertEquals(0, state.lockCalls.get());
        assertEquals(state.viewYaw, state.bodyYaw.get(), 1.0E-6F);
    }

    private static final class FrostPlayer {
        final UUID id = UUID.randomUUID();
        final int maxFreezeTicks = 140;
        final float viewYaw = 30.0F;
        final AtomicInteger freezeTicks = new AtomicInteger();
        final AtomicInteger setFreezeCalls = new AtomicInteger();
        final AtomicInteger lockCalls = new AtomicInteger();
        final AtomicReference<Float> bodyYaw = new AtomicReference<>(viewYaw);
        boolean inPowderedSnow;

        Player player() {
            return (Player) Proxy.newProxyInstance(
                    Player.class.getClassLoader(),
                    new Class<?>[]{Player.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getUniqueId" -> id;
                        case "getFreezeTicks" -> freezeTicks.get();
                        case "getMaxFreezeTicks" -> maxFreezeTicks;
                        case "setFreezeTicks" -> {
                            setFreezeCalls.incrementAndGet();
                            freezeTicks.set((int) args[0]);
                            yield null;
                        }
                        case "lockFreezeTicks" -> {
                            lockCalls.incrementAndGet();
                            yield null;
                        }
                        case "isInPowderedSnow" -> inPowderedSnow;
                        case "getBodyYaw" -> bodyYaw.get();
                        case "setBodyYaw" -> {
                            bodyYaw.set((float) args[0]);
                            yield null;
                        }
                        case "getLocation" -> new Location(
                                null,
                                0.0D,
                                64.0D,
                                0.0D,
                                viewYaw,
                                0.0F
                        );
                        case "toString" -> "FakeFrostPlayer";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> null;
                    }
            );
        }
    }
}
