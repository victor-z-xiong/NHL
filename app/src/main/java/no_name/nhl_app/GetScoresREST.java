package no_name.nhl_app;

/**
 * Created by user on 2018-04-20.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import org.json.JSONObject;

public class GetScoresREST {
    public static void getScores() {

        try {
            
            URL url = new URL("https://statsapi.web.nhl.com/api/v1/schedule");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output, jsonString;

            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(br).getAsJsonArray();

            System.out.println(array.size());

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
}
