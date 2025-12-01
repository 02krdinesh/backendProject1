package com.example.charityManagementSystem.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.charityManagementSystem.models.Needy;
import com.example.charityManagementSystem.repository.NeedyRepo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

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
import com.stripe.param.checkout.SessionRetrieveParams;
import com.stripe.param.AccountRetrieveParams;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;

@Service
public class PaymentService {

    private final DonorService donorService;
    private final NeedyService needyService;
    private final DonationService donationService;
    private final EmailService emailService;

    private String stripeApiKey;
    private String webhookSecret;

    // @Value("${stripe.api.key}")
    // private String apiKey;

    // @Value("${stripe.api.webhooksecret}")
    // private String stripewebhook;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        stripeApiKey = apiKey;
        webhookSecret = stripewebhook;
    }

    public PaymentService(
            DonorService donorService,
            NeedyService needyService,
            DonationService donationService,
            EmailService emailService) {

        this.donorService = donorService;
        this.needyService = needyService;
        this.donationService = donationService;
        this.emailService = emailService;
    }

    // public Map<String, String> createCheckoutSession(Map<String, Object> data)
    // throws Exception {

    // if (!checkIsOnboardingComplete(data.get("needyUsername").toString())) {
    // return null;
    // }
    // double amount = Double.parseDouble(data.get("amount").toString());
    // Long donorId =
    // donorService.findByUsername(data.get("donorUsername").toString()).getId();
    // Long needyId =
    // needyService.findByUsername(data.get("needyUsername").toString()).getId();
    // String connectedAccountId =
    // needyService.findByUsername(data.get("needyUsername").toString()).getStripeId();

    // SessionCreateParams params = SessionCreateParams.builder()
    // .setMode(SessionCreateParams.Mode.PAYMENT)
    // .setPaymentIntentData(
    // SessionCreateParams.PaymentIntentData.builder()
    // .putMetadata("donorId", donorId.toString())
    // .putMetadata("needyId", needyId.toString())
    // .putMetadata("amount", String.valueOf(amount))
    // .build())
    // .setSuccessUrl("https://yourfrontend.com/success?session_id={CHECKOUT_SESSION_ID}")
    // .setCancelUrl("https://yourfrontend.com/cancel")
    // .addLineItem(
    // SessionCreateParams.LineItem.builder()
    // .setQuantity(1L)
    // .setPriceData(
    // SessionCreateParams.LineItem.PriceData.builder()
    // .setCurrency("usd")
    // .setUnitAmount((long) (amount * 100))
    // .setProductData(
    // SessionCreateParams.LineItem.PriceData.ProductData.builder()
    // .setName("Donation")
    // .build())
    // .build())
    // .build())
    // .build();

    // Map<String, String> map = new HashMap<>();

    // RequestOptions requestOptions = RequestOptions.builder()
    // .setStripeAccount(connectedAccountId)
    // .build();

    // Session session = Session.create(params, requestOptions);

    // session = Session.retrieve(session.getId(),
    // SessionRetrieveParams.builder()
    // .addExpand("payment_intent")
    // .build(),
    // requestOptions);

    // String paymentId = session.getPaymentIntentObject().getId();

    // map.put("paymentId", paymentId);
    // map.put("paymentUrl", session.getUrl());
    // map.put("needyId", needyId.toString());
    // map.put("donorId", donorId.toString());
    // map.put("amount", String.valueOf(amount));
    // return map;
    // }

    public boolean checkIsOnboardingComplete(String needyUsername) throws Exception {

        Needy needy = needyService.findByUsername(needyUsername);

        boolean isComplete = needy.getAccountVerifiedStatus().equals("VERIFIED");

        return isComplete;
    }

    public ResponseEntity<String> handlePaymentIntentEvent(
            Long donorId,
            Long needyId,
            String paymentId,
            Double amount) {
        try {
            String connectedStripeId = needyService.findById(needyId).getStripeId();

            PaymentIntent paymentIntent = PaymentIntent.retrieve(
                    paymentId,
                    RequestOptions.builder()
                            .setStripeAccount(connectedStripeId)
                            .build());

            String status = paymentIntent.getStatus();

            if ("succeeded".equals(status)) {
                donationService.makedonationPaid(paymentId, donorId, needyId, amount);
                return ResponseEntity.ok("Payment succeeded and donation processed.");
            } else if ("requires_payment_method".equals(status) ||
                    "canceled".equals(status) ||
                    "requires_action".equals(status) ||
                    "requires_confirmation".equals(status)) {

                donationService.makedonationFailed(paymentId, donorId, needyId, amount);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Payment failed or incomplete.");
            } else {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("Payment status: " + status);
            }

        } catch (StripeException e) {
            donationService.makedonationFailed(paymentId, donorId, needyId, amount);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching payment intent from Stripe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred.");
        }
    }

    public Map<String, String> createCheckoutSession(Map<String, Object> data) throws Exception {

        if (!checkIsOnboardingComplete(data.get("needyUsername").toString())) {
            return null;
        }

        double amount = Double.parseDouble(data.get("amount").toString());
        Long donorId = donorService.findByUsername(data.get("donorUsername").toString()).getId();
        Long needyId = needyService.findByUsername(data.get("needyUsername").toString()).getId();
        String connectedAccountId = needyService.findByUsername(data.get("needyUsername").toString()).getStripeId();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("donorId", donorId.toString())
                                .putMetadata("needyId", needyId.toString())
                                .putMetadata("amount", String.valueOf(amount))
                                .build())
                .setSuccessUrl("https://yourfrontend.com/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://yourfrontend.com/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((long) (amount * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Donation")
                                                                .build())
                                                .build())
                                .build())
                .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build();

        Session session = Session.create(params, requestOptions);

        String paymentId = null;
        if (session.getPaymentIntent() != null) {
            session = Session.retrieve(session.getId(),
                    SessionRetrieveParams.builder()
                            .addExpand("payment_intent")
                            .build(),
                    requestOptions);
            if (session.getPaymentIntentObject() != null) {
                paymentId = session.getPaymentIntentObject().getId();
            }
        }

        Map<String, String> map = new HashMap<>();
        map.put("paymentId", paymentId != null ? paymentId : "pending");
        map.put("paymentUrl", session.getUrl());
        map.put("needyId", needyId.toString());
        map.put("donorId", donorId.toString());
        map.put("amount", String.valueOf(amount));

        return map;
    }

}
