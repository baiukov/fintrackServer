package me.vse.fintrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.rest.requests.GroupRemoveUserRequest;
import me.vse.fintrackserver.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/group")
@Tag(name = "Group Controller", description = "Operations related to user groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/all")
    @Operation(summary = "Get All Groups", description = "Retrieve all groups for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved groups"),
            @ApiResponse(responseCode = "409", description = "Conflict: user ID conflict")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "The ID of the user", required = true) @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(groupService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "Add Group", description = "Create a new group with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group successfully created"),
            @ApiResponse(responseCode = "409", description = "Conflict: group could not be created")
    })
    public ResponseEntity<?> add(
            @Parameter(description = "Details of the group to be created", required = true)
            @RequestBody GroupDto groupDto
    ) {
        try {
            return ResponseEntity.ok(groupService.add(groupDto));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/addUser")
    @Operation(summary = "Add User to Group", description = "Add a user to a specified group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully added to group"),
            @ApiResponse(responseCode = "409", description = "Conflict: user could not be added")
    })
    public ResponseEntity<?> addUser(
            @Parameter(description = "Request containing group ID and user ID", required = true)
            @RequestBody GroupRemoveUserRequest request
    ) {
        try {
            groupService.addUser(request.getGroupId(), request.getUserId());
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @DeleteMapping("/removeUser")
    @Operation(summary = "Remove User from Group", description = "Remove a user from a specified group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully removed from group"),
            @ApiResponse(responseCode = "409", description = "Conflict: user could not be removed")
    })
    public ResponseEntity<?> removeUser(
            @Parameter(description = "The ID of the group", required = true) @RequestParam String groupId,
            @Parameter(description = "The ID of the user to remove", required = true) @RequestParam String userId
    ) {
        try {
            groupService.removeUser(groupId, userId);
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Update Group", description = "Update an existing group with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group successfully updated"),
            @ApiResponse(responseCode = "409", description = "Conflict has occurred")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Updated details of the group", required = true)
            @RequestBody GroupDto request
    ) {
        return ResponseEntity.ok(groupService.update(request));
    }
}
