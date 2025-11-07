package com.project.back_end.security;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenValidationServiceImpl implements TokenValidationService {

    @Override
    public Map<String, String> validateToken(String token, String requiredRole) {
        // TODO: Replace with real JWT/DB validation:
        // - verify signature / expiration
        // - check role claim/authority
        if (token == null || token.isBlank()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "missing_token");
            return err;
        }
        // Pretend it's valid for demo:
        return Collections.emptyMap();
    }
}
