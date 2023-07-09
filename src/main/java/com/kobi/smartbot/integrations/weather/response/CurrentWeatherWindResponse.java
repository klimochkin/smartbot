package com.kobi.smartbot.integrations.weather.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CurrentWeatherWindResponse {

    private Integer speed; // m/s

    @JsonProperty("deg")
    private Integer degree;

}
