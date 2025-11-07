package com.project.back_end.security;

import java.util.Map;

public interface TokenValidationService {
    /**
     * Validate token for a given role.
     * Return an empty map when valid; otherwise return errors (e.g., {"error":"expired"}).
     */
    Map<String, String> validateToken(String token, String requiredRole);
}