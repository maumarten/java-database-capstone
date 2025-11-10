package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor", indexes = {
        @Index(name = "ix_doctors_specialty", columnList = "specialty")
})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull @Size(min = 3, max = 50)
    @Column(nullable = false, length = 50)
    private String specialty;

    @NotNull @Email
    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @NotNull @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Column(nullable = false, length = 12)
    private String phone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "doctor_available_times", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "available_times", nullable = false, length = 32) // e.g., "09:00-10:00"
    private List<String> availableTimes = new ArrayList<>();

    public Doctor() {}

    public Doctor(Long id, String name, String specialty, String email, String password, String phone) {
        this.id = id; this.name = name; this.specialty = specialty; this.email = email; this.password = password; this.phone = phone;
    }

    // Getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getAvailableTimes() { return availableTimes; }
    public void setAvailableTimes(List<String> availableTimes) { this.availableTimes = availableTimes; }
}
