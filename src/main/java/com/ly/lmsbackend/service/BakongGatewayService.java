package com.ly.lmsbackend.service;

import com.ly.lmsbackend.config.BakongProperties;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BakongGatewayService {

    private static final Logger log = LoggerFactory.getLogger(BakongGatewayService.class);

    public record KhqrGenerationResult(
            String qrCode,
            String md5,
            String deepLink,
            String currency
    ) {}

    public enum TransactionLookupState {
        PAID,
        PENDING,
        TOKEN_INVALID,
        ERROR
    }

    public record BakongTransactionCheckResult(
            TransactionLookupState state,
            String hash,
            String fromAccountId,
            String toAccountId,
            String currency,
            BigDecimal amount,
            String rawMessage
    ) {}

    private final BakongProperties properties;
    private final RestClient restClient;

    public BakongGatewayService(
            BakongProperties properties,
            @Qualifier("bakongRestClient") RestClient restClient
    ) {
        this.properties = properties;
        this.restClient = restClient;
    }

    public boolean isConfigured() {
        String token = properties.getApi().getToken();
        String accountId = properties.getAccountId();
        return token != null && !token.isBlank()
                && accountId != null && !accountId.isBlank();
    }

    public void ensureConfigured() {
        if (!isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Bakong payment gateway is not configured on this server. Set BAKONG_API_TOKEN and BAKONG_ACCOUNT_ID to enable Bakong KHQR payments."
            );
        }
    }

    public String getConfiguredAccountId() {
        return properties.getAccountId() == null ? "" : properties.getAccountId().trim();
    }

    public String getConfiguredCurrency() {
        return "KHR".equalsIgnoreCase(properties.getCurrency()) ? "KHR" : "USD";
    }

    public int getQrExpirationMinutes() {
        return Math.max(1, properties.getQrExpirationMinutes());
    }

    public KhqrGenerationResult createDynamicPayment(BigDecimal amount, String billReference, Instant expiresAt) {
        ensureConfigured();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bakong payment amount must be greater than zero");
        }

        KHQRCurrency khqrCurrency = "KHR".equalsIgnoreCase(getConfiguredCurrency())
                ? KHQRCurrency.KHR
                : KHQRCurrency.USD;
        int scale = khqrCurrency == KHQRCurrency.KHR ? 0 : 2;
        double normalizedAmount = amount.setScale(scale, RoundingMode.HALF_UP).doubleValue();

        String accountName = truncateOrDefault(properties.getAccountName(), "Lumen LMS", 25);
        String city = truncateOrDefault(properties.getCity(), "Phnom Penh", 15);
        String billNumber = truncateOrDefault(billReference, "LMS-PAY", 25);
        long expirationMillis = expiresAt.toEpochMilli();

        // NBC KHQR SDK uses IndividualInfo for Bakong Account ID payments; setMerchantName and
        // setMerchantCity populate the EMVCo display name (tag 59) and city (tag 60).
        IndividualInfo info = new IndividualInfo();
        info.setBakongAccountId(getConfiguredAccountId());
        info.setCurrency(khqrCurrency);
        info.setAmount(normalizedAmount);
        info.setMerchantName(accountName);
        info.setMerchantCity(city);
        info.setBillNumber(billNumber);
        info.setExpirationTimestamp(expirationMillis);

        KHQRResponse<KHQRData> khqrResponse = BakongKHQR.generateIndividual(info);

        if (khqrResponse == null
                || khqrResponse.getKHQRStatus() == null
                || khqrResponse.getKHQRStatus().getCode() != 0
                || khqrResponse.getData() == null
                || khqrResponse.getData().getQr() == null
                || khqrResponse.getData().getMd5() == null) {
            String message = (khqrResponse != null && khqrResponse.getKHQRStatus() != null)
                    ? khqrResponse.getKHQRStatus().getMessage()
                    : "Unknown KHQR SDK error";
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to generate Bakong KHQR payload: " + message
            );
        }

        String qrCode = khqrResponse.getData().getQr();
        String md5 = khqrResponse.getData().getMd5();
        String deepLink = generateDeepLink(qrCode);

        return new KhqrGenerationResult(qrCode, md5, deepLink, getConfiguredCurrency());
    }

    public String generateDeepLink(String qrCode) {
        if (!isConfigured() || qrCode == null || qrCode.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("qr", qrCode);

            Map<String, String> sourceInfo = new LinkedHashMap<>();
            String appName = trimOrNull(properties.getDeeplink().getAppName());
            String appIconUrl = trimOrNull(properties.getDeeplink().getAppIconUrl());
            String callbackUrl = trimOrNull(properties.getDeeplink().getCallbackUrl());

            if (appIconUrl != null && appName != null && callbackUrl != null) {
                sourceInfo.put("appIconUrl", appIconUrl);
                sourceInfo.put("appName", appName);
                sourceInfo.put("appDeepLinkCallback", callbackUrl);
                body.put("sourceInfo", sourceInfo);
            }

            String url = normalizedBaseUrl() + "/v1/generate_deeplink_by_qr";
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApi().getToken().trim())
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                return null;
            }
            int responseCode = asInt(response.get("responseCode"), -1);
            if (responseCode == 0 && response.get("data") instanceof Map<?, ?> dataMap) {
                Object shortLink = dataMap.get("shortLink");
                if (shortLink != null && !shortLink.toString().isBlank()) {
                    return shortLink.toString();
                }
            }
            log.debug("Bakong deeplink generation did not return a shortLink (responseCode={}): {}",
                    responseCode, response.get("responseMessage"));
            return null;
        } catch (Exception ex) {
            log.warn("Non-fatal error generating Bakong deeplink (QR payment still works): {}", ex.getMessage());
            return null;
        }
    }

    public BakongTransactionCheckResult checkTransactionByMd5(String md5) {
        ensureConfigured();
        if (md5 == null || md5.isBlank()) {
            return new BakongTransactionCheckResult(TransactionLookupState.ERROR, null, null, null, null, null, "Missing KHQR MD5");
        }

        String url = normalizedBaseUrl() + "/v1/check_transaction_by_md5";
        try {
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApi().getToken().trim())
                    .body(Map.of("md5", md5.trim()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                return new BakongTransactionCheckResult(TransactionLookupState.ERROR, null, null, null, null, null, "Empty Bakong API response");
            }

            int responseCode = asInt(response.get("responseCode"), -1);
            int errorCode = asInt(response.get("errorCode"), -1);
            String responseMessage = response.get("responseMessage") != null ? response.get("responseMessage").toString() : "";

            if (responseCode == 0 && response.get("data") instanceof Map<?, ?> dataMap) {
                String hash = asString(dataMap.get("hash"));
                String fromAccountId = asString(dataMap.get("fromAccountId"));
                String toAccountId = asString(dataMap.get("toAccountId"));
                String currency = asString(dataMap.get("currency"));
                BigDecimal amount = asBigDecimal(dataMap.get("amount"));
                return new BakongTransactionCheckResult(
                        TransactionLookupState.PAID,
                        hash,
                        fromAccountId,
                        toAccountId,
                        currency,
                        amount,
                        responseMessage
                );
            }

            if (errorCode == 6) {
                log.error("Bakong Open API reported expired/invalid token (errorCode=6): {}", responseMessage);
                return new BakongTransactionCheckResult(TransactionLookupState.TOKEN_INVALID, null, null, null, null, null, responseMessage);
            }

            if (responseCode == 1 && errorCode == 1) {
                return new BakongTransactionCheckResult(TransactionLookupState.PENDING, null, null, null, null, null, responseMessage);
            }

            log.debug("Bakong check_transaction_by_md5 returned responseCode={}, errorCode={}, msg={}", responseCode, errorCode, responseMessage);
            return new BakongTransactionCheckResult(TransactionLookupState.PENDING, null, null, null, null, null, responseMessage);
        } catch (RestClientResponseException httpEx) {
            if (httpEx.getStatusCode().value() == 401 || httpEx.getStatusCode().value() == 403) {
                log.error("Bakong Open API rejected credentials with HTTP {}: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
                return new BakongTransactionCheckResult(TransactionLookupState.TOKEN_INVALID, null, null, null, null, null, httpEx.getMessage());
            }
            log.warn("Bakong check_transaction_by_md5 HTTP {}: {}", httpEx.getStatusCode(), httpEx.getMessage());
            return new BakongTransactionCheckResult(TransactionLookupState.ERROR, null, null, null, null, null, httpEx.getMessage());
        } catch (Exception ex) {
            log.warn("Bakong check_transaction_by_md5 request failed: {}", ex.getMessage());
            return new BakongTransactionCheckResult(TransactionLookupState.ERROR, null, null, null, null, null, ex.getMessage());
        }
    }

    private String normalizedBaseUrl() {
        String base = properties.getApi().getBaseUrl();
        if (base == null || base.isBlank()) {
            return "https://api-bakong.nbc.gov.kh";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base.trim();
    }

    private static String truncateOrDefault(String value, String fallback, int maxLen) {
        String cleaned = (value == null || value.isBlank()) ? fallback : value.trim();
        return cleaned.length() > maxLen ? cleaned.substring(0, maxLen) : cleaned;
    }

    private static String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static int asInt(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();
        if (val == null) return fallback;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String asString(Object val) {
        return val == null ? null : val.toString();
    }

    private static BigDecimal asBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
