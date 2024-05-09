package com.nihed.booknetwork.auth;

import com.nihed.booknetwork.email.EmailService;
import com.nihed.booknetwork.email.EmailTemplateName;
import com.nihed.booknetwork.role.RoleRepository;
import com.nihed.booknetwork.user.Token;
import com.nihed.booknetwork.user.TokenRepository;
import com.nihed.booknetwork.user.User;
import com.nihed.booknetwork.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole =roleRepository.findByName("USER")
                //to do better exception handling
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initialized"));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"

        );
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        LocalDateTime now = LocalDateTime.now();
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(now.toLocalDate())
                .expiredAt(now.plusMinutes(15).toLocalDate())
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }


    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i= 0; i < length; i++){
           int randomIndex = secureRandom.nextInt(characters.length());
           codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
