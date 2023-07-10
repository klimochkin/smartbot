package com.kobi.smartbot.integrations.weather;

import com.kobi.smartbot.config.ExternalApiProperties;
import com.kobi.smartbot.integrations.weather.response.CurrentWeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenWeatherMapService {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWeatherMapService.class);

    private final WeatherMessageFormatter weatherMessageFormatter;
    private final ExternalApiProperties apiProperties;

    @Autowired
    public OpenWeatherMapService(WeatherMessageFormatter weatherMessageFormatter, ExternalApiProperties apiProperties) {
        this.weatherMessageFormatter = weatherMessageFormatter;
        this.apiProperties = apiProperties;
    }

    public String getWeather(final String cityName) {
        String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=%s&lang=ru&units=metric&appid=%s";

        String url = String.format(weatherUrl, cityName, apiProperties.getOpenWeatherMapApiKey());
        CurrentWeatherResponse currentWeatherResponse = getForObject(url, CurrentWeatherResponse.class);
        return weatherMessageFormatter.formatMessage(currentWeatherResponse);
    }

    private <T> T getForObject(String url, Class<T> clazz) {
        RestTemplate restTemplate = new RestTemplate();
        T response = restTemplate.getForObject(url, clazz);
        if (response == null) {
            LOG.error("Empty result from [{}].", url);
        }
        return response;
    }
}
