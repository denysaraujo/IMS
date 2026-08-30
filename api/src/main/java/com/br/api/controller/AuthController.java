package com.br.api.controller;

import com.br.api.dto.user.UserResponseDTO;
import com.br.api.model.User;
import com.br.api.repository.UserRepository;
import com.br.api.security.JwtTokenProvider;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = "http://localhost:4200",
        maxAge = 3600
)
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            UserRepository userRepository) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    // =========================================================
    // LOGIN REQUEST
    // =========================================================

    public static class LoginRequest {

        private String username;
        private String password;

        public LoginRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // =========================================================
    // LOGIN RESPONSE
    // =========================================================

    public static class LoginResponse {

        private String token;
        private UserResponseDTO user;

        public LoginResponse() {
        }

        public LoginResponse(
                String token,
                User user) {

            this.token = token;
            this.user = new UserResponseDTO(user);
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserResponseDTO getUser() {
            return user;
        }

        public void setUser(UserResponseDTO user) {
            this.user = user;
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository
                    .findByUsername(request.getUsername())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Usuário não encontrado"
                            )
                    );

            String token =
                    jwtTokenProvider.generateToken(
                            user.getUsername()
                    );

            LoginResponse response =
                    new LoginResponse(token, user);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "error",
                                    "Credenciais inválidas"
                            )
                    );
        }
    }
}

