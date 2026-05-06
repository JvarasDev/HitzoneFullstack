package cl.hitzone.fullstack.controller;

import cl.hitzone.fullstack.Dto.AuthResponseDTO;
import cl.hitzone.fullstack.Dto.LoginRequestDTO;
import cl.hitzone.fullstack.Dto.UserRegistrationDTO;
import cl.hitzone.fullstack.Model.User;
import cl.hitzone.fullstack.Service.AuthService;
import cl.hitzone.fullstack.config.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    // ── REGISTRO ─────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO registrationRequest) {
        try {
            User newUser = authService.registerUser(registrationRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario '" + newUser.getUsername() + "' registrado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            User user = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(token, user.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas: " + e.getMessage());
        }
    }
}
