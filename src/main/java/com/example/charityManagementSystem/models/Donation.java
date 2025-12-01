package com.example.charityManagementSystem.models;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long donationId;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "donated_org_id", nullable = false)
    private Long needyId;   // ---> this is a stripe id...

    @Column(name = "donated_amount", nullable = false)
    private Double donatedAmount = 0.0;

    @Column(name = "donation_status", nullable = false)
    private String donationStatus = "PENDING";

    @Column(name = "stripePaymentId", nullable = false)
    private String stripePaymentId;

}
