package com.andsantos.util.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientNative {
    private static final HttpClient client = HttpClient.newHttpClient();

    private HttpClientNative() {

    }

    public static void enviar(String url, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Erro HTTP: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar requisição: " + url + ": " + e.getMessage(), e);
        }
    }
}
