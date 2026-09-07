package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.response.VerifyOtpResponseDTO;
import com.e.commerce.mini.Repository.PasswordResetOtpRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.PasswordResetService;
import com.e.commerce.mini.Service.SmsService;
import com.e.commerce.mini.models.PasswordResetOtp;
import com.e.commerce.mini.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl
        implements PasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 5;

    private static final int MAX_OTP_ATTEMPTS = 5;

    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;

    private static final String OTP_TYPE_EMAIL = "EMAIL";

    private static final String OTP_TYPE_PHONE = "PHONE";

    private final UserRepository userRepository;

    private final PasswordResetOtpRepository passwordResetOtpRepository;

    private final JavaMailSender mailSender;

    private final PasswordEncoder passwordEncoder;

    private final SmsService smsService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Override
    public void requestOtp(
            String identifier,
            String otpType
    ) {

        String normalizedType =
                normalizeOtpType(otpType);

        String normalizedIdentifier =
                normalizeIdentifier(
                        identifier,
                        normalizedType
                );

        System.out.println("[PasswordReset] Request received. OTP type=" + normalizedType + ", identifier=" + normalizedIdentifier);

        User user =
                findUserForRecovery(
                        normalizedIdentifier,
                        normalizedType
                );

        /*
         * Keep the response generic for security.
         * Do not reveal whether an account exists.
         */
        if (user == null) {
            System.out.println("[PasswordReset] User not found.");
            return;
        }

        /*
         * Google/OAuth accounts do not have a normal
         * CommerceCore password that should be reset
         * through this flow.
         */
        System.out.println("[PasswordReset] User found. id=" + user.getId() + ", local=" + isLocalAccount(user));

        if (!isLocalAccount(user)) {
            System.out.println("[PasswordReset] Account is not LOCAL. Password reset skipped.");
            return;
        }

        PasswordResetOtp latestOtp =
                findLatestOtp(
                        normalizedIdentifier,
                        normalizedType
                );

        if (latestOtp != null) {

            LocalDateTime now =
                    LocalDateTime.now();

            long secondsSinceLastRequest =
                    Duration.between(
                            latestOtp.getCreatedAt(),
                            now
                    ).getSeconds();

            if (
                    secondsSinceLastRequest
                            < RESEND_COOLDOWN_SECONDS
            ) {

                long remainingSeconds =
                        RESEND_COOLDOWN_SECONDS
                                - secondsSinceLastRequest;

                throw new IllegalStateException(
                        "Please wait "
                                + remainingSeconds
                                + " seconds before requesting another OTP."
                );
            }
        }

        String otp =
                generateOtp();

        PasswordResetOtp passwordResetOtp =
                new PasswordResetOtp();

        if (OTP_TYPE_EMAIL.equals(normalizedType)) {

            passwordResetOtp.setEmail(
                    normalizedIdentifier
            );

            passwordResetOtp.setPhoneNumber(null);

        } else {

            passwordResetOtp.setEmail(null);

            passwordResetOtp.setPhoneNumber(
                    normalizedIdentifier
            );
        }

        passwordResetOtp.setUserId(
                user.getId()
        );

        passwordResetOtp.setOtpType(
                normalizedType
        );

        passwordResetOtp.setOtpHash(
                hashValue(otp)
        );

        passwordResetOtp.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(
                                OTP_EXPIRY_MINUTES
                        )
        );

        passwordResetOtp.setAttempts(0);

        passwordResetOtp.setVerifiedAt(null);

        passwordResetOtp.setResetTokenHash(null);

        passwordResetOtp.setResetTokenExpiresAt(null);

        passwordResetOtpRepository.saveAndFlush(
                passwordResetOtp
        );

        System.out.println("[PasswordReset] OTP saved to database. id=" + passwordResetOtp.getId());

        if (OTP_TYPE_EMAIL.equals(normalizedType)) {

            System.out.println("[PasswordReset] Sending EMAIL OTP...");

            sendEmailOtp(
                    normalizedIdentifier,
                    otp
            );

            System.out.println("[PasswordReset] EMAIL OTP sent successfully.");

        } else {

            System.out.println("[PasswordReset] Sending PHONE OTP...");

            smsService.sendOtp(
                    normalizedIdentifier,
                    otp
            );

            System.out.println("[PasswordReset] PHONE OTP sent successfully.");
        }
    }

    @Override
    @Transactional
    public VerifyOtpResponseDTO verifyOtp(
            String identifier,
            String otp,
            String otpType
    ) {

        String normalizedType =
                normalizeOtpType(otpType);

        String normalizedIdentifier =
                normalizeIdentifier(
                        identifier,
                        normalizedType
                );

        if (
                otp == null ||
                        !otp.matches("\\d{6}")
        ) {

            throw new IllegalArgumentException(
                    "Invalid OTP."
            );
        }

        PasswordResetOtp resetOtp =
                findLatestOtp(
                        normalizedIdentifier,
                        normalizedType
                );

        if (resetOtp == null) {

            throw new IllegalArgumentException(
                    "Invalid or expired OTP."
            );
        }

        if (
                !normalizedType.equals(
                        resetOtp.getOtpType()
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid OTP."
            );
        }

        if (
                resetOtp.getVerifiedAt() != null
        ) {

            throw new IllegalArgumentException(
                    "OTP has already been verified."
            );
        }

        if (
                resetOtp.getAttempts()
                        >= MAX_OTP_ATTEMPTS
        ) {

            throw new IllegalArgumentException(
                    "Maximum OTP attempts exceeded. Please request a new OTP."
            );
        }

        if (
                LocalDateTime.now()
                        .isAfter(
                                resetOtp.getExpiresAt()
                        )
        ) {

            throw new IllegalArgumentException(
                    "OTP expired."
            );
        }

        String submittedOtpHash =
                hashValue(otp);

        boolean otpMatches =
                MessageDigest.isEqual(
                        submittedOtpHash.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        resetOtp.getOtpHash()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        if (!otpMatches) {

            resetOtp.setAttempts(
                    resetOtp.getAttempts() + 1
            );

            passwordResetOtpRepository.save(
                    resetOtp
            );

            if (
                    resetOtp.getAttempts()
                            >= MAX_OTP_ATTEMPTS
            ) {

                throw new IllegalArgumentException(
                        "Maximum OTP attempts exceeded. Please request a new OTP."
                );
            }

            throw new IllegalArgumentException(
                    "Invalid OTP."
            );
        }

        User user =
                userRepository.findById(
                        resetOtp.getUserId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found."
                        )
                );

        if (!isLocalAccount(user)) {

            throw new IllegalArgumentException(
                    "Password reset is not available for this account."
            );
        }

        String resetToken =
                generateResetToken();

        resetOtp.setVerifiedAt(
                LocalDateTime.now()
        );

        resetOtp.setResetTokenHash(
                hashValue(resetToken)
        );

        resetOtp.setResetTokenExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(
                                RESET_TOKEN_EXPIRY_MINUTES
                        )
        );

        passwordResetOtpRepository.save(
                resetOtp
        );

        return new VerifyOtpResponseDTO(
                "OTP verified successfully.",
                resetToken
        );
    }

    @Override
    @Transactional
    public void resetPassword(
            String resetToken,
            String newPassword,
            String confirmPassword
    ) {

        if (
                resetToken == null ||
                        resetToken.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Reset token is required."
            );
        }

        if (
                newPassword == null ||
                        newPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "New password is required."
            );
        }

        if (
                confirmPassword == null ||
                        confirmPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Confirm password is required."
            );
        }

        if (
                !newPassword.equals(
                        confirmPassword
                )
        ) {

            throw new IllegalArgumentException(
                    "Passwords do not match."
            );
        }

        if (newPassword.length() < 8) {

            throw new IllegalArgumentException(
                    "Password must be at least 8 characters."
            );
        }

        String resetTokenHash =
                hashValue(resetToken);

        PasswordResetOtp resetOtp =
                passwordResetOtpRepository
                        .findByResetTokenHash(
                                resetTokenHash
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired reset session."
                                )
                        );

        if (
                resetOtp.getVerifiedAt() == null
        ) {

            throw new IllegalArgumentException(
                    "OTP verification is required."
            );
        }

        if (
                resetOtp.getResetTokenExpiresAt()
                        == null ||
                        LocalDateTime.now()
                                .isAfter(
                                        resetOtp.getResetTokenExpiresAt()
                                )
        ) {

            throw new IllegalArgumentException(
                    "Reset session expired. Please start again."
            );
        }

        if (resetOtp.getUserId() == null) {

            throw new IllegalArgumentException(
                    "Unable to identify the account."
            );
        }

        User user =
                userRepository.findById(
                        resetOtp.getUserId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found."
                        )
                );

        if (!isLocalAccount(user)) {

            throw new IllegalArgumentException(
                    "Password reset is not available for this account."
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        /*
         * Invalidate the reset token so the same token
         * cannot be reused.
         */
        resetOtp.setResetTokenHash(null);

        resetOtp.setResetTokenExpiresAt(null);

        resetOtp.setVerifiedAt(
                LocalDateTime.now()
        );

        passwordResetOtpRepository.save(
                resetOtp
        );
    }

    private User findUserForRecovery(
            String identifier,
            String otpType
    ) {

        if (OTP_TYPE_EMAIL.equals(otpType)) {

            return userRepository
                    .findByEmail(identifier)
                    .orElse(null);
        }

        return userRepository
                .findByContactNo(identifier)
                .orElse(null);
    }

    private PasswordResetOtp findLatestOtp(
            String identifier,
            String otpType
    ) {

        if (OTP_TYPE_EMAIL.equals(otpType)) {

            return passwordResetOtpRepository
                    .findTopByEmailOrderByCreatedAtDesc(
                            identifier
                    )
                    .orElse(null);
        }

        return passwordResetOtpRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(
                        identifier
                )
                .orElse(null);
    }

    private boolean isLocalAccount(
            User user
    ) {

        return user != null &&
                "LOCAL".equalsIgnoreCase(
                        user.getAuthProvider()
                );
    }

    private String normalizeOtpType(
            String otpType
    ) {

        if (otpType == null) {

            throw new IllegalArgumentException(
                    "OTP type is required."
            );
        }

        String normalized =
                otpType.trim()
                        .toUpperCase();

        if (
                !OTP_TYPE_EMAIL.equals(
                        normalized
                ) &&
                        !OTP_TYPE_PHONE.equals(
                                normalized
                        )
        ) {

            throw new IllegalArgumentException(
                    "OTP type must be EMAIL or PHONE."
            );
        }

        return normalized;
    }

    private String normalizeIdentifier(
            String identifier,
            String otpType
    ) {

        if (
                identifier == null ||
                        identifier.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Email or phone number is required."
            );
        }

        if (OTP_TYPE_EMAIL.equals(otpType)) {

            return identifier
                    .trim()
                    .toLowerCase();
        }

        String phone =
                identifier
                        .replaceAll(
                                "[^0-9]",
                                ""
                        );

        if (
                phone.startsWith("91") &&
                        phone.length() == 12
        ) {

            phone =
                    phone.substring(2);
        }

        if (phone.length() != 10) {

            throw new IllegalArgumentException(
                    "Please enter a valid 10-digit phone number."
            );
        }

        return phone;
    }

    private String generateOtp() {

        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );
    }

    private String generateResetToken() {

        byte[] tokenBytes =
                new byte[32];

        secureRandom.nextBytes(
                tokenBytes
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        tokenBytes
                );
    }

    private String hashValue(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to secure password reset data.",
                    exception
            );
        }
    }

    private void sendEmailOtp(
            String email,
            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "CommerceCore Password Reset OTP"
        );

        message.setText(
                """
                Hello,

                We received a request to reset your CommerceCore password.

                Your verification OTP is:

                %s

                This OTP is valid for 5 minutes.

                You can request a new OTP after the resend cooldown.

                If you did not request a password reset, please ignore this email.

                Regards,
                CommerceCore Team
                """.formatted(otp)
        );

        try {
            mailSender.send(message);
        } catch (Exception exception) {
            System.err.println("[PasswordReset] EMAIL OTP failed: " + exception.getMessage());
            exception.printStackTrace();
            throw new IllegalStateException("Unable to send password reset email.", exception);
        }
    }
}