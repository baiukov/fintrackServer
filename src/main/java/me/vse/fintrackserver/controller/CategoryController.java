package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(categoryService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CategoryDto categoryDto) {
        try {
            return ResponseEntity.ok(categoryService.add(categoryDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(@RequestBody CategoryDto categoryDto) {
        try {
            return ResponseEntity.ok(categoryService.update(categoryDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> update(@RequestParam String categoryId) {
        try {
            categoryService.delete(categoryId);
            return ResponseEntity.ok(HttpEntity.EMPTY);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
