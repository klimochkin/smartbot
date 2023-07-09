package com.kobi.smartbot.service.tg;

import com.kobi.smartbot.integrations.gpt.response.ImageGenerationResponse;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.ArrayList;
import java.util.List;

@Service
public class TgCommandService {


    public List<InputMedia> getPhotos(ImageGenerationResponse response) {
        List<InputMedia> photos = new ArrayList<>();
        for (ImageGenerationResponse.ImageData imageData : response.getData()) {
            InputMediaPhoto photo = new InputMediaPhoto();
            photo.setMedia(imageData.getUrl());
            photos.add(photo);
        }
        return photos;
    }


}
