package com.qorvia.paymentservice.client;

import com.qorvia.paymentservice.dto.PaymentStatusChangeDTO;
import com.qorvia.paymentservice.dto.message.PaymentStatusChangeMessage;
import com.qorvia.paymentservice.messaging.RabbitMQSender;
import com.qorvia.paymentservice.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventServiceClient {

    private final RabbitMQSender rabbitMQSender;

    public void paymentStatusUpdate(PaymentStatusChangeDTO paymentStatusChangeDTO) {
        PaymentStatusChangeMessage message = new PaymentStatusChangeMessage();

        message.setPaymentSessionId(paymentStatusChangeDTO.getPaymentSessionId());
        message.setUserEmail(paymentStatusChangeDTO.getUserEmail());
        message.setEventId(paymentStatusChangeDTO.getEventId());
        message.setPaymentStatus(paymentStatusChangeDTO.getPaymentStatus());

        rabbitMQSender.sendAsyncMessage(
                AppConstants.EVENT_MANAGEMENT_SERVICE_ASYNC_QUEUE,
                AppConstants.EVENT_MANAGEMENT_SERVICE_EXCHANGE,
                AppConstants.EVENT_MANAGEMENT_SERVICE_ROUTING_KEY,
                message
        );
    }


}
