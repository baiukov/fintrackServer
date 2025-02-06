package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; // Import Tag
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.rest.requests.AssetAddRequest;
import me.vse.fintrackserver.services.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/v1/asset")
@Tag(name = "Asset Controller", description = "Operations related to assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("/getAllByAccount")
    @Operation(summary = "Get All Assets For Account", description = "Retrieve all assets for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assets"),
            @ApiResponse(responseCode = "409", description = "Conflict: account ID conflict")
    })
    public ResponseEntity<?> getAllByAccount(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String accountId
    ) {
        try {
            return ResponseEntity.ok(assetService.getAllByAccount(accountId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get All Assets", description = "Retrieve all assets for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assets"),
            @ApiResponse(responseCode = "409", description = "Conflict: account ID conflict")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "The ID of the account", required = true) @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(assetService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "Add Asset", description = "Create a new asset with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset successfully created"),
            @ApiResponse(responseCode = "409", description = "Conflict: asset could not be created"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> add(
            @Parameter(description = "Details of the asset to be created", required = true)
            @RequestBody AssetAddRequest assetAddRequest
    ) {
        try {
            return ResponseEntity.ok(assetService.add(assetAddRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    @Operation(summary = "Update Asset", description = "Update an existing asset with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset successfully updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: authentication failed"),
            @ApiResponse(responseCode = "409", description = "Conflict: asset could not be updated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Updated details of the asset", required = true)
            @RequestBody AssetDto assetDto
    ) throws AuthenticationException {
        try {
            return ResponseEntity.ok(assetService.update(assetDto));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Asset", description = "Delete an existing asset by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset successfully deleted"),
            @ApiResponse(responseCode = "409", description = "Conflict: asset could not be deleted")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "The ID of the asset to delete", required = true) @RequestParam String id,
            @Parameter(description = "The ID of the asset owner", required = true) @RequestParam String userId
    ) throws AuthenticationException {
        try {
            assetService.delete(id, userId);
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }
}
