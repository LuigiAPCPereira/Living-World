package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePackClientPolicyTest {
    private final ResourcePackClientPolicy policy =
            new ResourcePackClientPolicy();
    private final UUID requestId = UUID.fromString(
            "e812669a-e3d0-4643-a899-254752489ef1"
    );

    @Test
    void fluxoAceitoDownloadedLoadedAtivaApresentacao() {
        ResourcePackClientSession session =
                ResourcePackClientSession.requested(requestId, false);

        session = policy.apply(
                session,
                requestId,
                ResourcePackClientEvent.ACCEPTED
        );
        assertEquals(ResourcePackClientStatus.ACCEPTED, session.status());
        session = policy.apply(
                session,
                requestId,
                ResourcePackClientEvent.DOWNLOADED
        );
        assertEquals(ResourcePackClientStatus.DOWNLOADED, session.status());
        session = policy.apply(
                session,
                requestId,
                ResourcePackClientEvent.LOADED
        );

        assertEquals(ResourcePackClientStatus.LOADED, session.status());
        assertTrue(session.status().presentationAvailable());
        assertTrue(session.contractSatisfied());
        assertFalse(session.useVanillaFallback());
    }

    @Test
    void requestIdDiferenteEhIgnorado() {
        ResourcePackClientSession session =
                ResourcePackClientSession.requested(requestId, false);

        ResourcePackClientSession unchanged = policy.apply(
                session,
                UUID.randomUUID(),
                ResourcePackClientEvent.LOADED
        );

        assertEquals(session, unchanged);
    }

    @Test
    void packOpcionalDeclinadoUsaFallbackEContratoContinuaCoerente() {
        ResourcePackClientSession session = policy.apply(
                ResourcePackClientSession.requested(requestId, false),
                requestId,
                ResourcePackClientEvent.DECLINED
        );

        assertEquals(ResourcePackClientStatus.DECLINED, session.status());
        assertTrue(session.useVanillaFallback());
        assertTrue(session.contractSatisfied());
    }

    @Test
    void packObrigatorioDeclinadoNaoTemFallback() {
        ResourcePackClientSession session = policy.apply(
                ResourcePackClientSession.requested(requestId, true),
                requestId,
                ResourcePackClientEvent.DECLINED
        );

        assertFalse(session.useVanillaFallback());
        assertFalse(session.contractSatisfied());
    }

    @Test
    void terminalNaoPodeSerReabertoPorEventoAtrasado() {
        ResourcePackClientSession declined = policy.apply(
                ResourcePackClientSession.requested(requestId, false),
                requestId,
                ResourcePackClientEvent.DECLINED
        );

        assertEquals(
                declined,
                policy.apply(
                        declined,
                        requestId,
                        ResourcePackClientEvent.LOADED
                )
        );
    }

    @Test
    void loadedDiretoEhAceitoPorquePaperPodeOmitirIntermediarios() {
        ResourcePackClientSession session = policy.apply(
                ResourcePackClientSession.requested(requestId, false),
                requestId,
                ResourcePackClientEvent.LOADED
        );

        assertEquals(ResourcePackClientStatus.LOADED, session.status());
    }
}
