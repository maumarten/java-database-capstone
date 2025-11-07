package com.project.back_end.controllers;


import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.CoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final CoordinatorService coordinator;

    public DoctorController(DoctorService doctorService, CoordinatorService coordinator) {
        this.doctorService = doctorService;
        this.coordinator = coordinator;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getAvailability(@PathVariable String user,
                                             @PathVariable Long doctorId,
                                             @PathVariable String date,
                                             @PathVariable String token) {
        var v = coordinator.validateToken(token, user);
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        List<String> slots = doctorService.getDoctorAvailability(doctorId, LocalDate.parse(date));
        return ResponseEntity.ok(Map.of("availability", slots));
    }

    @GetMapping
    public ResponseEntity<?> listDoctors() {
        return ResponseEntity.ok(Map.of("doctors", doctorService.getDoctors()));
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> addDoctor(@PathVariable String token, @RequestBody Doctor doctor) {
        var v = coordinator.validateToken(token, "admin");
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        int res = doctorService.saveDoctor(doctor);
        return switch (res) {
            case 1 -> ResponseEntity.status(201).body(Map.of("message", "Doctor added to db"));
            case -1 -> ResponseEntity.status(409).body(Map.of("message", "Doctor already exists"));
            default -> ResponseEntity.status(500).body(Map.of("message", "Some internal error occurred"));
        };
    }

    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateDoctor(@PathVariable String token, @RequestBody Doctor doctor) {
        var v = coordinator.validateToken(token, "admin");
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        int res = doctorService.updateDoctor(doctor);
        return switch (res) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor updated"));
            case -1 -> ResponseEntity.status(404).body(Map.of("message", "Doctor not found"));
            default -> ResponseEntity.status(500).body(Map.of("message", "Some internal error occurred"));
        };
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(@PathVariable long id, @PathVariable String token) {
        var v = coordinator.validateToken(token, "admin");
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        int res = doctorService.deleteDoctor(id);
        return switch (res) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
            case -1 -> ResponseEntity.status(404).body(Map.of("message", "Doctor not found with id"));
            default -> ResponseEntity.status(500).body(Map.of("message", "Some internal error occurred"));
        };
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<?> filterDoctors(@PathVariable String name,
                                           @PathVariable String time,
                                           @PathVariable("speciality") String specialty) {
        return ResponseEntity.ok(coordinator.filterDoctor(
                "null".equalsIgnoreCase(name) ? null : name,
                "null".equalsIgnoreCase(specialty) ? null : specialty,
                "null".equalsIgnoreCase(time) ? null : time
        ));
    }
}
