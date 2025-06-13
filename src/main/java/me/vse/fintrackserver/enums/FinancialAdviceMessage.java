package me.vse.fintrackserver.enums;

import lombok.Getter;

@Getter
public enum FinancialAdviceMessage {
    // Spending trend messages
    HIGH_SPENDING_WARNING(
        "Your current spending is %.1f%% higher than average. Consider limiting non-essential expenses.",
        "Vaše současné výdaje jsou o %.1f%% vyšší než průměr. Zvažte omezení zbytných výdajů."
    ),

    // Category distribution messages
    HIGH_CATEGORY_SPENDING(
        "Category '%s' accounts for %.1f%% of your expenses. Consider better distribution of expenses across categories.",
        "Kategorie '%s' tvoří %.1f%% vašich výdajů. Zvažte lepší rozložení výdajů mezi kategorie."
    ),

    // Transaction pattern messages
    TRANSACTION_DAY_PATTERN(
        "You make most transactions on %s. Plan your purchases in advance for better expense control.",
        "Nejvíce transakcí provádíte v %s. Plánujte své nákupy dopředu pro lepší kontrolu výdajů."
    ),

    // Savings recommendations
    MONTHLY_SAVINGS_RECOMMENDATION(
        "Based on your average monthly expenses (%.2f), we recommend saving at least %.2f monthly.",
        "Na základě vašich průměrných měsíčních výdajů (%.2f) doporučujeme odkládat alespoň %.2f měsíčně na úspory."
    ),

    // Low balance warning
    LOW_BALANCE_WARNING(
        "Your current balance is below 20% of your average monthly expenses. Consider adding funds.",
        "Váš současný zůstatek je pod 20% průměrných měsíčních výdajů. Zvažte doplnění prostředků."
    ),

    // Frequent small transactions
    FREQUENT_SMALL_TRANSACTIONS(
        "You have many small transactions. Consider consolidating purchases to reduce transaction fees.",
        "Máte mnoho malých transakcí. Zvažte sloučení nákupů pro snížení transakčních poplatků."
    ),

    // Regular income suggestion
    REGULAR_INCOME_SUGGESTION(
        "No regular income detected. Setting up regular income sources can help with financial planning.",
        "Nebyl zjištěn pravidelný příjem. Nastavení pravidelných zdrojů příjmů může pomoci s finančním plánováním."
    ),

    // Budget planning
    BUDGET_PLANNING_ADVICE(
        "Consider setting up a monthly budget for better expense tracking and financial goals.",
        "Zvažte nastavení měsíčního rozpočtu pro lepší sledování výdajů a finančních cílů."
    );

    private final String englishMessage;
    private final String czechMessage;

    FinancialAdviceMessage(String englishMessage, String czechMessage) {
        this.englishMessage = englishMessage;
        this.czechMessage = czechMessage;
    }

    public String getMessage(String language) {
        return "en".equalsIgnoreCase(language) ? englishMessage : czechMessage;
    }
} 