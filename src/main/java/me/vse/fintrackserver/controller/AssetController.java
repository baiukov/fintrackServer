package me.vse.fintrackserver.controller;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.services.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/v1/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AssetDto assetDto) {
        try {
            return ResponseEntity.ok(assetService.add(assetDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(@RequestBody AssetDto assetDto) throws AuthenticationException {
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

    @PatchMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam String id) throws AuthenticationException {
        try {
            assetService.delete(id);
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

}
