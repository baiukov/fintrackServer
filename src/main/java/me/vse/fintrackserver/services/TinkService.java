package me.vse.fintrackserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Objects;

@Service
public class TinkService {

    private static final String TINK_API_URL = "https://link.tink.com/1.0/";
    private final OkHttpClient client = new OkHttpClient();

    private String activeAccessToken;

    @Value("${tink.client-id}")
    private String clientId;

    @Value("${tink.client-secret}")
    private String clientSecret;

    @Value("${tink.redirect-uri}")
    private String redirectUri;

    public String generateAuthorizationUrl(String userId) {
        String url = TINK_API_URL + "account-check";
        String redirectUri = this.redirectUri + "?userId=" + userId;
        return url + "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=accounts:read,transactions:read" +
                "&locale=en_US";
    }

    @Scheduled(fixedRate = 29 * 60 * 1000)
    @PostConstruct
    private void getAccessToken() throws IOException {
        String url = "https://api.tink.com/api/v1/oauth/token";
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("client_id", "beebf6c7d60d4bb78dc5aea9b47f7846")
                .add("client_secret", "5f0ebd6461bb459a832851d73c394c5a")
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Response error: " + response);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            activeAccessToken = jsonNode.get("access_token").asText();
        }

    }
}
