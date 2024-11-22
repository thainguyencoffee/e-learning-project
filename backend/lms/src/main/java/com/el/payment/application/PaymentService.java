package com.el.payment.application;

import com.el.payment.domain.Payment;
import com.el.payment.domain.PaymentMethod;
import com.el.payment.domain.PaymentRepository;
import com.el.payment.web.dto.PaymentRequest;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentGateway stripePaymentGateway;

    public PaymentService(PaymentRepository paymentRepository, StripePaymentGateway stripePaymentGateway) {
        this.paymentRepository = paymentRepository;
        this.stripePaymentGateway = stripePaymentGateway;
    }

    public List<Payment> getAllPaymentsByCreatedByAndId(UUID orderId) {
        return paymentRepository.findAllByOrderId(orderId);
    }

    public Payment pay(PaymentRequest paymentRequest) {
        Payment payment = new Payment(paymentRequest.orderId(),
                paymentRequest.amount(), paymentRequest.paymentMethod());

        boolean isPaymentFailed = false;
        String failureMessage = null;

        if (payment.getPaymentMethod().equals(PaymentMethod.STRIPE)) {
            try {
                Charge charge = stripePaymentGateway.charge(paymentRequest);
                payment.markPaid(charge.getId(), charge.getReceiptUrl());
            } catch (CardException e) {
                isPaymentFailed = true;
                failureMessage = "Card error: " + e.getMessage();
            } catch (RateLimitException e) {
                isPaymentFailed = true;
                failureMessage = "Rate limit error: " + e.getMessage();
            } catch (InvalidRequestException e) {
                isPaymentFailed = true;
                failureMessage = "Invalid request: " + e.getMessage();
            } catch (AuthenticationException e) {
                isPaymentFailed = true;
                failureMessage = "Authentication error: " + e.getMessage();
            } catch (ApiConnectionException e) {
                isPaymentFailed = true;
                failureMessage = "API connection error: " + e.getMessage();
            } catch (ApiException e) {
                isPaymentFailed = true;
                failureMessage = "Stripe API error: " + e.getMessage();
            } catch (Exception e) {
                isPaymentFailed = true;
                failureMessage = "Unexpected error: " + e.getMessage();
            }
        } else {
            isPaymentFailed = true;
            failureMessage = "Unsupported payment method: " + payment.getPaymentMethod();
        }

        if (isPaymentFailed) {
            payment.markFailed(failureMessage);
        }

        return paymentRepository.save(payment);
    }

}
