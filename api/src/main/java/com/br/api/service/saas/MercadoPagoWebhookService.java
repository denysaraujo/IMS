package com.br.api.service.saas;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Locale;
import java.util.HexFormat;

@Service
public class MercadoPagoWebhookService {
    private final MercadoPagoClient mercadoPagoClient;
    private final BillingWebhookPersistenceService persistenceService;

    public MercadoPagoWebhookService(
            MercadoPagoClient mercadoPagoClient,
            BillingWebhookPersistenceService persistenceService) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.persistenceService = persistenceService;
    }

    public void process(
            JsonNode payload,
            String queryDataId,
            String signature,
            String requestId) {
        String dataId = payload.path("data").path("id").asText(queryDataId);
        if (dataId == null || dataId.isBlank() || !dataId.equals(queryDataId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador do evento inválido.");
        }
        verifySignature(dataId, signature, requestId);

        String type = payload.path("type").asText(payload.path("topic").asText(""));
        String eventId = payload.path("id").asText("");
        if (eventId.isBlank() || eventId.length() > 160) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evento Mercado Pago sem identificador válido.");
        }
        if ("subscription_authorized_payment".equals(type)) {
            MercadoPagoClient.ProviderPayment payment = mercadoPagoClient.getAuthorizedPayment(dataId);
            MercadoPagoClient.ProviderSubscription subscription = mercadoPagoClient.getSubscription(payment.preapprovalId());
            persistenceService.recordPaymentAndEvent(
                    eventId,
                    type,
                    payment,
                    subscription.externalReference()
            );
            return;
        }
        if (!"subscription_preapproval".equals(type)) {
            return;
        }
        MercadoPagoClient.ProviderSubscription subscription = mercadoPagoClient.getSubscription(dataId);
        persistenceService.recordAndUpdate(
                eventId,
                type,
                dataId,
                subscription.status(),
                subscription.externalReference()
        );
    }

    void verifySignature(String dataId, String signature, String requestId) {
        if (!mercadoPagoClient.hasWebhookSecret()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Validação de notificações Mercado Pago não está configurada."
            );
        }
        if (signature == null || requestId == null || requestId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
        }

        String timestamp = null;
        String receivedHash = null;
        for (String part : signature.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && "ts".equals(pair[0])) {
                timestamp = pair[1];
            } else if (pair.length == 2 && "v1".equals(pair[0])) {
                receivedHash = pair[1];
            }
        }
        if (timestamp == null || receivedHash == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
        }

        long timestampSeconds;
        try {
            timestampSeconds = Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Timestamp do webhook inválido.");
        }
        if (Math.abs(Instant.now().getEpochSecond() - timestampSeconds) > 300) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Notificação Mercado Pago expirada.");
        }

        String normalizedId = dataId.matches(".*[a-zA-Z].*") ? dataId.toLowerCase(Locale.ROOT) : dataId;
        String manifest = "id:" + normalizedId + ";request-id:" + requestId + ";ts:" + timestamp + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    mercadoPagoClient.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] expected = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            byte[] received = decodeHex(receivedHash);
            if (!MessageDigest.isEqual(expected, received)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível validar a assinatura do webhook.", exception);
        }
    }

    private byte[] decodeHex(String value) {
        if (value == null || value.length() != 64) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
        }
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
        }
    }
}
