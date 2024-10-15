package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.rest.requests.TransactionRequest;
import me.vse.fintrackserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/getAllByAccount")
    public ResponseEntity<?> getAllByAccount(@RequestParam String accountId,
                                             @RequestParam(required = false) LocalDateTime fromDate,
                                             @RequestParam(required = false) LocalDateTime endDate,
                                             @RequestParam(required = false, defaultValue = "0") int pageNumber
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByAccount(accountId, fromDate, endDate, pageNumber));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/getAllIncomesByCategories")
    public ResponseEntity<?> getAllIncomesByCategories(@RequestParam String accountId,
                                             @RequestParam(required = false) LocalDateTime fromDate,
                                             @RequestParam(required = false) LocalDateTime endDate
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByCategories(accountId, fromDate, endDate, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/getAllExpensesByCategories")
    public ResponseEntity<?> getAllExpensesByCategories(@RequestParam String accountId,
                                             @RequestParam(required = false) LocalDateTime fromDate,
                                             @RequestParam(required = false) LocalDateTime endDate
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByCategories(accountId, fromDate, endDate, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.ok(transactionService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(@RequestBody TransactionRequest transactionRequest) {
        try {
            return ResponseEntity.ok(transactionService.update(transactionRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> update(@RequestParam String transactionId) {
        try {
            return ResponseEntity.ok(transactionService.delete(transactionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/updateStandingOrder")
    public ResponseEntity<?> updateStandingOrder(@RequestBody TransactionRequest transactionRequest) {
        try {
            transactionService.updateStandingOrder(transactionRequest);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteStandingOrder")
    public ResponseEntity<?> deleteStandingOrder(@RequestParam String transactionId) {
        try {
            transactionService.deleteStandingOrder(transactionId);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

}
