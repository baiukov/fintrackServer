package me.vse.fintrackserver.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeneralStatementService {

    private final Map<String, Map<String, String>> language = new HashMap<>();

    public GeneralStatementService() {
        initLanguage();
    }

    private void initLanguage() {
        // EN
        Map<String, String> english = new HashMap<>();
        english.put("GENERAL_STATEMENT_FOR", "General statement for ");
        english.put("GENERAL_STATEMENT", "General statement");
        english.put("ASSETS", "Aassets");
        english.put("LIABILITIES", "Liabilities");
        english.put("REVENUES", "Revenues");
        english.put("COSTS", "Costs");

        language.put("en", english);

        // CZ
        Map<String, String> czech = new HashMap<>();
        czech.put("GENERAL_STATEMENT_FOR", "Rozvaha k ");
        czech.put("GENERAL_STATEMENT", "Rozvaha");
        czech.put("ASSETS", "Aktiva");
        czech.put("LIABILITIES", "Pasiva");
        czech.put("REVENUES", "Výnosy");
        czech.put("COSTS", "Náklady");

        language.put("cz", czech);
    }

    public byte[] generateReport(String lang) throws IOException {

        String templatePath = "templates/general_statement_template.xlsx";

        Workbook workbook;
        try {
            InputStream inputStream = new ClassPathResource(templatePath).getInputStream();
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO add exception
        }

        Sheet sheet = workbook.getSheetAt(0);

        // Step 1: set language
        localize(sheet, lang);

        // Step

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        return outputStream.toByteArray();
    }

    private void localize(Sheet sheet, String lang) {

        Map<String, String> dictionary = language.get(lang);
        if (dictionary == null) return;

        for (Row row : sheet) {
            for (Cell cell : row) {
                String cellValue = cell.getStringCellValue();
                if (cellValue == null) continue;
                if (!cellValue.contains("{")) continue;

                // if date
                if (cellValue.contains("DATE")) {
                    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy"));
                    cell.setCellValue(dictionary.get("GENERAL_STATEMENT_FOR") + date);
                    continue;
                }

                cellValue = cellValue.replaceAll("\\{", "")
                        .replaceAll(" ", "")
                        .replaceAll("\\}", "")
                        .replaceAll("L_", "");
                String word = dictionary.get(cellValue);
                String newValue = word == null ? cellValue : word;

                cell.setCellValue(newValue);
            }
        }

    }

    private void setCellValue(Sheet sheet, String id, String value) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                String cellValue = cell.getStringCellValue();
                if (cellValue == null) continue;
                if (!cellValue.contains("{") || !cellValue.equals(id)) continue;

                cell.setCellValue(value);
            }
        }
    }

}
