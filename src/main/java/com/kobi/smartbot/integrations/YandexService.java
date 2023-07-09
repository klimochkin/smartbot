package com.kobi.smartbot.integrations;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class YandexService {

    public byte[] speech(String text) throws IOException {
        String uri = "http://tts.voicetech.yandex.net/generate?text="
                + URLEncoder.encode(text, "utf-8")
                + "&format=mp3"
                + "&lang=ru-RU"
                + "&speaker=oksana"
//                + "&speaker=alena"
                + "&emotion=good"
                + "&key=f540e9a9-9641-446c-a0bc-916ce0ecb9ea";

        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) (url.openConnection());
        conn.setRequestMethod("GET");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream input = conn.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) >= 0) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }
}
