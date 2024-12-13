package com.el.payment.application;

import com.el.common.Currencies;
import com.el.payment.web.dto.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.money.NumberValue;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StripePaymentGateway {

    @Value("${STRIPE_SECRET_KEY}")
    String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Charge charge(PaymentRequest paymentRequest) throws StripeException {
        NumberValue amountNumber = paymentRequest.price().getNumber();
        if (paymentRequest.price().getCurrency() == Currencies.USD) {
            amountNumber = paymentRequest.price().multiply(100).getNumber();
        }
        log.info("Stripe process: amount={}, currency={}", amountNumber, paymentRequest.price().getCurrency());

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amountNumber);
        chargeParams.put("currency", paymentRequest.price().getCurrency());
        chargeParams.put("source", paymentRequest.token());
        return Charge.create(chargeParams);
    }

}
