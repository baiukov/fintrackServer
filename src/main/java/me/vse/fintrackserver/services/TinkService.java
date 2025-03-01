package me.vse.fintrackserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.bankIdentity.Account;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.rest.responses.BankAccountsResponse;
import okhttp3.*;
import org.h2.util.json.JSONArray;
import org.h2.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
public class TinkService {

    private static final String TINK_API_URL = "https://link.tink.com/1.0/authorize";
    private static final String TINK_TOKEN_URL = "https://api.tink.com/api/v1/oauth/token";
    private static final String TINK_ACCOUNT_REPORT_URL = "https://api.tink.com/api/v1/account-verification-reports/";

    private static final String FETCH_ACCOUNTS_URL = "https://api.tink.com/data/v2/accounts";

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

    @Autowired
    private AccountRepository accountRepository;

    private Map<String, BankAccountsResponse> pendingAccounts = new HashMap<>();

    public String generateAuthorizationUrl(String accountId) {
        String url = TINK_API_URL;
        String redirectUri = this.redirectUri + "?userId=" + accountId;
        pendingAccounts.put(accountId, new BankAccountsResponse(false, null, null));
        return url + "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=transactions:read,account-verification-reports:read,accounts:read,balances:read" +
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
                .add("scope", "authorization:grant,user:create")
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
        }

    }

    public void handleCode(String accountId, String code) throws IOException {

        if (!pendingAccounts.containsKey(accountId)) {
            return;
        }

        me.vse.fintrackserver.model.Account account = entityManager.find(
                me.vse.fintrackserver.model.Account.class,
                accountId
        );
        if (account == null) return;

        String userAccessToken;
        try {
            userAccessToken = exchangeAuthorizationCodeWithUserAccessToken(code);
        } catch (IOException e) {
            pendingAccounts.put(accountId, new BankAccountsResponse(
                    true,
                    ErrorMessages.COULD_NOT_HANDLE_ACCESS_CODE.name(),
                    null)
            );
            return;
        }


        List<Account> accounts;
        try {
            accounts = fetchUserBankAccounts(userAccessToken);
        } catch (Exception e) {
            pendingAccounts.put(accountId, new BankAccountsResponse(
                    true,
                    ErrorMessages.COULD_NOT_FETCH_ACCOUNTS.name(),
                    null)
            );
            return;
        }

        pendingAccounts.put(accountId, new BankAccountsResponse(true, null, accounts));
    }

    protected String exchangeAuthorizationCodeWithUserAccessToken(String authorizationCode) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "authorization_code")
                .add("code", authorizationCode)
                .build();

        Request request = new Request.Builder()
                .url(TINK_TOKEN_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch code: " + response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            return jsonNode.get("access_token").asText();
        } catch (IOException e) {
            throw new IOException("Failed to fetch code: " + e);
        }
    }

    protected List<Account> fetchUserBankAccounts(String userAccessToken) throws IllegalArgumentException {
        Request request = new Request.Builder()
                .url(FETCH_ACCOUNTS_URL)
                .addHeader("Authorization", "Bearer " + userAccessToken)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch accounts: " + response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            return mapAccountResponse(jsonNode);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to fetch accounts: " + e);
        }
    }

    protected List<Account> mapAccountResponse(JsonNode response) {
        JsonNode accountsNode = response.get("accounts");
        if (accountsNode == null) {
            return new ArrayList<>();
        }

        List<Account> accounts = new ArrayList<>();

        accountsNode.elements().forEachRemaining(accountNode -> {
            String accountId = accountNode.get("id").asText();
            String accountName = accountNode.get("name").asText();

            JsonNode amount = accountNode.get("balances").get("available").get("amount");
            JsonNode balanceNode = amount.get("value");
            String currency = amount.get("currencyCode").asText();

            int scale = balanceNode.get("scale").asInt();
            double balance = balanceNode.get("unscaledValue").asDouble() / Math.pow(10, scale);

            accounts.add(Account.builder()
                    .name(accountName)
                    .balance(balance)
                    .currency(currency)
                    .build());
        });

        return accounts;

    }

    public BankAccountsResponse getAccounts(String userId) throws IOException {
        BankAccountsResponse response = pendingAccounts.get(userId);
        if (response == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        return response;
    }

}
