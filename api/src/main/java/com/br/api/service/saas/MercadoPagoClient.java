package com.br.api.service.saas;

import com.br.api.model.saas.Plan;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MercadoPagoClient {
    private final RestClient restClient;
    private final String accessToken;
    private final String webhookSecret;
    private final String notificationUrl;
    private final String backUrl;

    public MercadoPagoClient(
            RestClient.Builder builder,
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${mercadopago.webhook-secret:}") String webhookSecret,
            @Value("${mercadopago.notification-url:}") String notificationUrl,
            @Value("${mercadopago.back-url:}") String backUrl,
            @Value("${mercadopago.api-url:https://api.mercadopago.com}") String apiUrl) {
        this.restClient = builder.baseUrl(apiUrl).build();
        this.accessToken = accessToken;
        this.webhookSecret = webhookSecret;
        this.notificationUrl = notificationUrl;
        this.backUrl = backUrl;
    }

    public boolean isConfigured() {
        return !accessToken.isBlank() && !webhookSecret.isBlank()
                && !notificationUrl.isBlank() && !backUrl.isBlank();
    }

    public boolean hasWebhookSecret() {
        return !webhookSecret.isBlank();
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public Checkout createSubscription(Plan plan, String payerEmail, Long companyId) {
        requireConfiguration();
        Map<String, Object> recurring = new LinkedHashMap<>();
        recurring.put("frequency", plan.getFrequency());
        recurring.put("frequency_type", plan.getFrequencyType());
        recurring.put("transaction_amount", plan.getAmount());
        recurring.put("currency_id", plan.getCurrency());

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("reason", "IMS - " + plan.getName());
        request.put("auto_recurring", recurring);
        request.put("payer_email", payerEmail);
        request.put("back_url", backUrl);
        request.put("notification_url", notificationUrl);
        request.put("external_reference", companyId.toString());

        JsonNode response = restClient.post()
                .uri("/preapproval")
                .headers(this::authorize)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("id") || !response.hasNonNull("init_point")) {
            throw new IllegalStateException("Mercado Pago não retornou o identificador e a URL de checkout.");
        }
        return new Checkout(response.get("id").asText(), response.get("init_point").asText());
    }

    public String cancelSubscription(String providerSubscriptionId) {
        requireAccessToken();
        JsonNode response = restClient.put()
                .uri("/preapproval/{id}", providerSubscriptionId)
                .headers(this::authorize)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "cancelled"))
                .retrieve()
                .body(JsonNode.class);
        return response != null && response.hasNonNull("status") ? response.get("status").asText() : "cancelled";
    }

    public ProviderSubscription getSubscription(String providerSubscriptionId) {
        requireAccessToken();
        JsonNode response = restClient.get()
                .uri("/preapproval/{id}", providerSubscriptionId)
                .headers(this::authorize)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("status")) {
            throw new IllegalStateException("Mercado Pago não retornou o estado da assinatura.");
        }
        String externalReference = response.hasNonNull("external_reference")
                ? response.get("external_reference").asText() : null;
        BigDecimal amount = response.path("auto_recurring").path("transaction_amount").decimalValue();
        return new ProviderSubscription(response.get("status").asText(), externalReference, amount);
    }

    public ProviderPayment getAuthorizedPayment(String authorizedPaymentId) {
        requireAccessToken();
        JsonNode response = restClient.get()
                .uri("/authorized_payments/{id}", authorizedPaymentId)
                .headers(this::authorize)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("id") || !response.hasNonNull("status")
                || !response.hasNonNull("preapproval_id") || !response.hasNonNull("transaction_amount")) {
            throw new IllegalStateException("Mercado Pago não retornou os dados da cobrança recorrente.");
        }
        String approvedAt = response.path("date_approved").asText(null);
        java.time.LocalDateTime paidAt = approvedAt == null || approvedAt.isBlank()
                ? null : java.time.OffsetDateTime.parse(approvedAt).toLocalDateTime();
        return new ProviderPayment(
                response.get("id").asText(),
                response.get("preapproval_id").asText(),
                response.get("status").asText(),
                response.get("transaction_amount").decimalValue(),
                response.path("currency_id").asText("BRL"),
                paidAt
        );
    }

    private void requireConfiguration() {
        if (!isConfigured()) {
            throw new IllegalStateException("A integração Mercado Pago não está configurada no servidor.");
        }
    }

    private void requireAccessToken() {
        if (accessToken.isBlank()) {
            throw new IllegalStateException("A integração Mercado Pago não está configurada no servidor.");
        }
    }

    private void authorize(HttpHeaders headers) {
        headers.setBearerAuth(accessToken);
    }

    public record Checkout(String id, String url) { }
    public record ProviderSubscription(String status, String externalReference, BigDecimal amount) { }
    public record ProviderPayment(
            String id,
            String preapprovalId,
            String status,
            BigDecimal amount,
            String currency,
            java.time.LocalDateTime paidAt
    ) { }
}
