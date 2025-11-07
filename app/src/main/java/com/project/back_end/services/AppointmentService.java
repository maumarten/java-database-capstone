package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final CoordinatorService  coordinator;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              CoordinatorService  coordinator) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.coordinator = coordinator;
    }

    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> res = new HashMap<>();
        return appointmentRepository.findById(appointment.getId())
                .map(existing -> {
                    int valid = coordinator.validateAppointment(appointment);
                    if (valid != 1) {
                        res.put("message", "Invalid appointment update");
                        return ResponseEntity.badRequest().body(res);
                    }
                    appointmentRepository.save(appointment);
                    res.put("message", "Appointment updated");
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    res.put("message", "Appointment not found");
                    return ResponseEntity.status(404).body(res);
                });
    }

    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> res = new HashMap<>();
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isEmpty()) {
            res.put("message", "Appointment not found");
            return ResponseEntity.status(404).body(res);
        }
        Appointment appt = apptOpt.get();

        // Ensure the patient who cancels is the owner (based on token email)
        String email = tokenService.extractIdentifier(token);
        Patient patient = appt.getPatient();
        if (patient == null || !email.equalsIgnoreCase(patient.getEmail())) {
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        appointmentRepository.delete(appt);
        res.put("message", "Appointment cancelled");
        return ResponseEntity.ok(res);
    }

    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        // doctorId is derived from token (if doctor). Adjust if your token payload differs.
        String identifier = tokenService.extractIdentifier(token);
        // TODO: map identifier->doctor (email)
        // Example:
        Doctor doctor = doctorRepository.findByEmail(identifier);
        Long doctorId = doctor != null ? doctor.getId() : null;

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> list;
        if (pname != null && !pname.equalsIgnoreCase("null") && !pname.isBlank()) {
            list = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, pname, start, end);
        } else {
            list = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        }

        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : list) {
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
        Map<String, Object> res = new HashMap<>();
        res.put("appointments", dtos);
        return res;
    }
}
