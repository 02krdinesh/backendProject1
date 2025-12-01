package com.example.charityManagementSystem.services;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.charityManagementSystem.models.Donor;
import com.example.charityManagementSystem.models.UserDonorPrincipal;
import com.example.charityManagementSystem.repository.DonorRepo;
import jakarta.mail.MessagingException;

@Service
public class DonorService {

    private final DonorRepo donorRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final TokenService tokenService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public DonorService(DonorRepo donorRepo,
            AuthenticationManager authManager,
            JWTService jwtService,
            EmailService emailService,
            TokenService tokenService) {
        this.donorRepo = donorRepo;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    public ResponseEntity<String> registerDonor(Donor donor) {
        donor.setPassword(encoder.encode(donor.getPassword()));
        donorRepo.save(donor);
        return ResponseEntity.ok("Donor registered successfully");
    }

    public String authenticateDonor(Donor donor, String role) {
        System.out.println(donor);
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(role + donor.getUsername(), donor.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(role + donor.getUsername());
        }
        return null;
    }

    public boolean isDonorEmailVerified(Donor donor) {
        if (donor == null || donor.getUsername() == null) {
            System.out.println("Invalid donor object or donor ID");
            return false;
        }
        Donor storedDonor = donorRepo.findByUsername(donor.getUsername());
        String status = storedDonor.getEmailVerificationStatus();
        System.out.println("Email verification status: " + status);

        return "VERIFIED".equalsIgnoreCase(status);
    }

    public ResponseEntity<String> sendVerificationEmail(Donor donor, String role) throws MessagingException {
        if (donor.getEmail() == null || donor.getEmail().isEmpty())
            return ResponseEntity.status(400).body("Email is required");

        String token = tokenService.generateEmailVerificationToken(donor, null);
        String username = donor.getUsername();
        String url = "http://localhost:8085/donor/public/verify-email?token=" + token +
                "&role=" + role + "&username=" + username;

                String message = "<p>Hi,</p>"
               + "<p>Click the link below to verify your email:</p>"
               + "<p><a href='" + url + "'>Email Verification</a></p>"
               + "<p>If you did not request this, ignore this email.</p>";

        emailService.sendEmail(donor.getEmail(), "Email Verification Link", message);
        return ResponseEntity.ok("Verification email sent successfully!");
    }

    public ResponseEntity<String> verifyDonorEmail(String token, String role, String username) {
        boolean valid = tokenService.validateEmailVerificationToken(token, role, username);
        if (!valid)
            return ResponseEntity.status(400).body("Email verification failed!");
        return ResponseEntity.ok("Email successfully verified!");
    }

    public ResponseEntity<String> sendPasswordResetEmail(Donor donor)
            throws MessagingException, UnsupportedEncodingException {
        Donor dbDonor = donorRepo.findByEmail(donor.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = tokenService.generatePasswordResetToken(dbDonor, null);
        String url = "http://localhost:8085/donor/public/reset-password?token=" 
             + token + "&email=" + dbDonor.getEmail();

        String subject = "Password Reset Link";

        String message = "<p>Hi,</p>"
               + "<p>Click the link below to reset your password:</p>"
               + "<p><a href='" + url + "'>Reset Password</a></p>"
               + "<p>If you did not request this, ignore this email.</p>";

        emailService.sendEmail(dbDonor.getEmail(), subject, message);

        return ResponseEntity.ok("Password reset email sent successfully.");
    }

    public ResponseEntity<String> resetDonorPassword(String token, String email, String role, String newPassword) {
        boolean success = tokenService.validatePasswordResetToken(token, email, role, newPassword);
        return ResponseEntity.ok(success ? "Password reset successfully!" : "Invalid or expired token");
    }

    public ResponseEntity<String> updateDonorEmail(Donor donor) {
        Donor dbDonor = donorRepo.findByEmail(donor.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dbDonor.getEmailVerificationStatus().equalsIgnoreCase("VERIFIED")) {
            return ResponseEntity.status(400).body("Email is already verified");
        }

        dbDonor.setEmail(donor.getEmail());
        donorRepo.save(dbDonor);
        return ResponseEntity.ok("Email updated successfully!");
    }

    public ResponseEntity<String> resetDonorPasswordDirectly(Donor donor) {
        Donor dbDonor = donorRepo.findByEmail(donor.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        dbDonor.setPassword(encoder.encode(donor.getPassword()));
        donorRepo.save(dbDonor);
        return ResponseEntity.ok("Password reset successfully!");
    }

    public ResponseEntity<Object> getTotalAmountDonated(String donorUsername) {
        Donor donor = donorRepo.findByUsername(donorUsername);
        return ResponseEntity.ok(donor.getDonatedAmount());
    }

    public void updateDonatedAmount(Long donorId, Long paymentAmount) { // --> this is ok called by paymentService...
        Optional<Donor> donor = donorRepo.findById(donorId);
        if (donor.isPresent()) {
            double dbAmount = donor.get().getDonatedAmount();
            long amountA = (long) (dbAmount * 100);
            amountA += paymentAmount;
            double amountTobeStored = (double) ((double) amountA / 100.0);
            donor.get().setDonatedAmount(amountTobeStored);
            donorRepo.save(donor.get());
        }
    }

    public Donor findByUsername(String username) {
        Donor donor = donorRepo.findByUsername(username);
        return donor;
    }

    public Optional<Donor> findByEmail(String email) {
        Optional<Donor> donor = donorRepo.findByEmail(email);
        return donor;
    }

    public void save(Donor dbDonor) {
        donorRepo.save(dbDonor);
    }

    public UserDetails findPrincipalByUsername(String username) {
        Donor donor = donorRepo.findByUsername(username);
        UserDonorPrincipal donorPrincipal = new UserDonorPrincipal(donor);
        return donorPrincipal;
    }

}
