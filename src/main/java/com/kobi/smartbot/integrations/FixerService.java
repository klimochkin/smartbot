package com.kobi.smartbot.integrations;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FixerService {


    public String getFixer() throws IOException {
        String answer = null;

        URL url = new URL("http://data.fixer.io/api/latest?access_key=f3e85b51f76cbc18e373523ec8149b07&base=EUR");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
        JsonObject rates = jsonObject.getAsJsonObject("rates");

        if (rates != null) {
            String eur = String.valueOf(rates.get("RUB").getAsBigDecimal());
            answer = "Евро = " + eur + " руб." +
                    "\n40 евро = " + Float.parseFloat(eur) * 40 + " руб.";
        }

        connection.disconnect();
        return answer;
    }
}
