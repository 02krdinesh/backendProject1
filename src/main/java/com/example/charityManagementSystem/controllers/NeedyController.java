
package com.example.charityManagementSystem.controllers;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.charityManagementSystem.models.Needy;
import com.example.charityManagementSystem.services.NeedyService;
import com.stripe.exception.StripeException;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/needy")
public class NeedyController {

    private final NeedyService needyService;

    public NeedyController(NeedyService needyService) {
        this.needyService = needyService;
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
    public ResponseEntity<String> registerUser(@RequestBody Needy needyUser) throws StripeException, MessagingException {
       
            needyService.addNeedy(needyUser);
       
        return ResponseEntity.ok("Needy user registered successfully");
    }

    @PostMapping("/public/login")
    public ResponseEntity<String> login(@RequestBody Needy needyUser) {


        String jwtToken = needyService.getNeedyByUsername(needyUser, "NEEDY");
        return jwtToken != null
                ? ResponseEntity.ok(jwtToken)
                : ResponseEntity.status(404).body("Invalid username or password");
    }

    @PostMapping("/public/check-email-verification-status")
    public ResponseEntity<Boolean> checkEmailVerifyNeedy(@RequestBody Needy needyUser) {

       
        boolean isChecked = needyService.checkEmailVerifyNeedy(needyUser);
        return ResponseEntity.ok(isChecked);
    }

    @PostMapping("/public/send-verification-email")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody Needy needyUser) throws MessagingException {

     
        String email = needyUser.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(400).body("Email is required");
        }
        needyService.sendVerificationEmail(needyUser);
        return ResponseEntity.ok("Verification email sent successfully!");
    }

    @GetMapping("/public/verify-email-token")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token,
            @RequestParam("role") String role,
            @RequestParam("username") String username) {



        return needyService.verifyEmail(token, role, username) ? ResponseEntity.ok("Email successfully verified!")
                : ResponseEntity.status(400).body("Email is not verified");
    }

    @PostMapping("/public/send-reset-password-email")
    public ResponseEntity<String> sendResetPasswordMail(@RequestBody Needy needyUser)
            throws UnsupportedEncodingException, MessagingException {

      

        return needyService.sendResetPasswordMail(needyUser);
    }

    @PutMapping("/public/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
            @RequestParam String email,
            @RequestParam String newPassword) {

      
        return needyService.resetPassword(token, email, newPassword);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> resetEmail(@RequestBody Needy needyUser) {

        if (!checkAuthority().equals("NEEDY")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }


        return needyService.resetEmail(needyUser);
    }

    @PutMapping("/direct/update-password")
    public ResponseEntity<String> directResetPassword(@RequestBody Needy needyUser) {


        if (!checkAuthority().equals("NEEDY")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }


        return needyService.directResetPassword(needyUser);
    }

    @GetMapping("/donations/total-collected")
    public ResponseEntity<Double> getCollectedDonation(@RequestParam String needyUserrname) {

        if (!checkAuthority().equals("NEEDY")) {
            return ResponseEntity.status(404).body(null);
        }


        return needyService.getCollectedDonation(needyUserrname);
    }

    @GetMapping("/public/account-verification-status")
    public ResponseEntity<Boolean> checkAccountVerifiedStatus(@RequestParam String username) {

        boolean isVerified = needyService.isAccountVerified(username);
        return ResponseEntity.ok(isVerified);
    }

}
