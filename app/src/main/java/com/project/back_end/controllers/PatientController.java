package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.CoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final CoordinatorService coordinator;

    public PatientController(PatientService patientService, CoordinatorService coordinator) {
        this.patientService = patientService;
        this.coordinator = coordinator;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Patient patient) {
        boolean ok = coordinator.validatePatient(patient);
        if (!ok) return ResponseEntity.status(409).body(Map.of("message", "Patient with email id or phone no already exist"));
        int r = patientService.createPatient(patient);
        if (r == 1) return ResponseEntity.status(201).body(Map.of("message", "Signup successful"));
        return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        return coordinator.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> appointments(@PathVariable Long id, @PathVariable String token) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filter(@PathVariable String condition,
                                    @PathVariable String name,
                                    @PathVariable String token) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return coordinator.filterPatient(condition, name, token);
    }
}
