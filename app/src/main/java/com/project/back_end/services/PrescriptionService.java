package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Profile("!test")
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.preservationCheck();
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> res = new HashMap<>();
        try {
            prescriptionRepository.save(prescription);
            res.put("message", "Prescription saved");
            return ResponseEntity.status(201).body(res);
        } catch (Exception e) {
            res.put("message", "Internal server error");
            return ResponseEntity.status(500).body(res);
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> res = new HashMap<>();
        try {
            var list = prescriptionRepository.findByAppointmentId(appointmentId);
            res.put("prescriptions", list);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("message", "Internal server error");
            return ResponseEntity.status(500).body(res);
        }
    }

    // Remove this if not needed; only here to avoid accidental constructor shadowing
    private void preservationCheck() {}
}
