package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.rest.requests.GroupRemoveUserRequest;
import me.vse.fintrackserver.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(groupService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody GroupDto groupDto) {
        try {
            return ResponseEntity.ok(groupService.add(groupDto));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody GroupRemoveUserRequest request) {
        try {
            groupService.addUser(request.getGroupId(), request.getUserId());
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @DeleteMapping("/removeUser")
    public ResponseEntity<?> removeUser(@RequestParam String groupId, String userId ) {
        try {
            groupService.removeUser(groupId, userId);
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }
}
