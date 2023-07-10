package com.kobi.smartbot.integrations;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kobi.smartbot.config.ExternalApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class OpenExchangeRatesService {

    private static final String API_URL = "https://openexchangerates.org/api/latest.json?app_id=397f24bb1dba4f41b7d7ee5942afd855";
    private static final Map<String, String> CURRENCY_NAMES = new HashMap<>() {{
        put("USD", "Доллар");
        put("EUR", "Евро");
        put("JPY", "Япон. иена");
        put("GBP", "Фунт");
        put("CNY", "Юань");
        put("AED", "Дирхам ОАЭ");
        put("TRY", "Тур. лира");
        put("BYN", "Бел. рубль");
        put("UAH", "Гривна");
    }};

    private final RestTemplate restTemplate;
    private final ExternalApiProperties apiProperties;

    @Autowired
    public OpenExchangeRatesService(RestTemplate restTemplate, ExternalApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public String getExchangeRates() {
        String API_URL = "https://openexchangerates.org/api/latest.json?app_id="+apiProperties.getOpenExchangeRatesApiKey();
        String response = restTemplate.getForObject(API_URL, String.class);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        JsonObject rates = jsonObject.getAsJsonObject("rates");

        double usdToRub = rates.get("RUB").getAsDouble();
        StringBuilder result = new StringBuilder("Актуальные курсы валют:\n\n");

        Set<Map.Entry<String, JsonElement>> entries = rates.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String currencyCode = entry.getKey();
            if (CURRENCY_NAMES.containsKey(currencyCode)) {
                double rateToUsd = entry.getValue().getAsDouble();
                double rateToRub = (1 / rateToUsd) * usdToRub;
                result.append(String.format("%s(%s)=%.2f ₽\n", CURRENCY_NAMES.get(currencyCode), currencyCode, rateToRub));
            }
        }

        double btcToUsd = 1 / rates.get("BTC").getAsDouble();
        result.append("--\n");
        result.append(String.format("Биткоин (BTC) = %.2f $", btcToUsd));

       return result.toString();
    }
}
