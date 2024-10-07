package com.el.payment.application;

import com.el.payment.domain.Payment;
import com.el.payment.domain.PaymentRepository;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository; // Repository để thao tác với DB
    private final StripePaymentGateway stripePaymentGateway; // Adapter cho Stripe

    public PaymentService(PaymentRepository paymentRepository, StripePaymentGateway stripePaymentGateway) {
        this.paymentRepository = paymentRepository;
        this.stripePaymentGateway = stripePaymentGateway;
    }

    public Payment pay (PaymentRequest paymentRequest) {
        Payment payment = new Payment(paymentRequest.orderId(),
                paymentRequest.amount(), paymentRequest.paymentMethod());

        switch (payment.getPaymentMethod()) {
            case STRIPE -> {
                try {
                    Charge charge = stripePaymentGateway.charge(paymentRequest);
                    payment.markPaid(charge.getId());
                } catch (Exception e) {
                    payment.markFailed();
                    paymentRepository.save(payment);
                    throw new RuntimeException(e);
                }
            }
            default -> {
                payment.markFailed();
                paymentRepository.save(payment);
                throw new IllegalArgumentException("Unsupported payment method: " + payment.getPaymentMethod());
            }
        }

        return paymentRepository.save(payment);
    }
}
