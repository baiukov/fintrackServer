package me.vse.fintrackserver.controller;

import lombok.RequiredArgsConstructor;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.services.AccountService;
import me.vse.fintrackserver.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * # REST kontrolér pro analytické funkce
 * Poskytuje endpointy pro analýzu finančních dat účtů
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AccountService accountService;

    /**
     * # Spuštění komplexní analýzy pro konkrétní účet
     * @param accountId ID účtu pro analýzu
     * @param userId ID přihlášeného uživatele
     * @param language Jazyk pro doporučení (en/cz), výchozí je cz
     * @return Náhodné finanční doporučení jako text
     */
    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeAccount(
            @RequestParam String accountId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "cz") String language) {
        User user = analyticsService.getUserById(userId);
        Account account = accountService.checkUserAccountAccess(accountId, user);
        String advice = analyticsService.getRandomFinancialAdvice(account, language);
        return ResponseEntity.ok(advice);
    }

} 