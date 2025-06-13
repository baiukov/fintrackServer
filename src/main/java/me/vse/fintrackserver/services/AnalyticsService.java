package me.vse.fintrackserver.services;

import lombok.RequiredArgsConstructor;
import me.vse.fintrackserver.enums.FinancialAdviceMessage;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Transaction;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.models.Subscription;
import me.vse.fintrackserver.repositories.TransactionRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * # Služba pro analýzu finančních transakcí
 * - Detekce anomálií
 * - Analýza kategorií
 * - Predikce výdajů
 * - Kontrola rozpočtu
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;

    /**
     * # Získání uživatele podle ID
     * @param userId ID uživatele
     * @return Uživatel
     * @throws IllegalArgumentException pokud uživatel neexistuje
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    /**
     * # Generování finančních doporučení
     * @param transactions Seznam transakcí
     * @param categoryTotals Součty podle kategorií
     * @param monthlyTotals Měsíční součty
     * @param averageMonthlyExpense Průměrné měsíční výdaje
     * @param language Jazyk pro doporučení (en/cz)
     * @return Seznam doporučení
     */
    private List<Map<String, Object>> generateFinancialAdvice(
            List<Transaction> transactions,
            Map<String, Double> categoryTotals,
            Map<String, Double> monthlyTotals,
            double averageMonthlyExpense,
            String language) {
        
        List<Map<String, Object>> advice = new ArrayList<>();

        // Analýza trendu výdajů
        if (!monthlyTotals.isEmpty()) {
            String currentMonth = LocalDateTime.now().getMonth().toString();
            double currentMonthTotal = monthlyTotals.getOrDefault(currentMonth, 0.0);
            
            if (currentMonthTotal > averageMonthlyExpense * 1.2) {
                double percentage = ((currentMonthTotal - averageMonthlyExpense) / averageMonthlyExpense) * 100;
                advice.add(Map.of(
                    "type", "WARNING",
                    "category", "SPENDING_TREND",
                    "message", String.format(
                        FinancialAdviceMessage.HIGH_SPENDING_WARNING.getMessage(language),
                        percentage
                    ),
                    "data", Map.of(
                        "currentSpending", currentMonthTotal,
                        "averageSpending", averageMonthlyExpense,
                        "percentage", percentage
                    )
                ));
            }
        }

        // Analýza kategorií
        if (!categoryTotals.isEmpty()) {
            Map.Entry<String, Double> highestCategory = categoryTotals.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get();

            double totalSpent = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            double categoryPercentage = (highestCategory.getValue() / totalSpent) * 100;

            if (categoryPercentage > 40) {
                advice.add(Map.of(
                    "type", "SUGGESTION",
                    "category", "CATEGORY_DISTRIBUTION",
                    "message", String.format(
                        FinancialAdviceMessage.HIGH_CATEGORY_SPENDING.getMessage(language),
                        highestCategory.getKey(), categoryPercentage
                    ),
                    "data", Map.of(
                        "categoryName", highestCategory.getKey(),
                        "categoryAmount", highestCategory.getValue(),
                        "percentage", categoryPercentage
                    )
                ));
            }
        }

        // Analýza frekvence transakcí
        if (!transactions.isEmpty()) {
            Map<String, Long> dayOfWeekCounts = transactions.stream()
                    .collect(Collectors.groupingBy(
                        t -> t.getExecutionDateTime().getDayOfWeek().toString(),
                        Collectors.counting()
                    ));

            String mostFrequentDay = dayOfWeekCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();

            advice.add(Map.of(
                "type", "INFO",
                "category", "TRANSACTION_PATTERN",
                "message", String.format(
                    FinancialAdviceMessage.TRANSACTION_DAY_PATTERN.getMessage(language),
                    mostFrequentDay
                ),
                "data", Map.of(
                    "dayOfWeek", mostFrequentDay,
                    "transactionCounts", dayOfWeekCounts
                )
            ));
        }

        // Doporučení pro úspory
        if (averageMonthlyExpense > 0) {
            double recommendedSavings = averageMonthlyExpense * 0.2;
            advice.add(Map.of(
                "type", "SUGGESTION",
                "category", "SAVINGS",
                "message", String.format(
                    FinancialAdviceMessage.MONTHLY_SAVINGS_RECOMMENDATION.getMessage(language),
                    averageMonthlyExpense, recommendedSavings
                ),
                "data", Map.of(
                    "averageMonthlyExpense", averageMonthlyExpense,
                    "recommendedSavings", recommendedSavings
                )
            ));
        }

        return advice;
    }

    /**
     * # Spuštění komplexní analýzy
     * @param account Účet pro analýzu
     * @param language Jazyk pro doporučení (en/cz)
     * @return Komplexní analýza účtu
     */
    public Map<String, Object> analyzeAccountTransactions(Account account, String language) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(account);
        
        Map<String, Object> result = new HashMap<>();
        
        // Anomální transakce
        double mean = 0.0;
        double threshold;
        if (!transactions.isEmpty()) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            transactions.stream()
                    .mapToDouble(Transaction::getAmount)
                    .forEach(stats::addValue);

            mean = stats.getMean();
            double stdDev = stats.getStandardDeviation();
            threshold = mean + (2 * stdDev);

            List<Transaction> anomalousTransactions = transactions.stream()
                    .filter(t -> t.getAmount() > threshold)
                    .collect(Collectors.toList());
            
            result.put("anomalousTransactions", anomalousTransactions);
            result.put("transactionThreshold", threshold);
            result.put("averageAmount", mean);
        } else {
            threshold = 0.0;
        }

        // Analýza kategorií
        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
        result.put("categoryAnalysis", categoryTotals);

        // Predikce výdajů
        Map<String, Double> monthlyTotals = new HashMap<>();
        double averageMonthlyExpense = 0.0;
        if (!transactions.isEmpty()) {
            monthlyTotals = transactions.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getExecutionDateTime().getMonth().toString(),
                            Collectors.summingDouble(Transaction::getAmount)
                    ));

            averageMonthlyExpense = monthlyTotals.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            
            result.put("predictedNextMonthExpense", averageMonthlyExpense);
            result.put("monthlyTotals", monthlyTotals);
        }

        // Generování doporučení
        List<Map<String, Object>> advice = generateFinancialAdvice(
            transactions,
            categoryTotals,
            monthlyTotals,
            averageMonthlyExpense,
            language
        );
        result.put("financialAdvice", advice);

        // Základní statistiky
        result.put("totalTransactions", transactions.size());
        result.put("accountId", account.getId());
        result.put("accountName", account.getName());
        result.put("analysisTimestamp", LocalDateTime.now());

        // Odeslání notifikace přes WebSocket
        messagingTemplate.convertAndSend(
            "/topic/analytics/" + account.getId(),
            result
        );

        return result;
    }

    /**
     * # Kontrola limitů rozpočtu
     * @param newTransaction Nová transakce
     */
    public void checkBudgetLimits(Transaction newTransaction) {
        Account account = newTransaction.getAccount();
        List<Transaction> monthTransactions = transactionRepository.findAllByAccountAndDaysBetween(
                account,
                LocalDateTime.now().withDayOfMonth(1),
                LocalDateTime.now()
        );

        double monthlyTotal = monthTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyLimit = 50000.0;

        if (monthlyTotal > monthlyLimit) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "BUDGET_LIMIT_EXCEEDED");
            notification.put("currentTotal", monthlyTotal);
            notification.put("limit", monthlyLimit);
            notification.put("accountId", account.getId());
            
            messagingTemplate.convertAndSend(
                "/topic/analytics/" + account.getId(),
                notification
            );

            // Send push notification to account owner
            account.getUserRights().stream()
                .filter(right -> right.isOwner())
                .map(right -> right.getUser())
                .findFirst()
                .ifPresent(owner -> {
                    Subscription subscription = pushNotificationService.getUserSubscription(owner.getId().toString());
                    if (subscription != null) {
                        pushNotificationService.sendNotification(
                            subscription,
                            String.format("Budget Alert: Account %s spending (%.2f) has exceeded the limit (%.2f)", 
                                account.getName(), monthlyTotal, monthlyLimit)
                        );
                    }
                });
        }
    }

    /**
     * # Získání souhrnné analýzy
     * @param account Účet pro analýzu
     * @return Souhrnná analýza
     */
    public Map<String, Object> getAnalyticsSummary(Account account) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(account);
        
        if (transactions.isEmpty()) {
            return Map.of(
                "totalTransactions", 0,
                "totalAmount", 0.0,
                "averageAmount", 0.0,
                "recentTransactions", Collections.emptyList()
            );
        }

        double totalAmount = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        double avgAmount = totalAmount / transactions.size();

        return Map.of(
            "totalTransactions", transactions.size(),
            "totalAmount", totalAmount,
            "averageAmount", avgAmount,
            "recentTransactions", transactions.stream()
                .sorted(Comparator.comparing(Transaction::getExecutionDateTime).reversed())
                .limit(5)
                .collect(Collectors.toList())
        );
    }

    /**
     * # Analýza podle kategorií
     * @param account Účet pro analýzu
     * @return Analýza kategorií
     */
    public Map<String, Object> getCategoryAnalytics(Account account) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(account);
        
        if (transactions.isEmpty()) {
            return Map.of(
                "categoryTotals", Collections.emptyMap(),
                "topCategories", Collections.emptyMap()
            );
        }

        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        Map<String, Double> topCategories = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

        return Map.of(
            "categoryTotals", categoryTotals,
            "topCategories", topCategories
        );
    }

    /**
     * # Kontrola stavu rozpočtu
     * @param account Účet pro kontrolu
     * @return Stav rozpočtu
     */
    public Map<String, Object> getBudgetStatus(Account account) {
        List<Transaction> monthTransactions = transactionRepository.findAllByAccountAndDaysBetween(
                account,
                LocalDateTime.now().withDayOfMonth(1),
                LocalDateTime.now()
        );

        double monthlyTotal = monthTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyLimit = 50000.0;
        double remainingBudget = monthlyLimit - monthlyTotal;

        if (monthlyTotal > monthlyLimit) {
            account.getUserRights().stream()
                .filter(right -> right.isOwner())
                .map(right -> right.getUser())
                .findFirst()
                .ifPresent(owner -> {
                    Subscription subscription = pushNotificationService.getUserSubscription(owner.getId().toString());
                    if (subscription != null) {
                        pushNotificationService.sendNotification(
                            subscription,
                            String.format("Budget Alert: Account %s spending (%.2f) has exceeded the limit (%.2f)", 
                                account.getName(), monthlyTotal, monthlyLimit)
                        );
                    }
                });
        }

        return Map.of(
            "currentSpending", monthlyTotal,
            "budgetLimit", monthlyLimit,
            "remainingBudget", remainingBudget
        );
    }

    /**
     * # Získání náhodného finančního doporučení
     * @param account Účet pro analýzu
     * @param language Jazyk pro doporučení (en/cz)
     * @return Náhodné doporučení jako text
     */
    public String getRandomFinancialAdvice(Account account, String language) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(account);
        
        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        Map<String, Double> monthlyTotals = new HashMap<>();
        double averageMonthlyExpense = 0.0;
        if (!transactions.isEmpty()) {
            monthlyTotals = transactions.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getExecutionDateTime().getMonth().toString(),
                            Collectors.summingDouble(Transaction::getAmount)
                    ));

            averageMonthlyExpense = monthlyTotals.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        }

        List<String> possibleAdvice = new ArrayList<>();

        if (!monthlyTotals.isEmpty()) {
            String currentMonth = LocalDateTime.now().getMonth().toString();
            double currentMonthTotal = monthlyTotals.getOrDefault(currentMonth, 0.0);
            if (currentMonthTotal > averageMonthlyExpense * 1.2) {
                double percentage = ((currentMonthTotal - averageMonthlyExpense) / averageMonthlyExpense) * 100;
                possibleAdvice.add(String.format(
                    FinancialAdviceMessage.HIGH_SPENDING_WARNING.getMessage(language),
                    percentage
                ));
            }
        }

        if (!categoryTotals.isEmpty()) {
            Map.Entry<String, Double> highestCategory = categoryTotals.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get();
            double totalSpent = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            double categoryPercentage = (highestCategory.getValue() / totalSpent) * 100;
            if (categoryPercentage > 40) {
                possibleAdvice.add(String.format(
                    FinancialAdviceMessage.HIGH_CATEGORY_SPENDING.getMessage(language),
                    highestCategory.getKey(), categoryPercentage
                ));
            }
        }

        if (!transactions.isEmpty()) {
            Map<String, Long> dayOfWeekCounts = transactions.stream()
                    .collect(Collectors.groupingBy(
                        t -> t.getExecutionDateTime().getDayOfWeek().toString(),
                        Collectors.counting()
                    ));
            String mostFrequentDay = dayOfWeekCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();
            possibleAdvice.add(String.format(
                FinancialAdviceMessage.TRANSACTION_DAY_PATTERN.getMessage(language),
                mostFrequentDay
            ));
        }

        if (averageMonthlyExpense > 0) {
            double recommendedSavings = averageMonthlyExpense * 0.2;
            possibleAdvice.add(String.format(
                FinancialAdviceMessage.MONTHLY_SAVINGS_RECOMMENDATION.getMessage(language),
                averageMonthlyExpense, recommendedSavings
            ));
        }

        possibleAdvice.add(FinancialAdviceMessage.BUDGET_PLANNING_ADVICE.getMessage(language));
        
        if (possibleAdvice.isEmpty()) {
            possibleAdvice.add(FinancialAdviceMessage.BUDGET_PLANNING_ADVICE.getMessage(language));
        }

        return possibleAdvice.get(new Random().nextInt(possibleAdvice.size()));
    }
} 