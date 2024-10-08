package me.vse.fintrackserver.controller;


import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.requests.UserAuthRequestDto;
import me.vse.fintrackserver.model.dto.requests.UserPincodeRequestDto;
import me.vse.fintrackserver.model.dto.responses.UserAuthResponseDto;
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

    @PostMapping("/register")
    private ResponseEntity<?> register(@RequestBody UserAuthRequestDto request) {
        try {
            User user = userService.registerUser(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            );

            return ResponseEntity.ok(UserAuthResponseDto.builder()
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
    private ResponseEntity<?> login(@RequestBody UserAuthRequestDto request) {
        try {
            User user = userService.login(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            );

            return ResponseEntity.ok(UserAuthResponseDto.builder()
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
    private ResponseEntity<?> setPincode(@RequestBody UserPincodeRequestDto request) {
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
    private ResponseEntity<?> verifyPincode(@RequestBody UserPincodeRequestDto request) {
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
