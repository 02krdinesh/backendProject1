package com.example.charityManagementSystem.controllers;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.charityManagementSystem.models.Donor;
import com.example.charityManagementSystem.services.DonorService;

import jakarta.mail.MessagingException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/donor")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    public String checkAuthority() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = null;
        if (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority();
        }
        return role;
    }

    @PostMapping("/public/register")
    public ResponseEntity<String> registerDonor(@RequestBody Donor donor) {
                
        return donorService.registerDonor(donor);
    }

    @PostMapping("/public/login")
    public ResponseEntity<String> loginDonor(@RequestBody Donor donor) {

        String jwtToken = donorService.authenticateDonor(donor, "DONOR");
        return jwtToken != null ? ResponseEntity.ok(jwtToken)
                : ResponseEntity.status(404).body("Invalid username or password");
    }

    @PostMapping("/public/check-email-verification")
    public ResponseEntity<Boolean> checkEmailVerificationStatus(@RequestBody Donor donor) {

        if (donorService.isDonorEmailVerified(donor)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(404).body(false);
    }

    @PostMapping("/public/send-verification-email")
    public ResponseEntity<String> sendEmailVerification(@RequestBody Donor donor)
            throws MessagingException {

       
        return donorService.sendVerificationEmail(donor, "DONOR");
    }

    @GetMapping("/public/verify-email")
    public ResponseEntity<String> verifyEmailAddress(@RequestParam("token") String token,
            @RequestParam("role") String role,
            @RequestParam("username") String username) {

        
        return donorService.verifyDonorEmail(token, role, username);
    }

    @PostMapping("/public/send-reset-password-email")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestBody Donor donor)
            throws UnsupportedEncodingException, MessagingException {

       
        return donorService.sendPasswordResetEmail(donor);
    }

    @PutMapping("/public/reset-password")
    public ResponseEntity<String> resetDonorPassword(@RequestParam("token") String token,
            @RequestParam("email") String email, @RequestBody Donor donor) {
       
        return donorService.resetDonorPassword(token, email, "DONOR", donor.getPassword());
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateDonorEmail(@RequestBody Donor donor) {
        if (!checkAuthority().equals("DONOR")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }
        return donorService.updateDonorEmail(donor);
    }

    @PutMapping("/direct-reset-password")
    public ResponseEntity<String> directPasswordReset(@RequestBody Donor donor) {
        if (!checkAuthority().equals("DONOR")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }
        return donorService.resetDonorPasswordDirectly(donor);
    }

    @GetMapping("/get-total-donated-amount")
    public ResponseEntity<Object> getTotalAmountDonated(@RequestParam(value = "donorUsername") String donorUsername) {
        System.out.println(checkAuthority());
        if (!checkAuthority().equals("DONOR")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }
        System.out.println("Getting total donated amount");
        return donorService.getTotalAmountDonated(donorUsername);
    }

}
