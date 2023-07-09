package com.kobi.smartbot.integrations.weather;

import com.kobi.smartbot.integrations.weather.enums.WindDirection;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class WindDto {

    private WindDirection direction;
    private Integer speed;

}
