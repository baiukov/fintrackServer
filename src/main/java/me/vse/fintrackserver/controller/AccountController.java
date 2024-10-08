package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.dto.requests.AccountAddRequestDto;
import me.vse.fintrackserver.model.dto.requests.UserId;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AccountAddRequestDto request) {
        try {
            return ResponseEntity.ok(accountService.add(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<?> retrieveAll(@RequestBody UserId userId) {
        return ResponseEntity.ok(accountService.retrieveAll(userId.getUserId()));
    }


}
