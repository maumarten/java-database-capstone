package com.project.back_end.controllers;


import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CoordinatorService coordinator;

    public AppointmentController(AppointmentService appointmentService, CoordinatorService coordinator) {
        this.appointmentService = appointmentService;
        this.coordinator = coordinator;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String date,
                                             @PathVariable String patientName,
                                             @PathVariable String token) {
        var v = coordinator.validateToken(token, "doctor");
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        Map<String, Object> res = appointmentService.getAppointment(patientName, LocalDate.parse(date), token);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@PathVariable String token,
                                             @RequestBody Appointment appointment) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;

        int valid = coordinator.validateAppointment(appointment);
        if (valid == -1) return ResponseEntity.badRequest().body(Map.of("message", "Doctor does not exist"));
        if (valid == 0) return ResponseEntity.badRequest().body(Map.of("message", "Time not available"));

        int ok = appointmentService.bookAppointment(appointment);
        if (ok == 1) return ResponseEntity.status(201).body(Map.of("message", "Appointment booked"));
        return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@PathVariable String token,
                                               @RequestBody Appointment appointment) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable long id,
                                               @PathVariable String token) {
        var v = coordinator.validateToken(token, "patient");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return appointmentService.cancelAppointment(id, token);
    }
}
