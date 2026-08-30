package com.br.api.service;

import com.br.api.dto.user.UserCreateDTO;
import com.br.api.dto.user.UserPasswordDTO;
import com.br.api.dto.user.UserResponseDTO;
import com.br.api.dto.user.UserUpdateDTO;
import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAll() {

        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado: " + id));

        return new UserResponseDTO(user);
    }

    public UserResponseDTO create(UserCreateDTO dto) {

        validateNewUser(dto);

        Role role = dto.getRole() != null
                ? dto.getRole()
                : Role.USER;

        User user = new User();

        user.setNomeCompleto(dto.getNomeCompleto());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setTelefone(dto.getTelefone());
        user.setCelular(dto.getCelular());
        user.setRole(role);

        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setLoginAttempts(0);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(savedUser);
    }

    public UserResponseDTO update(Long id, UserUpdateDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado: " + id));

        if (dto.getUsername() != null
                && !dto.getUsername().equalsIgnoreCase(user.getUsername())
                && userRepository.existsByUsernameAndIdNot(
                        dto.getUsername(), id)) {

            throw new IllegalArgumentException(
                    "Username já está sendo utilizado.");
        }

        if (dto.getEmail() != null
                && !dto.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailAndIdNot(
                        dto.getEmail(), id)) {

            throw new IllegalArgumentException(
                    "E-mail já está sendo utilizado.");
        }

        if (dto.getNomeCompleto() != null) {
            user.setNomeCompleto(dto.getNomeCompleto());
        }

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getTelefone() != null) {
            user.setTelefone(dto.getTelefone());
        }

        if (dto.getCelular() != null) {
            user.setCelular(dto.getCelular());
        }

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }

        if (dto.getAccountNonLocked() != null) {
            user.setAccountNonLocked(dto.getAccountNonLocked());
        }

        if (dto.getAccountNonExpired() != null) {
            user.setAccountNonExpired(dto.getAccountNonExpired());
        }

        if (dto.getCredentialsNonExpired() != null) {
            user.setCredentialsNonExpired(
                    dto.getCredentialsNonExpired());
        }

        return new UserResponseDTO(userRepository.save(user));
    }

    public void delete(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado: " + id));

        userRepository.delete(user);
    }

    public UserResponseDTO getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."));

        return new UserResponseDTO(user);
    }

    public UserResponseDTO updateCurrentUser(
            String username,
            UserUpdateDTO dto) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."));

        if (dto.getNomeCompleto() != null) {
            user.setNomeCompleto(dto.getNomeCompleto());
        }

        if (dto.getEmail() != null) {

            boolean emailChanged =
                    !dto.getEmail().equalsIgnoreCase(user.getEmail());

            if (emailChanged &&
                    userRepository.existsByEmailAndIdNot(
                            dto.getEmail(), user.getId())) {

                throw new IllegalArgumentException(
                        "E-mail já está sendo utilizado.");
            }

            user.setEmail(dto.getEmail());
        }

        if (dto.getTelefone() != null) {
            user.setTelefone(dto.getTelefone());
        }

        if (dto.getCelular() != null) {
            user.setCelular(dto.getCelular());
        }

        return new UserResponseDTO(userRepository.save(user));
    }

    public void changePassword(
            String username,
            UserPasswordDTO dto) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."));

        if (!passwordEncoder.matches(
                dto.getCurrentPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Senha atual inválida.");
        }

        if (dto.getNewPassword() == null
                || dto.getNewPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "A nova senha é obrigatória.");
        }

        user.setPassword(
                passwordEncoder.encode(dto.getNewPassword()));

        user.setCredentialsNonExpired(true);
        user.setLoginAttempts(0);

        userRepository.save(user);
    }

    private void validateNewUser(UserCreateDTO dto) {

        if (dto.getUsername() == null
                || dto.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "Username é obrigatório.");
        }

        if (dto.getPassword() == null
                || dto.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Senha é obrigatória.");
        }

        if (dto.getNomeCompleto() == null
                || dto.getNomeCompleto().isBlank()) {

            throw new IllegalArgumentException(
                    "Nome completo é obrigatório.");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException(
                    "Username já está sendo utilizado.");
        }

        if (dto.getEmail() != null
                && !dto.getEmail().isBlank()
                && userRepository.existsByEmail(dto.getEmail())) {

            throw new IllegalArgumentException(
                    "E-mail já está sendo utilizado.");
        }
    }
}