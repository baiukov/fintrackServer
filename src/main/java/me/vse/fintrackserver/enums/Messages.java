package me.vse.fintrackserver.enums;

import java.util.HashMap;
import java.util.Map;

public class Messages {

    private final Map<String, String> english = new HashMap<>();
    private final Map<String, String> czech = new HashMap<>();

    private void init() {

        english.put("PASSWORD_RECOVERY", "Password recovery");
        english.put("ENTER_CODE", "Enter the code below into application's form");

        czech.put("PASSWORD_RECOVERY", "Obnova hesla");
        czech.put("ENTER_CODE", "Zadejte níže uvedený kód do formuláře aplikace");

    }

    private static Messages instance;

    private Messages() {
        init();
    }

    public static Messages getInstance() {
        if (instance == null) {
            instance = new Messages();
        }
        return instance;
    }

    public String get(String language, String key) {
        return switch (language) {
            case "en" -> english.get(key);
            case "cz" -> czech.get(key);
            default -> null;
        };

    }
}
