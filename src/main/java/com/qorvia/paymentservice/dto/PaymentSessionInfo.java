package com.qorvia.paymentservice.dto;

import lombok.Data;
import com.stripe.model.checkout.Session;


@Data
public class PaymentSessionInfo {

   private Session session;
   private String initialSessionId;

}
