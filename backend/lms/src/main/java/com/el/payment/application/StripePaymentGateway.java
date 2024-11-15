package com.el.payment.application;

import com.el.common.Currencies;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.money.NumberValue;
import java.util.HashMap;
import java.util.Map;

@Component
public class StripePaymentGateway {

    @Value("${STRIPE_SECRET_KEY}")
    String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Charge charge(PaymentRequest paymentRequest) throws StripeException {
        NumberValue amountNumber = paymentRequest.amount().getNumber();
        if (paymentRequest.amount().getCurrency() == Currencies.USD) {
            amountNumber = paymentRequest.amount().multiply(100).getNumber();
        }

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amountNumber);
        chargeParams.put("currency", paymentRequest.amount().getCurrency());
        chargeParams.put("source", paymentRequest.token());
        return Charge.create(chargeParams);
    }

}
