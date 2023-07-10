package com.kobi.smartbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "externalapi")
public class ExternalApiProperties {

    private String yandexApiKey;
    private String openExchangeRatesApiKey;
    private String openWeatherMapApiKey;

    public String getYandexApiKey() {
        return yandexApiKey;
    }

    public void setYandexApiKey(String yandexApiKey) {
        this.yandexApiKey = yandexApiKey;
    }

    public String getOpenExchangeRatesApiKey() {
        return openExchangeRatesApiKey;
    }

    public void setOpenExchangeRatesApiKey(String openExchangeRatesApiKey) {
        this.openExchangeRatesApiKey = openExchangeRatesApiKey;
    }

    public String getOpenWeatherMapApiKey() {
        return openWeatherMapApiKey;
    }

    public void setOpenWeatherMapApiKey(String openWeatherMapApiKey) {
        this.openWeatherMapApiKey = openWeatherMapApiKey;
    }
}
