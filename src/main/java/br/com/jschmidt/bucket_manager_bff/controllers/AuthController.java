package br.com.jschmidt.bucket_manager_bff.controllers;

import br.com.jschmidt.bucket_manager_bff.security.jwt.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        Object principal = authentication.getPrincipal();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("authenticated", true);

        switch (principal) {
            case OAuth2User oauth2User -> {
                userInfo.put("name", oauth2User.getAttribute("name"));
                userInfo.put("email", oauth2User.getAttribute("email"));
                userInfo.put("picture", oauth2User.getAttribute("picture"));
            }
            case org.springframework.security.oauth2.jwt.Jwt jwt -> {
                userInfo.put("name", jwt.getClaimAsString("name"));
                userInfo.put("email", jwt.getClaimAsString("email"));
                userInfo.put("claims", jwt.getClaims());
            }
            case String str -> userInfo.put("principal", str);
            case null, default -> {
                assert principal != null;
                userInfo.put("principal", principal.toString());
            }
        }

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/token")
    public ResponseEntity<?> generateToken(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String jwt = jwtUtil.generateJwtToken(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("expiresIn", jwtUtil.getJwtExpirationSeconds());

        return ResponseEntity.ok(response);
    }
}
