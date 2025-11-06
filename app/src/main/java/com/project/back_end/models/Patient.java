package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "ix_patients_email", columnList = "email", unique = true)
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull @Email
    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @NotNull @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    @Column(nullable = false, length = 10)
    private String phone;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String address;

    public Patient() {}

    public Patient(Long id, String name, String email, String password, String phone, String address) {
        this.id = id; this.name = name; this.email = email; this.password = password; this.phone = phone; this.address = address;
    }

    // Getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
