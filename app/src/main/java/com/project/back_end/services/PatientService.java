package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> res = new HashMap<>();
        String email = tokenService.extractIdentifier(token);
        Patient tokenPatient = patientRepository.findByEmail(email);
        if (tokenPatient == null || !Objects.equals(tokenPatient.getId(), id)) {
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }
        List<Appointment> appts = appointmentRepository.findWithRelationsByPatientId(id);
        res.put("appointments", toDTOs(appts));
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> res = new HashMap<>();
        int status;
        if ("past".equalsIgnoreCase(condition)) status = 1;
        else if ("future".equalsIgnoreCase(condition) || "upcoming".equalsIgnoreCase(condition)) status = 0;
        else {
            res.put("message", "Invalid condition");
            return ResponseEntity.badRequest().body(res);
        }
        List<Appointment> appts = appointmentRepository.findWithRelationsByPatientIdAndStatusOrderByAppointmentTimeAsc(id, status);
        res.put("appointments", toDTOs(appts));
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> res = new HashMap<>();
        List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientId(name == null ? "" : name, patientId);
        res.put("appointments", toDTOs(appts));
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> res = new HashMap<>();
        int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
        List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(
                name == null ? "" : name, patientId, status);
        res.put("appointments", toDTOs(appts));
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> res = new HashMap<>();
        String email = tokenService.extractIdentifier(token);
        Patient p = patientRepository.findByEmail(email);
        if (p == null) {
            res.put("message", "Patient not found");
            return ResponseEntity.status(404).body(res);
        }
        res.put("patient", p);
        return ResponseEntity.ok(res);
    }

    private List<AppointmentDTO> toDTOs(List<Appointment> appts) {
        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appts) {
            dtos.add(new AppointmentDTO(
                    a.getId(),
                    a.getDoctor().getId(),
                    a.getDoctor().getName(),
                    a.getPatient().getId(),
                    a.getPatient().getName(),
                    a.getPatient().getEmail(),
                    a.getPatient().getPhone(),
                    a.getPatient().getAddress(),
                    a.getAppointmentTime(),
                    a.getStatus()
            ));
        }
        return dtos;
    }
}
