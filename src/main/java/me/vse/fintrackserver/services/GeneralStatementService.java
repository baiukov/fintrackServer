package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.TransactionTypes;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.Transaction;
import me.vse.fintrackserver.model.generalstatement.FinancialElement;
import me.vse.fintrackserver.model.generalstatement.FinancialElementRow;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GeneralStatementService {

    @Autowired
    private AssetService assetService;

    @Autowired
    private EntityManager entityManager;

    private final Map<String, Map<String, String>> language = new HashMap<>();

    public GeneralStatementService() {
        initLanguage();
    }

    private void initLanguage() {
        // EN
        Map<String, String> english = new HashMap<>();
        english.put("L_GENERAL_STATEMENT_FOR", "General statement for ");
        english.put("L_GENERAL_STATEMENT", "General statement");
        english.put("L_ASSETS", "Assets");
        english.put("L_LIABILITIES", "Liabilities");
        english.put("L_REVENUES", "Revenues");
        english.put("L_COSTS", "Costs");
        english.put("L_DEBIT", "Debit");
        english.put("L_CREDIT", "Credit");
        english.put("L_SHARE_CAPITAL", "Share capital");
        english.put("L_LOAN", "Loan");

        language.put("en", english);

        // CZ
        Map<String, String> czech = new HashMap<>();
        czech.put("L_GENERAL_STATEMENT_FOR", "Rozvaha k ");
        czech.put("L_GENERAL_STATEMENT", "Rozvaha");
        czech.put("L_ASSETS", "Aktiva");
        czech.put("L_LIABILITIES", "Pasiva");
        czech.put("L_REVENUES", "Výnosy");
        czech.put("L_COSTS", "Náklady");
        czech.put("L_DEBIT", "Má dáti");
        czech.put("L_CREDIT", "Dal");
        czech.put("L_SHARE_CAPITAL", "Základní kapitál");
        czech.put("L_LOAN", "Dlouhodobé závazky");

        language.put("cz", czech);
    }

    public byte[] generateReport(String lang, String accountId) throws IOException {

        Account account = entityManager.find(Account.class, accountId);

        if (account == null) return null;

        String templatePath = "templates/general_statement_template.xlsx";

        InputStream inputStream = new ClassPathResource(templatePath).getInputStream();
        Context context = new Context();

        // Step 1: Set date
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy"));
        context.putVar("date", date);

        // Step 2: localize
        localize(context, lang);

        // Step 3: set assets
        setAssets(context, accountId);

        // Step 4: set liabilities
        setLiabilities(context, account);

        // Step 5: set revenues
        setRevenues(context, account);

        // Step 6: set costs
        setCosts(context, account);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);

        return outputStream.toByteArray();
    }

    private void localize(Context context, String lang) {
        Map<String, String> dictionary = language.get(lang);
        if (dictionary == null) return;
        dictionary.forEach(context::putVar);
    }

    private void setCosts(Context context, Account account) {
        // Costs are expenses related to a specific asset
        List<Transaction> costs = account.getTransactions()
                .stream()
                .filter(transaction -> transaction.getForAsset() != null &&
                        TransactionTypes.EXPENSE.equals(transaction.getType()))
                .toList();

        Map<String, FinancialElement> elements = new HashMap<>();
        for (Transaction cost : costs) {
            String asset = cost.getForAsset().getName();
            elements.computeIfAbsent(asset, k -> FinancialElement.builder()
                            .name(asset)
                            .transactions(new ArrayList<>())
                            .build())
                    .getTransactions()
                    .add(new FinancialElementRow("" + cost.getAmount(), null));
        }

        for (FinancialElement element : elements.values()) {
            double total = element.getTransactions()
                    .stream()
                    .map(FinancialElementRow::getDebit)
                    .mapToDouble(Double::parseDouble)
                    .sum();
            element.setTotalDebit(total > 0 ? "" + total : null);
            element.setTotalCredit(total < 0 ? "" + Math.abs(total) : null);
        }

        context.putVar("costs", elements.values());

    }

    private void setRevenues(Context context, Account account) {
        // Revenues are income transactions without an asset
        List<Transaction> revenues = account.getTransactions()
                .stream()
                .filter(transaction -> transaction.getForAsset() == null &&
                        TransactionTypes.INCOME.equals(transaction.getType()))
                .toList();

        Map<String, FinancialElement> elements = new HashMap<>();
        for (Transaction revenue : revenues) {
            String category = revenue.getCategory() != null ? revenue.getCategory().getName() : "Other";
            elements.computeIfAbsent(category, k -> FinancialElement.builder()
                    .name(category)
                    .transactions(new ArrayList<>())
                    .build())
                    .getTransactions()
                    .add(new FinancialElementRow(null, "" + revenue.getAmount()));
        }

        for (FinancialElement element : elements.values()) {
            double total = element.getTransactions()
                    .stream()
                    .map(FinancialElementRow::getCredit)
                    .mapToDouble(Double::parseDouble)
                    .sum();
            element.setTotalCredit(total > 0 ? "" + total : null);
            element.setTotalDebit(total < 0 ? "" + Math.abs(total) : null);
        }

        context.putVar("revenues", elements.values());

    }

    private void setLiabilities(Context context, Account account) {
        if (account == null) return;

        Double shareCapital = account.getInitialAmount();
        context.putVar("shareCapital.totalCredit", shareCapital);

        Double loan = account.getGoalAmount(); // change to loan
        context.putVar("loanCredit", loan);
    }

    private void setAssets(Context context, String accountId) {
        List<Asset> assets = assetService.getAllByAccount(accountId);

        AtomicReference<Double> rawTotalDebit = new AtomicReference<>(0.0);
        List<FinancialElement> assetElements = new ArrayList<>();
        for (Asset asset : assets) {
            List<FinancialElementRow> list = new ArrayList<>();

            for (Transaction transaction : asset.getTransactions()) {
                FinancialElementRow financialElementRow;
                Double amount = transaction.getAmount();

                if (TransactionTypes.INCOME.equals(transaction.getType())) {
                    financialElementRow = new FinancialElementRow("" + amount, null);
                    rawTotalDebit.updateAndGet(v -> v + amount);
                } else {
                    financialElementRow = new FinancialElementRow(null, "" + amount);
                    rawTotalDebit.updateAndGet(v -> v - amount);
                }

                list.add(financialElementRow);
            }

            Double total = rawTotalDebit.get();
            FinancialElement apply = FinancialElement.builder()
                    .name(asset.getName())
                    .transactions(list)
                    .totalDebit(total > 0 ? "" + total : null)
                    .totalCredit(total < 0 ? "" + Math.abs(total) : null)
                    .build();
            assetElements.add(apply);
        }


        context.putVar("assets", assetElements);
    }

}
