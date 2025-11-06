package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "admins", indexes = {
        @Index(name = "ix_admins_username", columnList = "username", unique = true)
})
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "username cannot be null")
    @Size(min = 3, max = 64)
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @NotNull(message = "password cannot be null")
    @Size(min = 8, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // write-only in JSON
    @Column(nullable = false, length = 255)
    private String password;

    public Admin() {}

    public Admin(Long id, String username, String password) {
        this.id = id; this.username = username; this.password = password;
    }

    // Getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
