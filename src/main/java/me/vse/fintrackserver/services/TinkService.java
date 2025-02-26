package me.vse.fintrackserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.model.User;
import okhttp3.*;
import org.h2.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Objects;

@Service
public class TinkService {

    private static final String TINK_API_URL = "https://link.tink.com/1.0/account-check";
    private static final String TINK_TOKEN_URL = "https://api.tink.com/api/v1/oauth/token";
    private static final String TINK_ACCOUNT_REPORT_URL = "https://api.tink.com/api/v1/account-verification-reports/";


    private final OkHttpClient client = new OkHttpClient();

    private String activeAccessToken;

    @Value("${tink.client-id}")
    private String clientId;

    @Value("${tink.client-secret}")
    private String clientSecret;

    @Value("${tink.redirect-uri}")
    private String redirectUri;

    @Autowired
    private EntityManager entityManager;

    public String generateAuthorizationUrl(String userId) {
        String url = TINK_API_URL;
        String redirectUri = this.redirectUri + "?userId=" + userId;
        return url + "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=accounts:read,transactions:read,account-verification-reports:read" +
                "&locale=en_US" +
                "&market=CZ";
    }

    @Scheduled(fixedRate = 29 * 60 * 1000)
    @PostConstruct
    private void getAccessToken() throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "client_credentials")
                .add("scope", "account-verification-reports:read")
                .build();

        Request request = new Request.Builder()
                .url(TINK_TOKEN_URL)
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
            System.out.println(activeAccessToken);
        }

    }

    public void fetchAccount(String userId, String accountVerificationReportId) throws IOException {

//        User user = entityManager.find(User.class, userId);
//        if (user == null) return;

        Request request = new Request.Builder()
                .url(TINK_ACCOUNT_REPORT_URL + accountVerificationReportId)
                .get()
                .addHeader("Authorization", "Bearer " + activeAccessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch report: " + response);
            }
            System.out.println(userId + " " + response.body().string());
        }
    }
}
