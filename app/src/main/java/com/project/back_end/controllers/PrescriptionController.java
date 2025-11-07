package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.CoordinatorService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("!test")
@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final CoordinatorService coordinator;

    public PrescriptionController(PrescriptionService prescriptionService, CoordinatorService coordinator) {
        this.prescriptionService = prescriptionService;
        this.coordinator = coordinator;
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> save(@PathVariable String token, @RequestBody Prescription prescription) {
        var v = coordinator.validateToken(token, "doctor");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getByAppointment(@PathVariable Long appointmentId,
                                              @PathVariable String token) {
        var v = coordinator.validateToken(token, "doctor");
        if (!v.getStatusCode().is2xxSuccessful()) return v;
        return prescriptionService.getPrescription(appointmentId);
    }
}
