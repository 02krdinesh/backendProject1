package com.example.charityManagementSystem.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.charityManagementSystem.models.Admin;
import com.example.charityManagementSystem.models.Needy;
import com.example.charityManagementSystem.services.JWTService;
import com.example.charityManagementSystem.services.NeedyService;

@RestController
@RequestMapping("/admin")
public class AdminControleer {

    private final NeedyService needyService;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;

    public AdminControleer(NeedyService needyService,
            JWTService jwtService, AuthenticationManager authManager) {
        this.needyService = needyService;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    public String checkAuthority() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = null;
        if (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority();
        }
        return role;
    }

    @PostMapping("/public/login")
    public ResponseEntity<String> postMethodName(@RequestBody Admin admin) {
        String adminUsername = admin.getUsername();
        String adminPassword = admin.getPassword();

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                "ADMIN" + adminUsername,
                adminPassword));

        String jwtToken = jwtService.generateToken(adminUsername);
        return (authentication.isAuthenticated() && jwtToken != null)
                ? ResponseEntity.ok(jwtToken)
                : ResponseEntity.status(404).body("Invalid username or password");
    }

    @PostMapping("/completeVerf")
    public ResponseEntity<String> completeVerf(@RequestParam("needyUsername") String needyUsername) {

        if (!checkAuthority().equals("ADMIN")) {
            return ResponseEntity.status(404).body("Unauthorized");
        }

        Needy needy = needyService.findByUsername(needyUsername);
        String connectedAccountId = needy.getStripeId();

        Account account = Account.retrieve(connectedAccountId);

        Requirements requirements = account.getRequirements();

        List<String> currentlyDue = requirements.getCurrentlyDue();

        boolean isComplete = currentlyDue.isEmpty();

        if (isComplete) {
            System.out.println("Onboarding complete!");
            needy.setAccountVerifiedStatus("VERIFIED");
            needyService.save(needy);
        } else {
            System.out.println("Onboarding NOT complete.");
            System.out.println("Missing fields: " + currentlyDue);
        }

        return ResponseEntity.ok("Verification Completed");
    }

}
