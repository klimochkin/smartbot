package com.kobi.smartbot.model.enums;


public enum CommandEnum implements MessageTypeEnum {

    DVACH       ("двач",    "👉 двач - случпйный мем с двача", "vk"),
    MEM         ("мем",     "👉 мем - случайный мем", "vk"),
    VIDEO       ("видео",   "👉 видео <текст для поиска> - поиск видео", "vk"),
    GIF         ("гиф",     "👉 гиф <текст для поиска> - поиск гифки", "vk"),
    DETECTOR    ("чсв",     "👉 чсв - детектор самолюбия", "vk"),
    ONLINE      ("онлайн",  "👉 онлайн - список участников беседы \"онлайн\"", "vk"),
    SEARCH      ("найди",   "👉 найди <текст для поиска> - поиск по интернету", "all"),
    SAY         ("скажи",   "👉 скажи <текст> - озвучка текста", "all"),
    WEATHER     ("погода",  "👉 погода <город> - прогноз погоды", "all"),
    RATES       ("валюта",  "👉 валюта - текущий курс основных валют", "all"),
    IMAGES      ("пик",     "👉 пик <текст с описанием> - генерирует картинку по текстовому описанию", "all"),
    RESET       ("сброс",   "👉 сброс - сброс диалога, бот забудет историю вашей переписки с ним", "all"),
    COMMANDS    ("команды", "👉 команды - список команд", "all");


    private final String code;
    private final String description ;
    private final String type;

    CommandEnum(String inputCode, String inputDescription, String inputType) {
        this.code = inputCode;
        this.description = inputDescription;
        this.type = inputType;
    }

    public String getCode(){
        return this.code;
    }

    public String getDescription(){
        return this.description;
    }

    public boolean equalsCode(String liter) {
        return code.equals(liter);
    }

    public String getType() {
        return type;
    }
}
