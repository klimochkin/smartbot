package com.kobi.smartbot.integrations.weather;

import com.kobi.smartbot.integrations.weather.enums.Country;
import com.kobi.smartbot.integrations.weather.enums.WeatherCondition;
import com.kobi.smartbot.integrations.weather.response.CurrentWeatherIndicatorsResponse;
import com.kobi.smartbot.integrations.weather.response.CurrentWeatherResponse;
import com.kobi.smartbot.integrations.weather.response.CurrentWeatherSystemResponse;
import com.kobi.smartbot.integrations.weather.response.CurrentWeatherWindResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


@Component
public class WeatherMessageFormatter {

    private static final String WEATHER_PATTERN = """
            *%s* %s
            %s %s
            Температура: %s°C
            Ощущается как: %s°C
            Ветер: %s %s м/c
            Давление: %s мм рт.ст.
            Влажность: %s%%
            Рассвет: %s
            Закат: %s
            """;

    private final WindProvider windProvider;


    @Autowired
    public WeatherMessageFormatter(WindProvider windProvider) {
        this.windProvider = windProvider;
    }


    public String formatMessage(CurrentWeatherResponse weatherResponse) {
        String providedWeatherCondition = weatherResponse.getConditions().get(0).getCondition().toUpperCase();
        WeatherCondition weatherCondition = WeatherCondition.valueOf(providedWeatherCondition);

        CurrentWeatherWindResponse providedWind = weatherResponse.getWind();
        WindDto wind = windProvider.getWind(providedWind.getDegree(), providedWind.getSpeed());

        CurrentWeatherIndicatorsResponse weatherMain = weatherResponse.getIndicators();
        CurrentWeatherSystemResponse weatherSystem = weatherResponse.getSystem();
        Integer timezoneSeconds = weatherResponse.getTimezoneSeconds();

        return String.format(WEATHER_PATTERN,
                weatherResponse.getCityName(),
                Country.getByName(weatherSystem.getCountryCode()).getEmoji(),
                weatherCondition.getDescription(),
                weatherCondition.getEmoji(),
                weatherMain.getTemperature(),
                weatherMain.getTemperatureFeeling(),
                wind.getDirection().getEmoji(),
                wind.getSpeed(),
                weatherMain.getPressure() * 3 / 4,
                weatherMain.getHumidity(),
                getZonedTime(weatherSystem.getSunriseEpochSeconds(), timezoneSeconds),
                getZonedTime(weatherSystem.getSunsetEpochSeconds(), timezoneSeconds)
        );
    }

    private String getZonedTime(Integer secs, Integer zoneSecs) {
        Instant instant = Instant.ofEpochSecond(secs);
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(zoneSecs);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneOffset);
        return zonedDateTime.toLocalTime().toString();
    }
}
