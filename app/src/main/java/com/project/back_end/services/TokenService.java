package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret:change-this-secret-to-a-long-random-string}")
    private String secret;

    private SecretKey key;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String identifier) {
        long now = System.currentTimeMillis();
        long sevenDays = 7L * 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(new Date(now))
                .expiration(new Date(now + sevenDays))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractIdentifier(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, String user) {
        try {
            String id = extractIdentifier(token);
            return switch (user.toLowerCase()) {
                case "admin" -> adminRepository.findByUsername(id) != null;
                case "doctor" -> doctorRepository.findByEmail(id) != null;
                case "patient" -> patientRepository.findByEmail(id) != null;
                default -> false;
            };
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public SecretKey getSigningKey() {
        return key;
    }
}
