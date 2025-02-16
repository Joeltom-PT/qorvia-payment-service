package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.model.PaymentStatus;
import com.qorvia.paymentservice.service.PaymentService;
import com.qorvia.paymentservice.service.PayoutAccountService;
import com.qorvia.paymentservice.service.PayoutService;
import com.qorvia.paymentservice.service.RevenueService;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private static final String STRIPE_WEBHOOK_SECRET = "whsec_54485c1c064b53813218c624ac28195bf2793223dca24e3d35c4ae4a5857c24d";

    private final PaymentService paymentService;
    private final PayoutAccountService payoutAccountService;
    private final RevenueService revenueService;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(HttpServletRequest request, @RequestBody String payload) {
        String sigHeader = request.getHeader("Stripe-Signature");
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, STRIPE_WEBHOOK_SECRET);

            switch (event.getType()) {
                case "checkout.session.completed":
                    Session completedSession = (Session) event.getData().getObject();
                    try {
                        paymentService.updatePaymentStatus(completedSession.getId(), PaymentStatus.COMPLETED);
                        revenueService.updateRevenue(completedSession.getId());
                        logPaymentSuccess(completedSession);
                    } catch (Exception e) {
                        log.error("Failed to update payment status for session: {}", completedSession.getId(), e);
                    }
                    break;
                case "checkout.session.async_payment_failed":
                    Session failedSession = (Session) event.getData().getObject();
                    try {
                        paymentService.updatePaymentStatus(failedSession.getId(), PaymentStatus.FAILED);
                        logPaymentFailure(failedSession);
                    } catch (Exception e) {
                        log.error("Failed to update payment status for session: {}", failedSession.getId(), e);
                    }
                    break;
                case "checkout.session.async_payment_expired":
                    Session expiredSession = (Session) event.getData().getObject();
                    try {
                        paymentService.updatePaymentStatus(expiredSession.getId(), PaymentStatus.EXPIRED);
                        logPaymentExpiration(expiredSession);
                    } catch (Exception e) {
                        log.error("Failed to update payment status for session: {}", expiredSession.getId(), e);
                    }
                    break;
                case "checkout.session.async_payment_canceled":
                    Session canceledSession = (Session) event.getData().getObject();
                    try {
                        paymentService.updatePaymentStatus(canceledSession.getId(), PaymentStatus.CANCELED);
                        logPaymentCancellation(canceledSession);
                    } catch (Exception e) {
                        log.error("Failed to update payment status for session: {}", canceledSession.getId(), e);
                    }
                    break;
                case "refund.succeeded":
                    Refund refund = (Refund) event.getData().getObject();
                    try {
                        paymentService.handleRefundSucceeded(refund);
                    } catch (Exception e) {
                        log.error("Failed to update payment status for refund: {}", refund.getId(), e);
                    }
                    break;
                case "account.updated":
                    Account account = (Account) event.getData().getObject();
                    try {
                        payoutAccountService.handleAccountUpdate(account);
                    } catch (Exception e) {
                        log.error("Failed to handle account updated event: {}", account.getId(), e);
                    }
                    break;
                default:
                    log.warn("Unhandled event type: {}", event.getType());
                    break;
            }

            return ResponseEntity.ok("Event processed successfully");

        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error");
        }
    }


    private void logPaymentSuccess(Session session) {
        log.info("Payment successful! Session ID: {}", session.getId());
        log.info("Payment Amount: {} {}", session.getAmountTotal() / 100.0, session.getCurrency());
        log.info("Customer Email: {}", session.getCustomerEmail());
        log.info("Event ID: {}", session.getId());
        log.info("Payment Status: {}", session.getPaymentStatus());
    }

    private void logPaymentFailure(Session session) {
        log.error("Payment failed! Session ID: {}", session.getId());
        log.error("Payment Amount: {} {}", session.getAmountTotal() / 100.0, session.getCurrency());
        log.error("Customer Email: {}", session.getCustomerEmail());
        log.error("Event ID: {}", session.getId());
        log.error("Payment Status: {}", session.getPaymentStatus());
    }

    private void logPaymentExpiration(Session session) {
        log.warn("Payment expired! Session ID: {}", session.getId());
        log.warn("Payment Amount: {} {}", session.getAmountTotal() / 100.0, session.getCurrency());
        log.warn("Customer Email: {}", session.getCustomerEmail());
        log.warn("Event ID: {}", session.getId());
        log.warn("Payment Status: {}", session.getPaymentStatus());
    }

    private void logPaymentCancellation(Session session) {
        log.warn("Payment canceled! Session ID: {}", session.getId());
        log.warn("Payment Amount: {} {}", session.getAmountTotal() / 100.0, session.getCurrency());
        log.warn("Customer Email: {}", session.getCustomerEmail());
        log.warn("Event ID: {}", session.getId());
        log.warn("Payment Status: {}", session.getPaymentStatus());
    }

}
