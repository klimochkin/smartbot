package com.kobi.smartbot.integrations.weather.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class WeatherConditionResponse {

    @JsonProperty("main")
    private String condition;

}
