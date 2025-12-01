package com.example.charityManagementSystem.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.charityManagementSystem.models.Donor;
import com.example.charityManagementSystem.models.Needy;
import com.example.charityManagementSystem.repository.DonorRepo;
import com.example.charityManagementSystem.repository.NeedyRepo;

@Service
public class TokenService {

    private final DonorRepo donorRepo;
    private final NeedyRepo needyRepo;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    public TokenService(DonorRepo donorRepo, NeedyRepo needyRepo) {
        this.donorRepo = donorRepo;
        this.needyRepo = needyRepo;
    }

    // ------------------- Email Verification -------------------

    public String generateEmailVerificationToken(Donor donor, Needy needy) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        if (donor != null)
            updateDonorToken(donor, token, expiry, true);
        else if (needy != null)
            updateNeedyToken(needy, token, expiry, true);

        return token;
    }

    public boolean validateEmailVerificationToken(String token, String role, String username) {
        return switch (role) {
            case "DONOR" -> validateDonorToken(token, username, true);
            case "NEEDY" -> validateNeedyToken(token, username, true);
            default -> false;
        };
    }

    // ------------------- Password Reset -------------------

    public String generatePasswordResetToken(Donor donor, Needy needy) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);

        if (donor != null)
            updateDonorToken(donor, token, expiry, false);
        else if (needy != null)
            updateNeedyToken(needy, token, expiry, false);

        return token;
    }

    public boolean validatePasswordResetToken(String token, String email, String role, String newPassword) {
        return switch (role) {
            case "DONOR" -> validateAndSetPassword(donorRepo.findByEmail(email), token, newPassword);
            case "NEEDY" -> validateAndSetPassword(donorRepo.findByEmail(email), token, newPassword);
            default -> throw new IllegalArgumentException("Invalid user type");
        };
    }

    // ------------------- Helper Methods -------------------

    private void updateDonorToken(Donor donor, String token, LocalDateTime expiry, boolean isEmailVerification) {
        Donor dbDonor = donorRepo.findByUsername(donor.getUsername());
        if (dbDonor == null)
            throw new IllegalArgumentException("Donor not found");

        if (isEmailVerification) {
            dbDonor.setEmailVerificationToken(token);
            dbDonor.setExpriyDateTime(expiry);
        } else {
            dbDonor.setPasswordResetToken(token);
            dbDonor.setPassexpriyDateTime(expiry);
        }
        donorRepo.save(dbDonor);
    }

    private void updateNeedyToken(Needy needy, String token, LocalDateTime expiry, boolean isEmailVerification) {
        Needy dbNeedy = needyRepo.findByUsername(needy.getUsername());
        if (dbNeedy == null)
            throw new IllegalArgumentException("Needy user not found");

        if (isEmailVerification) {
            dbNeedy.setEmailVerificationToken(token);
            dbNeedy.setExpriyDateTime(expiry);
        } else {
            dbNeedy.setPasswordResetToken(token);
            dbNeedy.setPassexpriyDateTime(expiry);
        }
        needyRepo.save(dbNeedy);
    }

    private boolean validateDonorToken(String token, String username, boolean isEmailVerification) {

        Donor donor = donorRepo.findByUsername(username);
        if (donor == null)
            return false;

        String dbToken = isEmailVerification ? donor.getEmailVerificationToken() : donor.getPasswordResetToken();
        LocalDateTime expiry = isEmailVerification ? donor.getExpriyDateTime() : donor.getPassexpriyDateTime();

        if (isEmailVerification) {
            donor.setEmailVerificationStatus("VERIFIED");
            donor.setEmailVerificationToken(null);
            donor.setExpriyDateTime(null);
            donorRepo.save(donor);
        }

        return dbToken != null && dbToken.equals(token) && !expiry.isBefore(LocalDateTime.now());
    }

    private boolean validateNeedyToken(String token, String username, boolean isEmailVerification) {
        Needy needy = needyRepo.findByUsername(username);
        if (needy == null)
            return false;

        String dbToken = isEmailVerification ? needy.getEmailVerificationToken() : needy.getPasswordResetToken();
        LocalDateTime expiry = isEmailVerification ? needy.getExpriyDateTime() : needy.getPassexpriyDateTime();

        if (isEmailVerification) {
            needy.setEmailVerificationStatus("VERIFIED");
            needy.setEmailVerificationToken(null);
            needy.setExpriyDateTime(null);
            needyRepo.save(needy);
        }

        return dbToken != null && dbToken.equals(token) && !expiry.isBefore(LocalDateTime.now());
    }

    private <T> boolean validateAndSetPassword(Optional<T> optionalUser, String token, String newPassword) {
        if (!optionalUser.isPresent())
            throw new IllegalArgumentException("User not found");

        if (optionalUser.get() instanceof Donor donor) {
            if (!token.equals(donor.getPasswordResetToken())
                    || donor.getPassexpriyDateTime().isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("Invalid or expired token for donor");

            donor.setPassword(encoder.encode(newPassword));
            donor.setPasswordResetToken(null);
            donor.setPassexpriyDateTime(null);
            donorRepo.save(donor);
            return true;

        } else if (optionalUser.get() instanceof Needy needy) {
            if (!token.equals(needy.getPasswordResetToken())
                    || needy.getPassexpriyDateTime().isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("Invalid or expired token for needy");

            needy.setPassword(encoder.encode(newPassword));
            needy.setPassexpriyDateTime(null);
            needy.setPasswordResetToken(null);
            needyRepo.save(needy);
            return true;
        }

        throw new IllegalArgumentException("Unknown user type");
    }
}
