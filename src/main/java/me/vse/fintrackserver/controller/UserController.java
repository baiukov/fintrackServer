package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.rest.requests.UserAuthRequest;
import me.vse.fintrackserver.rest.requests.UserPincodeRequest;
import me.vse.fintrackserver.rest.responses.UserAuthResponse;
import me.vse.fintrackserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Controller", description = "Operations related to user management")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getAll")
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

    @GetMapping("/getAll")
    private ResponseEntity<?> getAll(@RequestParam(required = false,defaultValue = "100") int pageSize,
                                     @RequestParam(required = false,defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(userService.getAll(pageSize, pageNumber));
    }

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Register a new user with provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "409", description = "Conflict: user could not be registered due to an error")
    })
    private ResponseEntity<?> register(
            @Parameter(description = "User registration details", required = true) @RequestBody UserAuthRequest request) {
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
    @Operation(summary = "User Login", description = "Authenticate user with provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged in"),
            @ApiResponse(responseCode = "409", description = "Conflict: login failed due to invalid credentials")
    })
    private ResponseEntity<?> login(
            @Parameter(description = "User login credentials", required = true) @RequestBody UserAuthRequest request) {
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
    @Operation(summary = "Set User Pincode", description = "Set a pincode for user authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pincode successfully set"),
            @ApiResponse(responseCode = "409", description = "Conflict: pincode could not be set")
    })
    private ResponseEntity<?> setPincode(
            @Parameter(description = "User ID and pincode to set", required = true) @RequestBody UserPincodeRequest request) {
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
    @Operation(summary = "Verify User Pincode", description = "Verify the pincode for user authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pincode verification successful"),
            @ApiResponse(responseCode = "409", description = "Conflict: pincode verification failed")
    })
    private ResponseEntity<?> verifyPincode(
            @Parameter(description = "User ID and pincode to verify", required = true) @RequestBody UserPincodeRequest request) {
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
