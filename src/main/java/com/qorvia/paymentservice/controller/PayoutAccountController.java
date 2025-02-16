package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.security.RequireRole;
import com.qorvia.paymentservice.security.Roles;
import com.qorvia.paymentservice.service.PayoutAccountService;
import com.qorvia.paymentservice.service.PayoutService;
import com.qorvia.paymentservice.service.jwt.JwtService;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PayoutAccountController {

    private final PayoutAccountService payoutAccountService;
    private final JwtService jwtService;

//    @PostMapping("/onboard-stripe-account")
//    public ResponseEntity<String> generateAccountLinkForOnboarding(@RequestBody StripeAccountOnboardingRequest stripeAccountOnboardingRequest) {
//        log.info("Requested to get the connecting link");
//        try {
//            if (payoutAccountService.isConnected(stripeAccountOnboardingRequest.getOrganizerId())) {
//                log.warn("Account is already connected for organizer ID: {}", stripeAccountOnboardingRequest.getOrganizerId());
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("Account is already connected.");
//            }
//
//            String url = payoutAccountService.connectOrganizerAccount(stripeAccountOnboardingRequest);
//            log.info("Connecting link generated: {}", url);
//            return ResponseEntity.ok(url);
//
//        } catch (StripeException e) {
//            log.error("Stripe API error: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stripe error: " + e.getMessage());
//
//        } catch (Exception e) {
//            log.error("Unexpected error: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("An unexpected error occurred. Please try again later.");
//        }
//    }

    @GetMapping("/get-connected-account-details")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getStripeAccountDetails(HttpServletRequest servletRequest){
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return payoutAccountService.getStripeAccountDetails(organizerId);
    }

    @DeleteMapping("/remove-connected-account")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> removeConnectedAccount(HttpServletRequest servletRequest){
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return payoutAccountService.removeConnectedAccount(organizerId);
    }


}
