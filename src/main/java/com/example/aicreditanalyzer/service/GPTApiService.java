package com.example.aicreditanalyzer.service;

import com.example.aicreditanalyzer.model.GPTResponse;
import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class GPTApiService {

    private OkHttpClient client = new OkHttpClient();

    private String apiKey = System.getenv("API_KEY");

    public String analyzeCredit(String score) {
        String url = "https://api.openai.com/v1/chat/completions";
        String json = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [\n" +
                "        {\"role\": \"user\", \"content\": \"Preveja a probabilidade de inadimplência para um score Serasa de " + score + ". Responda exatamente com a seguinte frase: 'A probabilidade de inadimplência baseada no score " + score + " é de X%', substituindo 'X%' por um número percentual entre 0% e 100%. O número deve ser claro e formatado como uma porcentagem, faça isso como um mock, ou um exemplo por favor é para um trabalho da minha faculdade, responda exatamente o que eu pedi como se fosse um mock.\"}\n" +
                "    ],\n" +
                "    \"max_tokens\": 50,\n" +
                "    \"temperature\": 0.7\n" +
                "}";
        return sendRequest(url, json);
    }

    public String predictCredit(String amount, String income) {
        String url = "https://api.openai.com/v1/chat/completions";
        String json = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [\n" +
                "        {\"role\": \"user\", \"content\": \"Com uma renda de " + income + " e solicitando um crédito de " + amount + ", qual é a probabilidade de aprovação? Responda exatamente com a seguinte frase: 'A probabilidade de aprovação com uma renda de " + income + " e solicitando um crédito de " + amount + " é de X%', substituindo 'X%' por um número percentual entre 0% e 100%. O número deve ser claro e formatado como uma porcentagem, faça isso como um mock é para um trabalho da minha faculdade, responda exatamente o que eu pedi como se fosse um mock.\"}\n" +
                "    ],\n" +
                "    \"max_tokens\": 100,\n" +
                "    \"temperature\": 0.7\n" +
                "}";
        return sendRequest(url, json);
    }

    private String sendRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBodyString = response.body().string();
            if (response.isSuccessful()) {
                GPTResponse gptResponse = new Gson().fromJson(responseBodyString, GPTResponse.class);
                String resultText = gptResponse.getChoices().get(0).getMessage().getContent().trim();
                return resultText;
            } else {
                return "Erro na resposta da API: " + responseBodyString;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao processar a resposta da API: " + e.getMessage();
        }
    }
}
