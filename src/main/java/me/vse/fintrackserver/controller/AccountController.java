package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import me.vse.fintrackserver.model.dto.UserIdDto;
import me.vse.fintrackserver.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/account")
@Tag(name = "Account Controller", description = "Operations related to user accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/getBalance")
    @Operation(summary = "Get Account Balance", description = "Retrieve the balance of the specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved balance"),
            @ApiResponse(responseCode = "409", description = "Invalid account ID provided"),
    })
    public ResponseEntity<?> getBalance(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String id,
            @Parameter(description = "Optional start date for the balance calculation")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Optional end date for the balance calculation")
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getBalance(id, fromDate, endDate));
    }

    @GetMapping("/getNetWorth")
    @Operation(summary = "Get Net Worth", description = "Calculate the net worth of the specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved net worth"),
            @ApiResponse(responseCode = "409", description = "Invalid account ID provided")
    })
    public ResponseEntity<?> getNetWorth(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String id,
            @Parameter(description = "Optional start date for the net worth calculation")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Optional end date for the net worth calculation")
            @RequestParam(required = false) LocalDateTime endDate

    ) {
        return ResponseEntity.ok(accountService.getNetWorth(id, fromDate, endDate));
    }

    @GetMapping("/getIncome")
    @Operation(summary = "Get Income", description = "Retrieve the income details for the specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved income"),
            @ApiResponse(responseCode = "409", description = "Invalid account ID provided")
    })
    public ResponseEntity<?> getIncome(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String id,
            @Parameter(description = "Optional start date for the income calculation")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Optional end date for the income calculation")
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getIncome(id, fromDate, endDate));
    }

    @GetMapping("/getExpense")
    @Operation(summary = "Get Expense", description = "Retrieve the expense details for the specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved expenses"),
            @ApiResponse(responseCode = "409", description = "Invalid account ID provided")
    })
    public ResponseEntity<?> getExpense(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String id,
            @Parameter(description = "Optional start date for the expense calculation")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Optional end date for the expense calculation")
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getExpense(id, fromDate, endDate));
    }

    @PostMapping("/add")
    @Operation(summary = "Add Account", description = "Create a new account with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account successfully created"),
            @ApiResponse(responseCode = "409", description = "Conflict: account could not be created")
    })
    public ResponseEntity<?> add(
            @Parameter(description = "Details of the account to be created", required = true)
            @RequestBody AccountAddRequest request) {
        try {
            return ResponseEntity.ok(accountService.add(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @GetMapping("/retrieveAll")
    @Operation(summary = "Retrieve All Accounts", description = "Get all accounts for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    })
    public ResponseEntity<?> retrieveAll(
            @Parameter(description = "User ID for which to retrieve accounts", required = true)
            @RequestParam String userId) {
        return ResponseEntity.ok(accountService.retrieveAll(userId));
    }

    @GetMapping("/retrievaAllByName")
    @Operation(summary = "Retrieve All Accounts by name and user id", description = "Get all accounts for a specific user with name filter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    })
    public ResponseEntity<?> retrieveAllByName(
            @Parameter(description = "User ID for which to retrieve accounts", required = true)
            @RequestParam String userId,

            @Parameter(description = "Part of the account name, if present")
            @RequestParam(required = false, defaultValue = "") String name,

            @Parameter(description = "Limit of the found account list")
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(accountService.retrieveAllByName(userId, name, limit));
    }

    @PatchMapping("/update")
    @Operation(summary = "Update Account", description = "Update an existing account with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account successfully updated")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Updated details of the account", required = true)
            @RequestBody AccountDto request) {
        return ResponseEntity.ok(accountService.update(request));
    }
}
