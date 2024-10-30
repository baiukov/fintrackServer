package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.vse.fintrackserver.services.AccountService;
import me.vse.fintrackserver.services.AdviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/advice")
@Tag(name = "Advice Controller", description = "Getting advice generated on server")
public class AdviceController {

    @Autowired
    private AdviceService adviceService;

    @GetMapping("/getAdvice")
    @Operation(summary = "Get Advice", description = "Get generated advice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved advice"),
            @ApiResponse(responseCode = "409", description = "Invalid user ID provided"),
    })
    public ResponseEntity<?> getBalance(
            @Parameter(description = "The ID of the user", required = true) @RequestParam String id
    ) {
        return ResponseEntity.ok(adviceService.getAdvice(id));
    }
}
