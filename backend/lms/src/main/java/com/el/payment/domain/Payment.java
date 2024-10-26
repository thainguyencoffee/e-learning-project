package com.el.payment.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
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
    private String receiptUrl;

    public Payment(UUID orderId, MonetaryAmount amount, PaymentMethod paymentMethod) {
        if (orderId == null) throw new InputInvalidException("Order ID must not be null.");
        if (amount == null) throw new InputInvalidException("Amount must not be null.");
        if (paymentMethod == null) throw new InputInvalidException("Payment method must not be null.");

        if (amount.isNegativeOrZero()) {
            throw new InputInvalidException("Amount must be positive.");
        }

        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    public void markPaid(String transactionId, String receiptUrl) {
        if (transactionId.isEmpty()) throw new InputInvalidException("Transaction ID must not be empty.");
        if (receiptUrl.isEmpty()) throw new InputInvalidException("Receipt URL must not be empty.");

        if (this.status != PaymentStatus.PENDING) {
            throw new InputInvalidException("Payment cannot be marked as paid in current state.");
        }

        this.paymentDate = LocalDateTime.now();
        this.transactionId = transactionId;
        this.receiptUrl = receiptUrl;
        this.status = PaymentStatus.PAID;
    }

    public void markFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InputInvalidException("Payment cannot be marked as failed in current state.");
        }
        this.status = PaymentStatus.FAILED;
    }


}
