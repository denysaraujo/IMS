package com.br.api.controller;

import com.br.api.model.User;
import com.br.api.repository.UserRepository;
import com.br.api.security.JwtTokenProvider;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    
    public AuthController(JwtTokenProvider jwtTokenProvider,
                         AuthenticationManager authenticationManager,
                         PasswordEncoder passwordEncoder,
                         UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    // Classe DTO para a requisição de login
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters e Setters
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(), 
                    request.getPassword()
                )
            );
            
            String token = jwtTokenProvider.generateToken(request.getUsername());
            

            User user = userRepository.findByUsername(request.getUsername())
               .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            LoginResponse loginResponse = new LoginResponse(token, user);

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }

     // Classe DTO para a resposta do login
    public static class LoginResponse {
        private String token;
        private UserResponse user;

        public LoginResponse(String token, User user) {
            this.token = token;
            this.user = new UserResponse(user.getUsername(), user.getNomeCompleto(), user.getRole());
        }

        // Getters e Setters
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserResponse getUser() {
            return user;
        }

        public void setUser(UserResponse user) {
            this.user = user;
        }
    }

    // Classe DTO para os dados do usuário na resposta
    public static class UserResponse {
        private String username;
        private String nomeCompleto;
        private String role;

        public UserResponse(String username, String nomeCompleto, String role) {
            this.username = username;
            this.nomeCompleto = nomeCompleto;
            this.role = role;
        }

        // Getters e Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public void setNomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
