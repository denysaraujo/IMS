package com.br.api.service;

import com.br.api.model.PasswordResetToken;
import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.repository.PasswordResetTokenRepository;
import com.br.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void requestReset_shouldCreateTokenAndSendEmail() {
        User user = new User("Usuário", "usuario", "encoded", Role.USER);
        user.setId(1L);
        user.setEmail("usuario@example.com");
        when(userRepository.findByEmailIgnoreCase("usuario@example.com")).thenReturn(Optional.of(user));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        PasswordRecoveryService service = new PasswordRecoveryService(
                userRepository, tokenRepository, passwordEncoder, mailSenderProvider);

        service.requestReset("usuario@example.com");

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_shouldEncodeAndPersistNewPassword() {
        User user = new User("Usuário", "usuario", "old", Role.USER);
        user.setId(1L);
        PasswordResetToken resetToken = new PasswordResetToken("token", user, java.time.LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        PasswordRecoveryService service = new PasswordRecoveryService(
                userRepository, tokenRepository, passwordEncoder, mailSenderProvider);

        service.resetPassword("token", "new-password");

        verify(passwordEncoder).encode("new-password");
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
        assertTrue(resetToken.isUsed());
    }
}
