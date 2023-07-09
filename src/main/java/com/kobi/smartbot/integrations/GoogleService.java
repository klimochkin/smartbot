package com.kobi.smartbot.integrations;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleService {

    public String googleSearch(String textSearch) throws GeneralSecurityException, IOException {

        // String cx = "002845322276752338984:vxqzfa86nqc"; //запасной движок

        StringBuilder answerBild = new StringBuilder();
        answerBild.append("Вот что я нашел: \n");
        String cx = "007141834458377101389:35ukoc4tzi0";

        Customsearch cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                .setApplicationName("MyApplication")
                .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer("AIzaSyD05ev90XC5rtBYlcSTl5r8JvkNZ-fbo-c"))
                .build();

        Customsearch.Cse.List list = cs.cse().list(textSearch).setCx(cx);
        Search result = list.execute();
        int i = 0;
        if (result.getItems() != null) {
            for (Result ri : result.getItems()) {
                answerBild.append(ri.getTitle()).append(", ").append(ri.getLink());

                i++;
                if (i > 3)
                    break;

                answerBild.append("\n\n**********\n");
            }
        }
        return answerBild.toString();
    }

}
