package dev.kingrabbit.website;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class HttpUtil {

    public static CompletableFuture<String> sendGetRequestAsync(String urlString, String[] headers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Set the headers
                for (String header : headers) {
                    String[] parts = header.split(":");
                    String name = parts[0];
                    String value = parts[1];
                    connection.setRequestProperty(name, value);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                // Return the response
                return response.toString();
            } catch (IOException e) {
                // Handle the exception
                e.printStackTrace();
                return null;
            }

        });
    }

}
