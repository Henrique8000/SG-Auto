package com.sgauto.app.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CepUtil {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record EnderecoCep(String logradouro, String bairro, String cidade, String uf) {}

    public static void buscarCepAsync(String cep, Consumer<EnderecoCep> callback) {
        if (cep == null) return;

        String cepLimpo = cep.replaceAll("[^0-9]", "");
        if (cepLimpo.length() != 8) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://viacep.com.br/ws/" + cepLimpo + "/json/"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode node = MAPPER.readTree(response.body());

                    if (!node.has("erro")) {
                        EnderecoCep endereco = new EnderecoCep(
                                node.path("logradouro").asText(""),
                                node.path("bairro").asText(""),
                                node.path("localidade").asText(""),
                                node.path("uf").asText("")
                        );

                        Platform.runLater(() -> callback.accept(endereco));
                    }
                }
            } catch (Exception e) {
                System.out.println("Consulta de CEP indisponível no momento.");
            }
        });
    }
}