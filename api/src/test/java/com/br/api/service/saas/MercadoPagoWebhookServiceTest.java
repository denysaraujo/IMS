package com.br.api.service.saas;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MercadoPagoWebhookServiceTest {
    private static final String SECRET = "webhook-test-secret";
    private static final String REQUEST_ID = "request-123";
    private final MercadoPagoClient client = mock(MercadoPagoClient.class);
    private final MercadoPagoWebhookService service =
            new MercadoPagoWebhookService(client, mock(BillingWebhookPersistenceService.class));

    @Test
    void acceptsValidMercadoPagoSignature() throws Exception {
        when(client.hasWebhookSecret()).thenReturn(true);
        when(client.getWebhookSecret()).thenReturn(SECRET);
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String id = "PREAPPROVAL-123";

        assertDoesNotThrow(() ->
                service.verifySignature(id, signature(id, timestamp), REQUEST_ID));
    }

    @Test
    void rejectsTamperedSignature() throws Exception {
        when(client.hasWebhookSecret()).thenReturn(true);
        when(client.getWebhookSecret()).thenReturn(SECRET);
        String timestamp = Long.toString(Instant.now().getEpochSecond());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
                service.verifySignature("123", "ts=" + timestamp + ",v1=" + "0".repeat(64), REQUEST_ID));
    }

    @Test
    void rejectsExpiredSignature() throws Exception {
        when(client.hasWebhookSecret()).thenReturn(true);
        when(client.getWebhookSecret()).thenReturn(SECRET);
        String timestamp = Long.toString(Instant.now().minusSeconds(600).getEpochSecond());
        String id = "123";

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
                service.verifySignature(id, signature(id, timestamp), REQUEST_ID));
    }

    private String signature(String id, String timestamp) throws Exception {
        String normalizedId = id.matches(".*[a-zA-Z].*") ? id.toLowerCase(java.util.Locale.ROOT) : id;
        String manifest = "id:" + normalizedId + ";request-id:" + REQUEST_ID + ";ts:" + timestamp + ";";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "ts=" + timestamp + ",v1=" + HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
    }
}
