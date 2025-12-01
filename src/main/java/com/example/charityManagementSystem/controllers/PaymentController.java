package com.example.charityManagementSystem.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.charityManagementSystem.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/payments")
public class PaymentController {

    private final PaymentService paymentService;

    private String webhookSecret;

    @Value("${stripe.api.webhooksecret}")
    private String stripewebhook;

    @PostConstruct
    public void init() {
        webhookSecret = stripewebhook;
    }

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/public/stripe/checkOutSession")
    public ResponseEntity<Map<String, String>> postMethodName(@RequestBody Map<String, Object> payload)
            throws Exception {

        System.out.println("Creating checkcout Session");
        Map<String, String> map = paymentService.createCheckoutSession(payload);
        return ResponseEntity.ok(map);
    }

    @PostMapping("/public/stripe/handlePayment")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader,
            HttpServletRequest request) {

        String payload;
        Event event;
        String endpointSecret = webhookSecret;

        try (BufferedReader reader = request.getReader()) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (paymentIntent != null) {
                handlePaymentIntent(paymentIntent);
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (paymentIntent != null) {
                handlePaymentIntent(paymentIntent);
            }
        }

        return ResponseEntity.ok("Webhook processed");
    }

    private void handlePaymentIntent(PaymentIntent paymentIntent) {
        
        Map<String, String> metadata = paymentIntent.getMetadata();

        Long donorId = metadata.containsKey("donorId") ? Long.parseLong(metadata.get("donorId")) : null;
        Long needyId = metadata.containsKey("needyId") ? Long.parseLong(metadata.get("needyId")) : null;
        Double amount = metadata.containsKey("amount") ? Double.parseDouble(metadata.get("amount")) : null;

        String paymentId = paymentIntent.getId();

        paymentService.handlePaymentIntentEvent(donorId, needyId, paymentId, amount);
    }

}

/*
 * 
 * https://numerally-predacious-jarvis.ngrok-free.dev/payments/public/
 * createCheckOutSession
 * https://numerally-predacious-jarvis.ngrok-free.dev/payments/public/
 * sendRegsitrationMail
 * https://numerally-predacious-jarvis.ngrok-free.dev/payments/public/updateCADb
 * 
 */
