package com.example.charityManagementSystem.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.charityManagementSystem.repository.NeedyRepo;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;

import com.stripe.model.Account;
import com.stripe.model.Account.Requirements;
import com.stripe.model.AccountLink;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;

import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.AccountRetrieveParams;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;

import jakarta.mail.MessagingException;

import com.example.charityManagementSystem.models.Needy;
import com.example.charityManagementSystem.models.UserNeedyPrincipal;

@Service
public class NeedyService {

    private final AuthenticationProvider authenticationProvider;

    private final NeedyRepo needyRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtservice;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final String adminEmail;

    @Value("${adminEmail}")
    private String tempAdminEmail;

    public NeedyService(NeedyRepo needyRepo,
            AuthenticationManager authManager,
            JWTService jwtservice,
            EmailService emailService,
            TokenService tokenService, AuthenticationProvider authenticationProvider) {
        this.needyRepo = needyRepo;
        this.authManager = authManager;
        this.jwtservice = jwtservice;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.authenticationProvider = authenticationProvider;
        this.adminEmail = tempAdminEmail;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<String> addNeedy(@RequestBody Needy needy)
            throws StripeException, MessagingException {

        System.out.println(needy);

        needy.setPassword(encoder.encode(needy.getPassword()));

        String needyEmail = needy.getEmail();

        AccountCreateParams accountParams = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS) 
                .setCountry("US") 
                .setEmail(needyEmail)
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(
                                        AccountCreateParams.Capabilities.CardPayments.builder()
                                                .setRequested(true)
                                                .build())
                                .setTransfers(
                                        AccountCreateParams.Capabilities.Transfers.builder()
                                                .setRequested(true)
                                                .build())
                                .build())
                .build();

        Account account = Account.create(accountParams);

        needy.setStripeId(account.getId());
        needyRepo.save(needy);

        AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                .setAccount(account.getId())
                .setRefreshUrl("https://yourdomain.com/reauth") 
                .setReturnUrl("https://yourdomain.com/done") 
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(linkParams);
        String onboardingUrl = accountLink.getUrl();

        String message = "<p>Hi,</p>"
               + "<p>Click the link below to verify your email:</p>"
               + "<p><a href='" + onboardingUrl + "'>Email Verification</a></p>"
               + "<p>If you did not request this, ignore this email.</p>";

        emailService.sendEmail(
                needyEmail,
                "Complete your Stripe Account Onboarding",
                message);



            //below we are sending one more email to admin to make needy verified ...
            //by checking completion of account verification in the stripe dashboard ...

            String adminEmialContent = needy.getEmail() + "\n" + needy.getUsername();
            emailService.sendEmail(adminEmail, "Make the needy Organization marked verified", adminEmialContent);
           
            

