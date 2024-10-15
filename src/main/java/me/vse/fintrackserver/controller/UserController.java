package me.vse.fintrackserver.controller;


import com.fasterxml.jackson.core.ErrorReportConfiguration;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.rest.requests.UserAuthRequest;
import me.vse.fintrackserver.rest.requests.UserPincodeRequest;
import me.vse.fintrackserver.rest.responses.UserAuthResponse;
import me.vse.fintrackserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/getAll")
    private ResponseEntity<?> getAll(@RequestParam(required = false,defaultValue = "100") int pageSize,
                                     @RequestParam(required = false,defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(userService.getAll(pageSize, pageNumber));
    }

    @PostMapping("/register")
    private ResponseEntity<?> register(@RequestBody UserAuthRequest request) {
        try {
            User user = userService.registerUser(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            );

            return ResponseEntity.ok(UserAuthResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .isAdmin(user.isAdmin())
                    .isBlocked(user.isBlocked())
                    .build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/login")
    private ResponseEntity<?> login(@RequestBody UserAuthRequest request) {
        try {
            User user = userService.login(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            );

            return ResponseEntity.ok(UserAuthResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .isAdmin(user.isAdmin())
                    .isBlocked(user.isBlocked())
                    .build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/setPincode")
    private ResponseEntity<?> setPincode(@RequestBody UserPincodeRequest request) {
        try {
            userService.setPincode(
                    UUID.fromString(request.getId()),
                    request.getPincode()
            );

            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/verifyPincode")
    private ResponseEntity<?> verifyPincode(@RequestBody UserPincodeRequest request) {
        try {
            return ResponseEntity.ok(userService.verifyPinCode(
                    UUID.fromString(request.getId()),
                    request.getPincode()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

}
