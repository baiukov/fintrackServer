package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; // Import Tag
import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/category")
@Tag(name = "Category Controller", description = "Operations related to categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/getAll")
    @Operation(summary = "Get All Categories", description = "Retrieve all categories for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "409", description = "Conflict: user ID conflict")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "The ID of the user", required = true) @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(categoryService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Create Category", description = "Create a new category with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category successfully created"),
            @ApiResponse(responseCode = "409", description = "Conflict: category could not be created")
    })
    public ResponseEntity<?> create(
            @Parameter(description = "Details of the category to be created", required = true)
            @RequestBody CategoryDto categoryDto
    ) {
        try {
            return ResponseEntity.ok(categoryService.add(categoryDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    @Operation(summary = "Update Category", description = "Update an existing category with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category successfully updated"),
            @ApiResponse(responseCode = "409", description = "Conflict: category could not be updated")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Updated details of the category", required = true)
            @RequestBody CategoryDto categoryDto
    ) {
        try {
            return ResponseEntity.ok(categoryService.update(categoryDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Category", description = "Delete an existing category by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category successfully deleted"),
            @ApiResponse(responseCode = "409", description = "Conflict: category could not be deleted")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "The ID of the category to delete", required = true)
            @RequestParam String categoryId,

            @Parameter(description = "The ID of owner of the category", required = true)
            @RequestParam String userId
    ) {
        try {
            categoryService.delete(categoryId, userId);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
