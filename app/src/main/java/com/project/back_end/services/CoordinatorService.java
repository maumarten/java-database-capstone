package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.*;
import com.project.back_end.repo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CoordinatorService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public CoordinatorService(TokenService tokenService,
                              AdminRepository adminRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              DoctorService doctorService,
                              PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> res = new HashMap<>();
        boolean ok = tokenService.validateToken(token, user);
        if (!ok) {
            res.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(res);
        }
        return ResponseEntity.ok(res); // empty map = valid (per your spec)
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> res = new HashMap<>();
        Admin db = adminRepository.findByUsername(receivedAdmin.getUsername());
        if (db == null || !Objects.equals(db.getPassword(), receivedAdmin.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(res);
        }
        String token = tokenService.generateToken(db.getUsername());
        res.put("token", token);
        return ResponseEntity.ok(res);
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = name != null && !name.equalsIgnoreCase("null") && !name.isBlank();
        boolean hasSpec = specialty != null && !specialty.equalsIgnoreCase("null") && !specialty.isBlank();
        boolean hasTime = time != null && !time.equalsIgnoreCase("null") && !time.isBlank();

        if (hasName && hasSpec && hasTime) return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        if (hasName && hasTime) return doctorService.filterDoctorByNameAndTime(name, time);
        if (hasName && hasSpec) return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        if (hasSpec && hasTime) return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        if (hasSpec) return doctorService.filterDoctorBySpecility(specialty);
        if (hasTime) return doctorService.filterDoctorsByTime(time);

        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorService.getDoctors());
        return res;
    }

    public int validateAppointment(Appointment appointment) {
        var docOpt = doctorRepository.findById(appointment.getDoctor().getId());
        if (docOpt.isEmpty()) return -1;
        LocalDate date = appointment.getAppointmentTime().toLocalDate();
        List<String> available = doctorService.getDoctorAvailability(docOpt.get().getId(), date);
        String slot = appointment.getAppointmentTime().toLocalTime().toString();
        return available.contains(slot) ? 1 : 0;
    }

    public boolean validatePatient(Patient patient) {
        var existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> res = new HashMap<>();
        String email = login.getIdentifier();
        var p = patientRepository.findByEmail(email);
        if (p == null || !Objects.equals(p.getPassword(), login.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(res);
        }
        String token = tokenService.generateToken(email);
        res.put("token", token);
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        String email = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        boolean hasCond = condition != null && !condition.equalsIgnoreCase("null") && !condition.isBlank();
        boolean hasName = name != null && !name.equalsIgnoreCase("null") && !name.isBlank();

        if (hasCond && hasName) return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
        if (hasCond) return patientService.filterByCondition(condition, patient.getId());
        if (hasName) return patientService.filterByDoctor(name, patient.getId());

        return patientService.getPatientAppointment(patient.getId(), token);
    }
}
