package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.dto.request.GetAllPayoutRequest;
import com.qorvia.paymentservice.model.PayoutStatus;
import com.qorvia.paymentservice.security.RequireRole;
import com.qorvia.paymentservice.security.Roles;
import com.qorvia.paymentservice.service.PayoutService;
import com.qorvia.paymentservice.service.jwt.JwtService;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PayoutController {

    private final PayoutService payoutService;
    private final JwtService jwtService;

    @GetMapping("/admin/get-all-payouts")
    @RequireRole(role = Roles.ADMIN)
    public ResponseEntity<?> getAllPayoutsByAdmin(@RequestParam(required = false) String payoutStatus,
                                                  @RequestParam(required = false) Long organizerId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GetAllPayoutRequest getAllPayoutRequest = new GetAllPayoutRequest();
        if (payoutStatus != null) {
            try {
                getAllPayoutRequest.setPayoutStatus(PayoutStatus.valueOf(payoutStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid payoutStatus value: " + payoutStatus);
            }
        }
        getAllPayoutRequest.setOrganizerId(organizerId);
        getAllPayoutRequest.setStartDate(startDate);
        getAllPayoutRequest.setEndDate(endDate);
        return payoutService.getAllPayouts(getAllPayoutRequest);
    }

    @GetMapping("/organizer/get-all-payouts")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getAllPayoutsByOrganizer(@RequestParam(required = false) String payoutStatus,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                      HttpServletRequest httpServletRequest) {
        long organizerId = jwtService.getUserIdFormRequest(httpServletRequest);
        GetAllPayoutRequest getAllPayoutRequest = new GetAllPayoutRequest();
        if (payoutStatus != null) {
            try {
                getAllPayoutRequest.setPayoutStatus(PayoutStatus.valueOf(payoutStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid payoutStatus value: " + payoutStatus);
            }
        }
        getAllPayoutRequest.setOrganizerId(organizerId);
        getAllPayoutRequest.setStartDate(startDate);
        getAllPayoutRequest.setEndDate(endDate);
        return payoutService.getAllPayouts(getAllPayoutRequest);
    }


    @PostMapping("/perform-payout/{id}")
    @RequireRole(role = Roles.ADMIN)
    public ResponseEntity<?> performPayout(@PathVariable("id") Long payoutId){
        return payoutService.performPayout(payoutId);
    }



}
