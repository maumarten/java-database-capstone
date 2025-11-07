package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
           SELECT a FROM Appointment a
             LEFT JOIN FETCH a.doctor d
             LEFT JOIN FETCH a.patient p
            WHERE d.id = :doctorId
              AND a.appointmentTime BETWEEN :start AND :end
           """)
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(@Param("doctorId") Long doctorId,
                                                              @Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end);

    @Query("""
           SELECT a FROM Appointment a
             LEFT JOIN FETCH a.doctor d
             LEFT JOIN FETCH a.patient p
            WHERE d.id = :doctorId
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%'))
              AND a.appointmentTime BETWEEN :start AND :end
           """)
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    @Query("""
           SELECT a FROM Appointment a
             JOIN a.doctor d
             JOIN a.patient p
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
              AND p.id = :patientId
           """)
    List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName,
                                                     @Param("patientId") Long patientId);

    @Query("""
           SELECT a FROM Appointment a
             JOIN a.doctor d
             JOIN a.patient p
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
              AND p.id = :patientId
              AND a.status = :status
           """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("doctorName") String doctorName,
                                                              @Param("patientId") Long patientId,
                                                              @Param("status") int status);
}
