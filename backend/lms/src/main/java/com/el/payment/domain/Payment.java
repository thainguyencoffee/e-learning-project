package com.el.payment.domain;

import com.el.common.MoneyUtils;
import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table("payment")
public class Payment extends AbstractAggregateRoot<Payment> {
    @Id
    private UUID id;
    private UUID orderId;
    private MonetaryAmount amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private String receiptUrl;
    private String failureReason;

    public Payment(UUID orderId, MonetaryAmount amount, PaymentMethod paymentMethod) {
        if (orderId == null) throw new InputInvalidException("Order ID must not be null.");
        if (amount == null) throw new InputInvalidException("Amount must not be null.");
        if (paymentMethod == null) throw new InputInvalidException("Payment method must not be null.");

        MoneyUtils.checkValidPrice(amount);

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
        registerEvent(new PaymentPaid(this.id, this.orderId));
    }

    public void markFailed(String failureReason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new InputInvalidException("Payment cannot be marked as failed in current state.");
        }

        if (failureReason.isEmpty()) throw new InputInvalidException("Failure reason must not be empty.");

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public record PaymentPaid(UUID id, UUID orderId) {}

}
