package me.vse.fintrackserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.vse.fintrackserver.models.Subscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * # Služba pro správu webových push notifikací
 * - Správa odběrů uživatelů
 * - Odesílání notifikací
 * - Zabezpečená komunikace
 */
@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private final PushService pushService;
    private final ObjectMapper objectMapper;


    private final Map<String, Subscription> userSubscriptions = new ConcurrentHashMap<>();

    /**
     * # Uložení odběru pro uživatele
     * @param userId ID uživatele
     * @param subscription Data odběru
     */
    public void saveUserSubscription(String userId, Subscription subscription) {
        userSubscriptions.put(userId, subscription);
    }

    /**
     * # Získání odběru pro uživatele
     * @param userId ID uživatele
     * @return Uložená data odběru
     */
    public Subscription getUserSubscription(String userId) {
        return userSubscriptions.get(userId);
    }

    /**
     * # Odeslání notifikace
     * @param subscription Data odběru
     * @param message Obsah zprávy
     */
    public void sendNotification(Subscription subscription, String message) {
        try {
            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getKeys().getP256dh(),
                subscription.getKeys().getAuth(),
                message.getBytes()
            );
            pushService.send(notification);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send push notification", e);
        }
    }

    /**
     * # Zpracování JSON dat odběru
     * @param subscriptionJson JSON řetězec s daty odběru
     * @return Objekt odběru
     */
    public Subscription subscribe(String subscriptionJson) {
        try {
            return objectMapper.readValue(subscriptionJson, Subscription.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse subscription data", e);
        }
    }
} 