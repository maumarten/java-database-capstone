
package com.project.back_end.controllers;

import com.project.back_end.models.Admin;
import com.project.back_end.services.CoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

    private final CoordinatorService coordinator;

    public AdminController(CoordinatorService coordinator) {
        this.coordinator = coordinator;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return coordinator.validateAdmin(admin);
    }
}
