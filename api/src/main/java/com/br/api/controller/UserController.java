package com.br.api.controller;

import com.br.api.dto.user.UserCreateDTO;
import com.br.api.dto.user.UserPasswordDTO;
import com.br.api.dto.user.UserResponseDTO;
import com.br.api.dto.user.UserUpdateDTO;
import com.br.api.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
        origins = "http://localhost:4200",
        maxAge = 3600
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================
    // ADMINISTRAÇÃO DE USUÁRIOS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponseDTO>> findAll() {

        return ResponseEntity.ok(
                userService.findAll()
        );
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponseDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.findById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> create(
            @RequestBody UserCreateDTO dto) {

        return ResponseEntity.ok(
                userService.create(dto)
        );
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {

        return ResponseEntity.ok(
                userService.update(id, dto)
        );
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // PERFIL DO USUÁRIO LOGADO
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getCurrentUser(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            Authentication authentication,
            @RequestBody UserUpdateDTO dto) {

        return ResponseEntity.ok(
                userService.updateCurrentUser(
                        authentication.getName(),
                        dto
                )
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody UserPasswordDTO dto) {

        userService.changePassword(
                authentication.getName(),
                dto
        );

        return ResponseEntity.noContent().build();
    }
}