package com.pghub.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(OwnerRepository ownerRepository, PasswordEncoder passwordEncoder) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Validate inputs
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password cannot be empty."));
        }

        // Check if username already exists
        if (ownerRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is already registered."));
        }

        // Create new owner and encrypt password with BCrypt
        Owner newOwner = new Owner();
        newOwner.setUsername(username);
        newOwner.setPassword(passwordEncoder.encode(password)); // Crucial step for security!
        newOwner.setRole("OWNER");

        ownerRepository.save(newOwner);

        return ResponseEntity.ok(Map.of("message", "Owner registered successfully! Ready for login."));
    }
}