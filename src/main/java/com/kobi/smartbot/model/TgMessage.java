package com.kobi.smartbot.model;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.util.List;

public class TgMessage extends AbstractMessage{

    private List<InputMedia> photos;
    private InputFile inputFile;

    public List<InputMedia> getPhotos() {
        return photos;
    }

    public void setPhotos(List<InputMedia> photos) {
        this.photos = photos;
    }

    public InputFile getInputFile() {
        return this.inputFile;
    }

    public void setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }
}
