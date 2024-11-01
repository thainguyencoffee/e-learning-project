package com.el.payment.domain;

import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTests {

    @Test
    void createPayment_ValidParameters_CreatesPaymentSuccessfully() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;

        Payment payment = new Payment(orderId, amount, paymentMethod);

        assertEquals(orderId, payment.getOrderId());
        assertNull(payment.getPaymentDate());
        assertEquals(amount, payment.getAmount());
        assertEquals(paymentMethod, payment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void createPayment_NegativeAmount_ThrowsException() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(-100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;

        assertThrows(InputInvalidException.class, () -> new Payment(orderId, amount, paymentMethod));
    }

    @Test
    void markPaid_ValidTransactionId_MarksPaymentAsPaid() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;
        Payment payment = new Payment(orderId, amount, paymentMethod);
        String transactionId = "TX123456";
        String receiptUrl = "https://example.com/receipt";

        payment.markPaid(transactionId, receiptUrl);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertNotNull(payment.getPaymentDate());
        assertEquals(transactionId, payment.getTransactionId());
    }

    @Test
    void markPaid_NonPendingStatus_ThrowsException() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;
        Payment payment = new Payment(orderId, amount, paymentMethod);
        String receiptUrl = "https://example.com/receipt";
        payment.markPaid("TX123456", receiptUrl);

        assertThrows(InputInvalidException.class, () -> payment.markPaid("TX789012", receiptUrl));
    }

    @Test
    void markFailed_PendingStatus_MarksPaymentAsFailed() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;
        Payment payment = new Payment(orderId, amount, paymentMethod);

        payment.markFailed();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void markFailed_NonPendingStatus_ThrowsException() {
        UUID orderId = UUID.randomUUID();
        MonetaryAmount amount = Money.of(100, Currencies.VND);
        PaymentMethod paymentMethod = PaymentMethod.STRIPE;
        Payment payment = new Payment(orderId, amount, paymentMethod);

        String receiptUrl = "https://example.com/receipt";
        payment.markPaid("TX123456", receiptUrl);

        assertThrows(InputInvalidException.class, payment::markFailed);
    }


}
