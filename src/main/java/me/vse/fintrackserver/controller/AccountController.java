package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import me.vse.fintrackserver.model.dto.UserIdDto;
import me.vse.fintrackserver.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/getBalance")
    public ResponseEntity<?> getBalance(@RequestParam String id,
                                        @RequestParam(required = false) LocalDateTime fromDate,
                                        @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getBalance(id, fromDate, endDate));
    }

    @GetMapping("/getIncome")
    public ResponseEntity<?> getIncome(@RequestParam String id,
                                        @RequestParam(required = false) LocalDateTime fromDate,
                                        @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getIncome(id, fromDate, endDate));
    }
    @GetMapping("/getExpense")
    public ResponseEntity<?> getExpense(@RequestParam String id,
                                        @RequestParam(required = false) LocalDateTime fromDate,
                                        @RequestParam(required = false) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(accountService.getExpense(id, fromDate, endDate));
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AccountAddRequest request) {
        try {
            return ResponseEntity.ok(accountService.add(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<?> retrieveAll(@RequestBody UserIdDto userId) {
        return ResponseEntity.ok(accountService.retrieveAll(userId.getUserId()));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(@RequestBody AccountDto request) {
        return ResponseEntity.ok(accountService.update(request));
    }


}
