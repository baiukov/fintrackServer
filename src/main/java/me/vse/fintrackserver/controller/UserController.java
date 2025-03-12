package me.vse.fintrackserver.controller;

import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.rest.requests.*;
import me.vse.fintrackserver.rest.responses.UserAuthResponse;
import me.vse.fintrackserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Controller", description = "Operations related to user management")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/allByName")
    @Operation(summary = "Get All Users by part of the user name", description = "Retrieve all users with limit by part of the user name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "400", description = "Invalid page size or page number")
    })
    private ResponseEntity<?> getAllByName(
            @Parameter(description = "Part of the user name", required = true) @RequestParam("name") String name,
            @Parameter(description = "Number of users", required = false, example = "10") @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(userService.getByName(name, limit));
    }

    @GetMapping("/all")
    @Operation(summary = "Get All Users", description = "Retrieve all users with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "400", description = "Invalid page size or page number")
    })
    private ResponseEntity<?> getAll(
            @Parameter(description = "Number of users per page", required = false, example = "100") @RequestParam(required = false, defaultValue = "100") int pageSize,
            @Parameter(description = "Page number for pagination", required = false, example = "0") @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(userService.getAll(pageSize, pageNumber));
    }

    @PostMapping("auth/register")
    @Operation(summary = "Register User", description = "Register a new user with provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "409", description = "Conflict: user could not be registered due to an error")
    })
    private ResponseEntity<?> register(
            @Parameter(description = "User registration details", required = true) @RequestBody UserAuthRequest request) {
        try {

            return ResponseEntity.ok(userService.registerUser(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("auth/login")
    @Operation(summary = "User Login", description = "Authenticate user with provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged in"),
            @ApiResponse(responseCode = "409", description = "Conflict: login failed due to invalid credentials")
    })
    private ResponseEntity<?> login(
            @Parameter(description = "User login credentials", required = true) @RequestBody UserAuthRequest request) {
        try {
            return ResponseEntity.ok(userService.login(
                    request.getEmail(),
                    request.getUserName(),
                    request.getPassword()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("auth/refresh")
    @Operation(summary = "Refresh Access Token", description = "Generates a new access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New access token generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or expired refresh token"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Missing refresh token in request body")
    })
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "Refresh token payload", required = true)
            @RequestBody Map<String, String> request
    ) {
        return userService.refreshToken(request.get("refreshToken"));
    }

    @GetMapping("/info")
    @Operation(summary = "Get User Info", description = "Retrieve user data using the access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or expired token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Token is missing or incorrect")
    })
    public ResponseEntity<?> getUserInfo(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authHeader
    ) {
        return userService.getUserInfo(authHeader);
    }

    @PostMapping("/setPincode")
    @Operation(summary = "Set User Pincode", description = "Set a pincode for user authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pincode successfully set"),
            @ApiResponse(responseCode = "409", description = "Conflict: pincode could not be set")
    })
    private ResponseEntity<?> setPincode(
            @Parameter(description = "User ID and pincode to set", required = true) @RequestBody UserPincodeRequest request) {
        try {
            userService.setPincode(
                    request.getId(),
                    request.getPincode()
            );

            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/verifyPincode")
    @Operation(summary = "Verify User Pincode", description = "Verify the pincode for user authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pincode verification successful"),
            @ApiResponse(responseCode = "409", description = "Conflict: pincode verification failed")
    })
    private ResponseEntity<?> verifyPincode(
            @Parameter(description = "User ID and pincode to verify", required = true) @RequestBody UserPincodeRequest request) {
        try {
            return ResponseEntity.ok(userService.verifyPinCode(
                request.getId(),
                request.getPincode()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/sendCode")
    @Operation(summary = "Send User Verification Code", description = "Verification code is sent to email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code is sent successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict: code sending is failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> sendCode(
            @Parameter(description = "User ID to verify and their language", required = true) @RequestBody SendCodeRequest request
    ) {
        try {
            userService.sendCode(request.getLogin(), request.getLang());
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/verifyRecoveryCode")
    @Operation(summary = "Verify User Verification Code", description = "Check if user insert the correct code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code has been verified successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict: code is incorrect or user doesnt asked for it"),
    })
    public ResponseEntity<?> verifyRecoveryCode(
            @Parameter(description = "User ID to verify and their language", required = true)
            @RequestBody VerifyCodeRequesst request
    ) {
        try {
            return ResponseEntity.ok(userService.verifyCode(request.getLogin(), request.getCode()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    @Operation(summary = "Set New User Password", description = "Set a new password for user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "409", description = "Conflict: password could not be updated"),
    })
    public ResponseEntity<?> updatePassword(
            @Parameter(description = "User id and new password", required = true)
            @RequestBody UpdatePasswordRequest request
    ) {
        try {
            userService.updatePassword(request.getLogin(), request.getPassword());
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }
}
