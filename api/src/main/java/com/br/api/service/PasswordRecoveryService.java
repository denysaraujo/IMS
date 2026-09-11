package com.br.api.service;

import com.br.api.model.PasswordResetToken;
import com.br.api.model.User;
import com.br.api.repository.PasswordResetTokenRepository;
import com.br.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@Transactional
public class PasswordRecoveryService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
    }

    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            tokenRepository.deleteByUserId(user.getId());
            String token = HexFormat.of().formatHex(randomBytes(32));
            tokenRepository.save(new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15)));
            sendResetEmail(user, token);
        });
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Token ou senha inválidos");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        user.setLoginAttempts(0);
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private void sendResetEmail(User user, String token) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException("Recuperação de senha indisponível: SMTP não configurado ou usuário sem e-mail");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Recuperação de senha - ISMS");
        message.setText("Use este token para redefinir sua senha: " + token + "\nValidade: 15 minutos.");
        mailSender.send(message);
    }

    private byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
