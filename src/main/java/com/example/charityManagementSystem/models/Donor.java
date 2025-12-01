package com.example.charityManagementSystem.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "donor_table")
public class Donor {

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

    @Column(name = "amount_donated")
    private Double donatedAmount = 0.0;

}
