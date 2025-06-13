package me.vse.fintrackserver.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * # Model webového push odběru
 * Obsahuje data potřebná pro odesílání push notifikací
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    /** Koncový bod pro doručení notifikace */
    private String endpoint;
    
    /** Šifrovací klíče */
    private Keys keys;

    /**
     * # Třída pro šifrovací klíče
     * Obsahuje klíče pro zabezpečení notifikací
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keys {
        /** VAPID veřejný klíč */
        private String p256dh;
        
        /** Autentizační klíč */
        private String auth;
    }
} 