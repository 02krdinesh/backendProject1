package com.example.charityManagementSystem.services;

import org.springframework.stereotype.Service;

import com.example.charityManagementSystem.models.Donation;
import com.example.charityManagementSystem.repository.DonationRepo;

@Service
public class DonationService {

    private final DonationRepo donationRepo;

    public DonationService(DonationRepo donationRepo) {
        this.donationRepo = donationRepo;
    }



    public void makedonationPaid(String paymentId, Long donorId, Long needyId, Double amount) {

        System.out.println("saving a paid donation");


        Donation donation = new Donation();
        donation.setStripePaymentId(paymentId);
        donation.setDonorId(donorId);
        donation.setNeedyId(needyId); 
        donation.setDonatedAmount(amount);
        donation.setDonationStatus("PAID");

        donationRepo.save(donation);
    }

    public void makedonationFailed(String paymentId, Long donorId, Long needyId, Double amount) {

        System.out.println("saving a failed donation");
        Donation donation = new Donation();
        donation.setStripePaymentId(paymentId);
        donation.setDonorId(donorId);
        donation.setNeedyId(needyId); 
        donation.setDonatedAmount(amount);
        donation.setDonationStatus("PENDING");

        donationRepo.save(donation);
    }
}
