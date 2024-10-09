package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.rest.requests.AccountAddRequest;
import me.vse.fintrackserver.model.dto.UserIdDto;
import me.vse.fintrackserver.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

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
