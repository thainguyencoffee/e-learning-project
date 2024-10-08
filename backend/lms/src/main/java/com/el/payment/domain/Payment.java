package com.el.payment.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table("payment")
public class Payment {
    @Id
    private UUID id;
    private UUID orderId;
    private MonetaryAmount amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionId;

    public Payment(UUID orderId, MonetaryAmount amount, PaymentMethod paymentMethod) {
        Validate.notNull(orderId, "Order ID must not be null.");
        Validate.notNull(amount, "Amount must not be null.");
        Validate.notNull(paymentMethod, "Payment method must not be null.");

        if (amount.isNegativeOrZero()) {
            throw new InputInvalidException("Amount must be positive.");
        }

        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    public void markPaid(String transactionId) {
        Validate.notEmpty(transactionId, "Transaction ID must not be empty.");

        if (this.status != PaymentStatus.PENDING) {
            throw new InputInvalidException("Payment cannot be marked as paid in current state.");
        }

        this.paymentDate = LocalDateTime.now();
        this.transactionId = transactionId;
        this.status = PaymentStatus.PAID;
    }

    public void markFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be marked as failed in current state.");
        }
        this.status = PaymentStatus.FAILED;
    }


}
