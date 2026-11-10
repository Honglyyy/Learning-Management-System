package com.ly.lmsbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bakong")
public class BakongProperties {

    private final Api api = new Api();
    private final Deeplink deeplink = new Deeplink();

    private String accountId = "";
    private String accountName = "Lumen LMS";
    private String city = "Phnom Penh";
    private String currency = "USD";
    private int qrExpirationMinutes = 15;

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://api-bakong.nbc.gov.kh";
        private String token = "";
    }

    @Getter
    @Setter
    public static class Deeplink {
        private String appName = "Lumen LMS";
        private String appIconUrl = "";
        private String callbackUrl = "";
    }
}
