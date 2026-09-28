package dev.signalshards.livingworld.features.discovery.application;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Distribui {@link DiscoveryUnlocked} para os listeners registrados.
 *
 * <p>Não é um event bus genérico: existe para um único evento tipado deste feature.
 * A falha de um listener é isolada — uma apresentação quebrada não desfaz um fato já
 * persistido e não impede os demais listeners de rodar, o mesmo acordo do progresso do
 * calendário, onde a falha do clima não reverte o dia salvo.
 */
public final class DiscoveryEventPublisher {
    private final List<DiscoveryListener> listeners = new CopyOnWriteArrayList<>();
    private final Consumer<RuntimeException> failureReporter;

    public DiscoveryEventPublisher(Consumer<RuntimeException> failureReporter) {
        this.failureReporter = Objects.requireNonNull(
                failureReporter,
                "relator de falha de apresentação"
        );
    }

    public void subscribe(DiscoveryListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener de descoberta"));
    }

    public void unsubscribe(DiscoveryListener listener) {
        listeners.remove(Objects.requireNonNull(listener, "listener de descoberta"));
    }

    public void publish(DiscoveryUnlocked unlocked) {
        Objects.requireNonNull(unlocked, "descoberta registrada");
        for (DiscoveryListener listener : listeners) {
            try {
                listener.onDiscovery(unlocked);
            } catch (RuntimeException failure) {
                failureReporter.accept(failure);
            }
        }
    }
}
