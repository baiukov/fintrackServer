package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Parameter;
import me.vse.fintrackserver.services.TinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/tink")
public class TinkController {

    @Autowired
    private TinkService tinkService;

    @GetMapping("/generate-link")
    public ResponseEntity<String> getAuthorizationUrl(
            @Parameter(description = "The ID of the user", required = true)
            @RequestParam String userId
    ) {
        String authUrl = tinkService.generateAuthorizationUrl(userId);
        if (authUrl == null || authUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
        return ResponseEntity.ok(authUrl);
    }

    @PostMapping("/callback")
    public String exchangeCode(@RequestParam String userId,
                               @RequestParam String account_verification_report_id) throws IOException {
        System.out.println(userId + " " + account_verification_report_id);
        return null;
    }
}
