package com.example.charityManagementSystem.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "needy_table")
public class Needy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "collected_donation")
    private Double collectedDonation = 0.0;

    @Column(name = "account_verified_status")
    private String accountVerifiedStatus = "PENDING";

    @Column(name = "email_verification_token")
    private String emailVerificationToken = null;

    @Column(name = "email_verification_status")
    private String emailVerificationStatus = "PENDING";

    @Column(name = "email_expiry_date")
    private LocalDateTime expriyDateTime = null;

    @Column(name = "password_reset_token")
    private String passwordResetToken = null;

    @Column(name = "pass_expiry_date")
    private LocalDateTime passexpriyDateTime = null;

    @Column(name = "stripe_id", nullable = false, unique = true)
    private String stripeId;

}
