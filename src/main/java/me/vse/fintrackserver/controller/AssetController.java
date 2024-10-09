package me.vse.fintrackserver.controller;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.services.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping("/add")
    public ResponseEntity<?> add(AssetDto assetDto) {
        try {
            return ResponseEntity.ok(assetService.add(assetDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(AssetDto assetDto) {

    }

}
