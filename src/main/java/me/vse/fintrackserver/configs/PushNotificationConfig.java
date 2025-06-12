package me.vse.fintrackserver.configs;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
@RequiredArgsConstructor
public class PushNotificationConfig {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Value("${push-notification.public-key}")
    private String publicKey;

    @Value("${push-notification.private-key}")
    private String privateKey;

    @Value("${push-notification.email}")
    private String email;


    @Bean
    public PushService pushService() throws GeneralSecurityException {
        return new PushService(
            publicKey,
            privateKey,
            email
        );
    }
}