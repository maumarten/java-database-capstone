package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        // Collect booked times (canonical format HH:mm)
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<String> bookedSlots = new HashSet<>();
        for (Appointment a : booked) {
            bookedSlots.add(a.getAppointmentTime().toLocalTime().toString()); // e.g., "09:00"
        }

        // Normalize doctor's availableTimes to HH:mm (take the start if a range like "09:00-10:00")
        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isEmpty()) return List.of();

        List<String> normalized = new ArrayList<>();
        for (String slot : docOpt.get().getAvailableTimes()) {
            if (slot == null || slot.isBlank()) continue;
            String startPart = slot.contains("-") ? slot.split("-", 2)[0] : slot;
            normalized.add(startPart.trim());
        }

        normalized.removeIf(bookedSlots::contains);
        return normalized;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) return -1;
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> res = new HashMap<>();
        String email = login.getIdentifier();
        var doc = doctorRepository.findByEmail(email);
        if (doc == null || !Objects.equals(doc.getPassword(), login.getPassword())) {
            res.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(res);
        }
        String token = tokenService.generateToken(email);
        res.put("token", token);
        return ResponseEntity.ok(res);
    }

    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorRepository.findByNameLike(name));
        return res;
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> byNameSpec = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                name == null ? "" : name, specialty == null ? "" : specialty);
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", filterDoctorByTime(byNameSpec, amOrPm));
        return res;
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> byName = doctorRepository.findByNameLike(name == null ? "" : name);
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", filterDoctorByTime(byName, amOrPm));
        return res;
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                name == null ? "" : name, specilty == null ? "" : specilty));
        return res;
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        List<Doctor> bySpec = doctorRepository.findBySpecialtyIgnoreCase(specilty == null ? "" : specilty);
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", filterDoctorByTime(bySpec, amOrPm));
        return res;
    }

    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorRepository.findBySpecialtyIgnoreCase(specilty == null ? "" : specilty));
        return res;
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> all = doctorRepository.findAll();
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", filterDoctorByTime(all, amOrPm));
        return res;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isBlank()) return doctors;
        String target = amOrPm.trim().toUpperCase(Locale.ROOT);
        // Simple heuristic: keep doctors who have any slot containing "AM"/"PM".
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : doctors) {
            if (d.getAvailableTimes() == null) continue;
            boolean match = d.getAvailableTimes().stream().anyMatch(t ->
                    ("AM".equals(target) && timeIsAm(t)) || ("PM".equals(target) && !timeIsAm(t)));
            if (match) filtered.add(d);
        }
        return filtered;
    }

    private boolean timeIsAm(String hhmm) {
        // expects "HH:mm"
        try {
            int hour = Integer.parseInt(hhmm.substring(0, 2));
            return hour < 12;
        } catch (Exception e) {
            return true;
        }
    }
}
