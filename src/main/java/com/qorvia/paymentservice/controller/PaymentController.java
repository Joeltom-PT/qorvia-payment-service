package com.qorvia.paymentservice.controller;

import com.qorvia.paymentservice.dto.PaymentInfoDTO;
import com.qorvia.paymentservice.dto.PaymentSessionInfo;
import com.qorvia.paymentservice.dto.PayoutInfoDTO;
import com.qorvia.paymentservice.dto.RefundDTO;
import com.qorvia.paymentservice.dto.request.ExternalAccountRequest;
import com.qorvia.paymentservice.dto.request.PaymentRequestDTO;
import com.qorvia.paymentservice.dto.request.RefundRequestDTO;
import com.qorvia.paymentservice.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.stripe.model.Token;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.param.AccountCreateParams;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<PaymentInfoDTO> createCheckoutSession(@RequestBody PaymentRequestDTO paymentRequestDTO) throws Exception {
        PaymentSessionInfo paymentSessionInfo = paymentService.createCheckoutSession(paymentRequestDTO);

        PaymentInfoDTO paymentInfoDTO = new PaymentInfoDTO();
        paymentInfoDTO.setPaymentUrl(paymentSessionInfo.getSession().getUrl());
        paymentInfoDTO.setSessionId(paymentSessionInfo.getSession().getId());
        paymentInfoDTO.setTempSessionId(paymentSessionInfo.getInitialSessionId());
        return ResponseEntity.ok(paymentInfoDTO);
    }




}
