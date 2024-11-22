package com.el.payment.web;

import com.el.payment.web.dto.PaymentRequest;
import com.el.payment.application.PaymentService;
import com.el.payment.domain.Payment;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/orders/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentByMyOrderId(@PathVariable UUID orderId) {
        List<Payment> payments = paymentService.getAllPaymentsByCreatedByAndId(orderId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> payment(@Valid @RequestBody PaymentRequest paymentRequest) {
        Payment payment = paymentService.pay(paymentRequest);
        return ResponseEntity.created(URI.create("/payments/" + payment.getId())).body(payment);
    }


}
