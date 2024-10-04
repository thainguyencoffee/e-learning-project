package com.elearning.payment.web;

import com.elearning.payment.application.PaymentRequest;
import com.elearning.payment.application.PaymentService;
import com.elearning.payment.domain.Payment;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> payment(@Valid @RequestBody PaymentRequest paymentRequest) {
        Payment payment = paymentService.pay(paymentRequest);
        return ResponseEntity.created(URI.create("/payments/" + payment.getId())).body(payment);
    }


}
