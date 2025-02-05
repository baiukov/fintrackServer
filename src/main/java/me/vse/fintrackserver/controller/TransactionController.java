package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transaction Controller", description = "Operations related to transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/getAllByAccount")
    @Operation(summary = "Get All Transactions by Account", description = "Retrieve all transactions for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
            @ApiResponse(responseCode = "409", description = "Conflict: account ID conflict")
    })
    public ResponseEntity<?> getAllByAccount(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String accountId,
            @Parameter(description = "Start date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "End date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Page number for pagination", required = false, example = "0") @RequestParam(required = false, defaultValue = "0") int pageNumber
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByAccount(accountId, fromDate, endDate, pageNumber));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/getAllIncomesByCategories")
    @Operation(summary = "Get All Incomes by Categories", description = "Retrieve all income transactions categorized for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved income transactions"),
            @ApiResponse(responseCode = "409", description = "Conflict: account ID conflict")
    })
    public ResponseEntity<?> getAllIncomesByCategories(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String accountId,
            @Parameter(description = "Start date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "End date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime endDate
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByCategories(accountId, fromDate, endDate, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/getAllExpensesByCategories")
    @Operation(summary = "Get All Expenses by Categories", description = "Retrieve all expense transactions categorized for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved expense transactions"),
            @ApiResponse(responseCode = "409", description = "Conflict: account ID conflict")
    })
    public ResponseEntity<?> getAllExpensesByCategories(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String accountId,
            @Parameter(description = "Start date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "End date for filtering transactions", required = false) @RequestParam(required = false) LocalDateTime endDate
    ) {
        try {
            return ResponseEntity.ok(transactionService.findAllByCategories(accountId, fromDate, endDate, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Create Transaction", description = "Create a new transaction with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction successfully created"),
            @ApiResponse(responseCode = "409", description = "Conflict: transaction could not be created")
    })
    public ResponseEntity<?> create(
            @Parameter(description = "Details of the transaction to be created", required = true) @RequestBody TransactionRequest request
    ) {
        try {
            return ResponseEntity.ok(transactionService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    @Operation(summary = "Update Transaction", description = "Update an existing transaction with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction successfully updated"),
            @ApiResponse(responseCode = "409", description = "Conflict: transaction could not be updated")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Updated details of the transaction", required = true) @RequestBody TransactionRequest transactionRequest
    ) {
        try {
            return ResponseEntity.ok(transactionService.update(transactionRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Transaction", description = "Delete an existing transaction by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction successfully deleted"),
            @ApiResponse(responseCode = "409", description = "Conflict: transaction could not be deleted")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "The ID of the transaction to delete", required = true) @RequestParam String transactionId,
            @Parameter(description = "The ID of the owner of the transaction", required = true) @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(transactionService.delete(transactionId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/updateStandingOrder")
    @Operation(summary = "Update Standing Order", description = "Update an existing standing order transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standing order successfully updated"),
            @ApiResponse(responseCode = "409", description = "Conflict: standing order could not be updated")
    })
    public ResponseEntity<?> updateStandingOrder(
            @Parameter(description = "Updated details of the standing order transaction", required = true) @RequestBody TransactionRequest transactionRequest
    ) {
        try {
            transactionService.updateStandingOrder(transactionRequest);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteStandingOrder")
    @Operation(summary = "Delete Standing Order", description = "Delete an existing standing order transaction by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standing order successfully deleted"),
            @ApiResponse(responseCode = "409", description = "Conflict: standing order could not be deleted")
    })
    public ResponseEntity<?> deleteStandingOrder(
            @Parameter(description = "The ID of the standing order transaction to delete", required = true) @RequestParam String transactionId
    ) {
        try {
            transactionService.deleteStandingOrder(transactionId);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
