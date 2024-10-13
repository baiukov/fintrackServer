package me.vse.fintrackserver.controller;

import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody GroupDto groupDto) {
        try {
            return ResponseEntity.ok(groupService.add(groupDto));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }
}
