package com.kobi.smartbot.integrations.weather.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CurrentWeatherSystemResponse {

    @JsonProperty("country")
    private String countryCode;

    @JsonProperty("sunrise")
    private Integer sunriseEpochSeconds;

    @JsonProperty("sunset")
    private Integer sunsetEpochSeconds;

}
