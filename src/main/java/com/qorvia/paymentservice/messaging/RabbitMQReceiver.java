package com.qorvia.paymentservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qorvia.paymentservice.dto.PaymentInfoDTO;
import com.qorvia.paymentservice.dto.PaymentSessionInfo;
import com.qorvia.paymentservice.dto.RefundDTO;
import com.qorvia.paymentservice.dto.message.PaymentRequestMessage;
import com.qorvia.paymentservice.dto.message.RefundRequestMessage;
import com.qorvia.paymentservice.dto.message.StripeAccountOnboardingRequestMessage;
import com.qorvia.paymentservice.dto.message.response.PaymentInfoMessageResponse;
import com.qorvia.paymentservice.dto.message.response.RefundMessageResponse;
import com.qorvia.paymentservice.dto.message.response.StripeAccountOnboardingRequestMessageResponse;
import com.qorvia.paymentservice.dto.request.PaymentRequestDTO;
import com.qorvia.paymentservice.dto.request.RefundRequestDTO;
import com.qorvia.paymentservice.service.PaymentService;
import com.qorvia.paymentservice.service.PayoutAccountService;
import com.qorvia.paymentservice.utils.AppConstants;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQReceiver {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final PayoutAccountService payoutAccountService;
    private final PaymentService paymentService;

    @RabbitListener(queues = { AppConstants.PAYMENT_SERVICE_RPC_QUEUE })
    public void receiveMessage(Message amqpMessage) {
        try {
            byte[] messageBytes = amqpMessage.getBody();
            log.info("I am getting the message bytes as : ========================================== : {}", new String(messageBytes, StandardCharsets.UTF_8));
            MessageProperties amqpProps = amqpMessage.getMessageProperties();
            String correlationId = amqpProps.getCorrelationId();
            if (correlationId != null) {
                log.info("Received RPC message with correlation ID: {}", correlationId);
            }

            Map<String, Object> messageMap = objectMapper.readValue(messageBytes, Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "stipe-onboarding-message-request":
                    StripeAccountOnboardingRequestMessage stripeAccountOnboardingRequestMessage = objectMapper.convertValue(messageMap, StripeAccountOnboardingRequestMessage.class);
                    StripeAccountOnboardingRequestMessageResponse stripeAccountOnboardingRequestMessageResponse = handleStripeAccountOnboardingRequestMessage(stripeAccountOnboardingRequestMessage);
                    sendRpcResponse(amqpProps, stripeAccountOnboardingRequestMessageResponse);
                    break;
                case "payment-request-message":
                    PaymentRequestMessage paymentRequestMessage = objectMapper.convertValue(messageMap, PaymentRequestMessage.class);
                    PaymentInfoMessageResponse paymentInfoMessageResponse = handlePaymentRequestMessage(paymentRequestMessage);
                    sendRpcResponse(amqpProps, paymentInfoMessageResponse);
                    break;
                case "refund-request":
                    RefundRequestMessage requestMessage = objectMapper.convertValue(messageMap, RefundRequestMessage.class);
                    RefundMessageResponse refundMessageResponse = handleRefundRequestMessage(requestMessage);
                    sendRpcResponse(amqpProps, refundMessageResponse);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process message", e);
        }
    }

    private StripeAccountOnboardingRequestMessageResponse handleStripeAccountOnboardingRequestMessage(StripeAccountOnboardingRequestMessage message) {
        log.info("Generating account connecting url for : {}", message.getEmail());
        try {
            String url = payoutAccountService.connectOrganizerAccount(message);
            StripeAccountOnboardingRequestMessageResponse messageResponse = new StripeAccountOnboardingRequestMessageResponse();
            messageResponse.setMessage("");
            messageResponse.setUrl(url);
            return messageResponse;
        } catch (StripeException e){
            return null;
        }
    }

    private PaymentInfoMessageResponse handlePaymentRequestMessage(PaymentRequestMessage message) {
        try {
            PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
            paymentRequestDTO.setAmount(message.getAmount());
            paymentRequestDTO.setCurrency(message.getCurrency());
            paymentRequestDTO.setEmail(message.getEmail());

            PaymentRequestDTO.EventData eventData = new PaymentRequestDTO.EventData(
                    message.getEventId(),
                    message.getEventOrganizerId(),
                    message.getEventName(),
                    message.getImgUrl()
            );
            paymentRequestDTO.setEventData(eventData);

            PaymentSessionInfo paymentSessionInfo = paymentService.createCheckoutSession(paymentRequestDTO);

            PaymentInfoMessageResponse paymentInfoMessageResponse = new PaymentInfoMessageResponse();
            paymentInfoMessageResponse.setPaymentUrl(paymentSessionInfo.getSession().getUrl());
            paymentInfoMessageResponse.setSessionId(paymentSessionInfo.getSession().getId());
            paymentInfoMessageResponse.setTempSessionId(paymentSessionInfo.getInitialSessionId());

            return paymentInfoMessageResponse;
        } catch (StripeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private RefundMessageResponse handleRefundRequestMessage(RefundRequestMessage message) {

        try {
            RefundRequestDTO requestDTO = RefundRequestDTO.builder()
                    .refundPercentage(message.getRefundPercentage())
                    .sessionId(message.getSessionId())
                    .build();

            RefundDTO refundDTO = paymentService.refundPayment(requestDTO);

            RefundMessageResponse response = new RefundMessageResponse();

            response.setSessionId(refundDTO.getSessionId());
            response.setRefundAmount(refundDTO.getRefundAmount());
            response.setRefundStatus(refundDTO.getRefundStatus());
            response.setErrorMessage(refundDTO.getErrorMessage());

            return response;

        } catch (StripeException e) {
            return null;
        }
    }




    private void sendRpcResponse(MessageProperties amqpProps, Object response) throws JsonProcessingException {
        byte[] responseBytes = objectMapper.writeValueAsBytes(response);
        MessageProperties responseProperties = new MessageProperties();
        responseProperties.setCorrelationId(amqpProps.getCorrelationId());
        responseProperties.setContentType("application/octet-stream");

        Message responseMessage = new Message(responseBytes, responseProperties);
        rabbitTemplate.send(amqpProps.getReplyTo(), responseMessage);
    }
}
