package com.kobi.smartbot.integrations.weather;

import com.kobi.smartbot.integrations.weather.enums.WindDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


@Component
public class WindProvider {
    private static final Logger LOGGER = LogManager.getLogger(WindProvider.class);


    public WindDto getWind(Integer degree, Integer speed) {
        WindDirection windDirection = speed > 0 ? getWindDirection(degree) : WindDirection.DEFAULT;

        WindDto windDto = new WindDto();
        windDto.setDirection(windDirection);
        windDto.setSpeed(speed);
        return windDto;
    }


    private WindDirection getWindDirection(Integer degree) {
        if (degree <= 22)
            return WindDirection.NORTH;
        if (degree <= 67)
            return WindDirection.NORTH_EAST;
        if (degree <= 112)
            return WindDirection.EAST;
        if (degree <= 157)
            return WindDirection.SOUTH_EAST;
        if (degree <= 202)
            return WindDirection.SOUTH;
        if (degree <= 247)
            return WindDirection.SOUTH_WEST;
        if (degree <= 292)
            return WindDirection.WEST;
        if (degree <= 337)
            return WindDirection.NORTH_WEST;
        if (degree <= 360) {
            return WindDirection.NORTH;
        }

        LOGGER.error("Wrong wind degree [{}].", degree);
        throw new IllegalArgumentException();
    }

}
