package com.qorvia.paymentservice.service;

import com.qorvia.paymentservice.client.EventServiceClient;
import com.qorvia.paymentservice.dto.PaymentSessionInfo;
import com.qorvia.paymentservice.dto.PaymentStatusChangeDTO;
import com.qorvia.paymentservice.dto.RefundDTO;
import com.qorvia.paymentservice.dto.request.PaymentRequestDTO;
import com.qorvia.paymentservice.dto.request.RefundRequestDTO;
import com.qorvia.paymentservice.exceptions.PaymentNotFoundException;
import com.qorvia.paymentservice.model.Payment;
import com.qorvia.paymentservice.model.PaymentStatus;
import com.qorvia.paymentservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${payment.success.url}")
    private String successUrl;

    @Value("${payment.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.account.id}")
    private String accountId;

    private final PaymentRepository paymentRepository;


    private final EventServiceClient eventManagementClient;



    @Override
    public PaymentSessionInfo createCheckoutSession(PaymentRequestDTO paymentRequestDTO) throws StripeException {
        long finalAmount = paymentRequestDTO.getAmount();
        Long amountInSubunits = finalAmount * 100;

        log.info("Total amount for payment: {}", amountInSubunits);

        long expirationTimeInSeconds = 1800;
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long expirationTimestamp = currentTimeInSeconds + expirationTimeInSeconds;

        SessionCreateParams initialParams =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(paymentRequestDTO.getCurrency())
                                                        .setUnitAmount(amountInSubunits)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(paymentRequestDTO.getEventData().getEventName())
                                                                        .addImage(paymentRequestDTO.getEventData().getImgUrl())
                                                                        .build())
                                                        .build())
                                        .build())
                        .setExpiresAt(expirationTimestamp)
                        .build();

        Session initialSession = Session.create(initialParams);

        String encodedSessionId = Base64.getEncoder().encodeToString(initialSession.getId().getBytes());

        String successUrlWithParams = successUrl + "?session_id=" + encodedSessionId + "&status=success";
        String cancelUrlWithParams = cancelUrl + "?session_id=" + encodedSessionId + "&status=failure";

        SessionCreateParams finalParams =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrlWithParams)
                        .setCancelUrl(cancelUrlWithParams)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(paymentRequestDTO.getCurrency())
                                                        .setUnitAmount(amountInSubunits)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(paymentRequestDTO.getEventData().getEventName())
                                                                        .addImage(paymentRequestDTO.getEventData().getImgUrl())
                                                                        .build())
                                                        .build())
                                        .build())
                        .setExpiresAt(expirationTimestamp)
                        .build();

        Session session = Session.create(finalParams);

        Payment payment = new Payment();
        payment.setEventId(paymentRequestDTO.getEventData().getEventId());
        payment.setEventOrganizerId(paymentRequestDTO.getEventData().getEventOrganizerId());
        payment.setUserEmail(paymentRequestDTO.getEmail());
        payment.setAmount(amountInSubunits);
        payment.setCurrency(paymentRequestDTO.getCurrency());
        payment.setEventName(paymentRequestDTO.getEventData().getEventName());
        payment.setSessionId(session.getId());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpirationTimestamp(expirationTimestamp);
        paymentRepository.save(payment);

        PaymentSessionInfo paymentSessionInfo = new PaymentSessionInfo();
        paymentSessionInfo.setSession(session);
        paymentSessionInfo.setInitialSessionId(encodedSessionId);

        return paymentSessionInfo;
    }


    @Override
    public void updatePaymentStatus(String sessionId, PaymentStatus status) {
        try {
            Payment payment = paymentRepository.findBySessionId(sessionId);
            log.debug("Payment retrieved: {}", payment);

            PaymentStatusChangeDTO paymentStatusChangeDTO = new PaymentStatusChangeDTO();
            paymentStatusChangeDTO.setPaymentSessionId(payment.getSessionId());
            paymentStatusChangeDTO.setUserEmail(payment.getUserEmail());
            paymentStatusChangeDTO.setEventId(payment.getEventId());
            paymentStatusChangeDTO.setPaymentStatus(status);

                log.info("Updating payment status message to the event management service with status: {} and sessionId: {}", status, sessionId);

            eventManagementClient.paymentStatusUpdate(paymentStatusChangeDTO);

            payment.setPaymentStatus(status);
            paymentRepository.save(payment);

            log.info("Payment status updated to {} for Session ID: {}", status, sessionId);
        } catch (Exception e) {
            log.error("Failed to update payment status for Session ID: {}", sessionId, e);
        }
    }


    @Override
    public RefundDTO refundPayment(RefundRequestDTO refundRequest) throws StripeException {
        try {
            Payment payment = paymentRepository.findBySessionId(refundRequest.getSessionId());

            if (payment == null || payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                return createRefundDTO(refundRequest.getSessionId(), "FAILED", "Invalid session ID or payment is not completed.");
            }

            Session session = Session.retrieve(refundRequest.getSessionId());
            String paymentIntentId = session.getPaymentIntent();

            long refundAmount = (payment.getAmount() * refundRequest.getRefundPercentage()) / 100;

            Refund refund = Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(refundAmount)
                    .build());

            log.info("Refund successful for Payment Intent ID: {} for amount: {}", paymentIntentId, refundAmount);

            payment.setPaymentStatus(PaymentStatus.REFUND_IN_PROGRESS);
            payment.setRefundId(refund.getId());
            paymentRepository.save(payment);

            RefundDTO refundDTO = createRefundDTO(refundRequest.getSessionId(), "SUCCESS", null);
            refundDTO.setRefundAmount(refundAmount);

            return refundDTO;
        } catch (StripeException e) {
            log.error("Error during refund with session ID: {} error: {}", refundRequest.getSessionId(), e);

            RefundDTO refundDTO = createRefundDTO(refundRequest.getSessionId(), "FAILED", e.getMessage());
            return refundDTO;
        } catch (Exception e) {
            log.error("General error during refund with session ID: {} error: {}", refundRequest.getSessionId(), e);

            RefundDTO refundDTO = createRefundDTO(refundRequest.getSessionId(), "FAILED", e.getMessage());
            return refundDTO;
        }
    }



    @Override
    public void handleRefundSucceeded(Refund refund) {
        try {
            Payment payment = paymentRepository.findByRefundId(refund.getId());
            if (payment == null) {
                log.error("No payment found for Payment Intent ID: {}", refund.getPaymentIntent());
                return;
            }

            if ("succeeded".equals(refund.getStatus())) {
                payment.setPaymentStatus(PaymentStatus.REFUND_PROCESSED);
                payment.setRefundId(refund.getId());
                paymentRepository.save(payment);
                log.info("Refund succeeded for Payment Intent ID: {}. Refunded amount: {}. Refund ID: {}",
                        refund.getPaymentIntent(), refund.getAmount(), refund.getId());
            } else {
                log.warn("Refund failed for Payment Intent ID: {}. Refund Status: {}", refund.getPaymentIntent(), refund.getStatus());
            }

        } catch (Exception e) {
            log.error("Error processing refund success for Payment Intent ID: {}. Error: {}", refund.getPaymentIntent(), e);
        }
    }

    @Override
    public Payment getPaymentBySessionId(String sessionId) {
        return Optional.ofNullable(paymentRepository.findBySessionId(sessionId))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for Session ID: " + sessionId));
    }




    private RefundDTO createRefundDTO(String sessionId, String status, String errorMessage) {
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSessionId(sessionId);
        refundDTO.setRefundStatus(status);
        refundDTO.setErrorMessage(errorMessage);
        return refundDTO;
    }

}
