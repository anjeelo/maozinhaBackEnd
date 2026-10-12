package br.com.maozinha.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String senderEmail;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String senderEmail
    ) {
        this.mailSender = Objects.requireNonNull(
                mailSender,
                "mailSender não pode ser nulo"
        );

        this.senderEmail = validateEmail(
                senderEmail
        );
    }

    public void sendResetToken(
            String recipientEmail,
            String rawToken
    ) {
        String recipient = validateEmail(
                recipientEmail
        );

        Objects.requireNonNull(
                rawToken,
                "O token de recuperação não pode ser nulo."
        );

        if (rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "O token de recuperação não pode estar vazio."
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipient);
        message.setSubject("Mãozinha - Recuperação de senha");

        message.setText(
                """
                Você solicitou a recuperação da sua senha no Mãozinha.

                Utilize o token abaixo para definir uma nova senha:

                %s

                Este token expira em 30 minutos e pode ser utilizado apenas uma vez.

                Não compartilhe este token com outras pessoas.

                Se você não solicitou a recuperação, ignore este e-mail.
                Sua senha não será alterada sem a utilização do token.

                Equipe Mãozinha
                """.formatted(rawToken)
        );

        mailSender.send(message);
    }

    private static String validateEmail(
            String email
    ) {
        Objects.requireNonNull(
                email,
                "O endereço de e-mail não pode ser nulo."
        );

        String normalizedEmail = email.trim();

        if (normalizedEmail.isBlank()
                || normalizedEmail.contains("\r")
                || normalizedEmail.contains("\n")) {
            throw new IllegalArgumentException(
                    "O endereço de e-mail é inválido."
            );
        }

        return normalizedEmail;
    }
}