package com.kobi.smartbot.integrations.weather.enums;

import lombok.Getter;


@Getter
public enum WindDirection {
    DEFAULT(""),
    NORTH("⬇"),
    NORTH_EAST("↙"),
    EAST("⬅"),
    SOUTH_EAST("↖"),
    SOUTH("⬆"),
    SOUTH_WEST("↗"),
    WEST("➡"),
    NORTH_WEST("↘");


    private final String emoji;


    WindDirection(String emoji) {
        this.emoji = emoji;
    }

}