        return ResponseEntity.ok("Needy person added successfully");
    }

    public String getNeedyByUsername(Needy needy, String role) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                role + needy.getUsername(),
                needy.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtservice.generateToken(role + needy.getUsername());
        } else {
            return null;
        }
    }

    public boolean checkEmailVerifyNeedy(Needy needy) {
        Optional<Needy> db_needy = needyRepo.findById(needy.getId());
        if (db_needy.isPresent()) {
            Needy exist_db_needy = db_needy.get();
            if (exist_db_needy.getEmailVerificationStatus().equals("VERIFIED"))
                return true;
            else
                return false;
        }
        return false;
    }

    public String sendVerificationEmail(Needy needy) {
        String token = tokenService.generateEmailVerificationToken(null, needy);
        String url = "http://localhost:8085/needy/public/verify-email?token=" + token +
                "&role=NEEDY&username=" + needy.getUsername();

        String subject = "Email Verification Link";
        String htmlContent = "<a href=\"" + url + "\">Verify Email</a>";
        try {
            emailService.sendEmail(needy.getEmail(), subject, htmlContent);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "Email sent successfully";
    }

    public boolean verifyEmail(String token, String role, String username) {
        if (!tokenService.validateEmailVerificationToken(token, role, username)) {
            return false;
        }

        Needy dbNeedy = Optional.ofNullable(needyRepo.findByUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("Needy not found"));

        dbNeedy.setEmailVerificationStatus("VERIFIED");
        needyRepo.save(dbNeedy);
        return true;
    }

    public ResponseEntity<String> sendResetPasswordMail(Needy needyUser)
            throws UnsupportedEncodingException, MessagingException {

        Needy dbNeedy = needyRepo.findByEmail(needyUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String passToken = tokenService.generatePasswordResetToken(null, dbNeedy);
        String url = "http://localhost:8085/needy/public/ResetPass?token=" +
                passToken + "&email=" + dbNeedy.getEmail() + "&role=NEEDY";

        String subject = "Password Reset Link";
        String textContent = "<a href=\"" + url + "\">Password Reset Mail</a>";

        emailService.sendEmail(dbNeedy.getEmail(), subject, textContent);

        return ResponseEntity.ok("Password reset email sent successfully.");
    }

    public ResponseEntity<String> resetPassword(String passToken, String email, String newPassword) {
        Needy dbNeedy = needyRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean valid = tokenService.validatePasswordResetToken(passToken, email, "needy", newPassword);

        if (valid) {
            dbNeedy.setPassword(newPassword);
            needyRepo.save(dbNeedy);
        }

        return ResponseEntity.ok(valid ? "Password reset successfully!" : "Page error, try again!");
    }

    public ResponseEntity<String> resetEmail(Needy needyUser) {
        Needy dbNeedy = needyRepo.findByEmail(needyUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        dbNeedy.setEmail(needyUser.getEmail());
        needyRepo.save(dbNeedy);

        return ResponseEntity.ok("Email reset successfully!");
    }

    public ResponseEntity<String> directResetPassword(Needy needyUser) {
        Needy dbNeedy = needyRepo.findByEmail(needyUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        dbNeedy.setPassword(needyUser.getPassword());
        needyRepo.save(dbNeedy);

        return ResponseEntity.ok("Password reset successfully!");
    }

    public ResponseEntity<Double> getCollectedDonation(String needyUserrname) {
        Needy dbNeedy = needyRepo.findByUsername(needyUserrname);

        return ResponseEntity.ok(dbNeedy.getCollectedDonation());
    }

    public String getNeedyEmailByStripeId(Long needyId) {
        Optional<Needy> needy = needyRepo.findById(needyId);
        if (!needy.isPresent())
            return null;
        return needy != null ? needy.get().getEmail() : null;
    }

    public void updateNeedyVerificationStatus(Long needyId, boolean verified) {
        Optional<Needy> needy = needyRepo.findById(needyId);
        if (needy.isPresent()) {
            String verificationStatus = verified ? "VERIFIED" : "PENDING";
            needy.get().setAccountVerifiedStatus(verificationStatus);
            needyRepo.save(needy.get());
        }
    }

    public boolean checkStripeAccountVerification(Account account) {
        Account.Requirements r = account.getRequirements();
        return r.getCurrentlyDue().isEmpty() &&
                r.getEventuallyDue().isEmpty() &&
                r.getPendingVerification().isEmpty() &&
                r.getDisabledReason() == null;
    }

    public String generateOnboardingLink(Long needyId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("account", needyId);
        params.put("refresh_url", "https://yourapp.com/refresh");
        params.put("return_url", "https://yourapp.com/return");
        params.put("type", "account_onboarding");

        return AccountLink.create(params).getUrl();
    }

    public void updateCollectedDonation(Long needyId, Long paymentAmount) {
        Optional<Needy> needy = needyRepo.findById(needyId);
        if (needy.isPresent()) {
            double dbAmount = needy.get().getCollectedDonation();
            long amountA = (long) (dbAmount * 100);
            amountA += paymentAmount;
            double amountTobeStored = (double) ((double) amountA / 100.0);
            needy.get().setCollectedDonation(amountTobeStored);
            needyRepo.save(needy.get());
        }
    }

    public Needy findByUsername(String username) {
        Needy needy = needyRepo.findByUsername(username);
        return needy;
    }

    public Optional<Needy> findByEmail(String email) {
        Optional<Needy> needy = needyRepo.findByEmail(email);
        return needy;
    }

    public void save(Needy dbNeedy) {
        needyRepo.save(dbNeedy);
    }

    public UserDetails findPrincipalByUsername(String username) {
        Needy needy = needyRepo.findByUsername(username);
        UserNeedyPrincipal needyPrincipal = new UserNeedyPrincipal(needy);
        return needyPrincipal;
    }

    public boolean isAccountVerified(String username) {
        Needy needy = needyRepo.findByUsername(username);
        boolean accountStatus = needy.getAccountVerifiedStatus().equals("VERIFIED");
        return accountStatus;
    }

    public Needy findById(Long needyId) {
        Optional<Needy> needy = needyRepo.findById(needyId);
        if (needy.isPresent()) {
            return needy.get();
        }
        return null;
    }

}
