package dev.signalshards.livingworld.core.status;

@FunctionalInterface
public interface LivingWorldStatusProvider {
    LivingWorldStatusSnapshot snapshot();
}
