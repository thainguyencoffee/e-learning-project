package com.el.payment.application;

import com.el.common.Currencies;
import com.el.order.application.OrderService;
import com.el.payment.domain.Payment;
import com.el.payment.domain.PaymentMethod;
import com.el.payment.domain.PaymentRepository;
import com.el.payment.domain.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripePaymentGateway stripePaymentGateway;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private Charge charge;

    @Test
    void pay_ValidPaymentRequest_CreatesAndProcessesPayment() throws StripeException {
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), Money.of(100L, Currencies.VND),
                PaymentMethod.STRIPE, UUID.randomUUID().toString());

        when(charge.getId()).thenReturn("ch_1J2Y3Z4A5B6C7D8E9F0G");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stripePaymentGateway.charge(any(PaymentRequest.class))).thenReturn(charge);

        Payment result = paymentService.pay(paymentRequest);

        // Assert and Verify
        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getStatus());
        assertEquals("ch_1J2Y3Z4A5B6C7D8E9F0G", result.getTransactionId());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderService, times(1)).paymentSucceeded(paymentRequest.orderId());
    }


    @Test
    void testPay_withStripePaymentFailure_shouldMarkPaymentFailed() throws StripeException {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), Money.of(100L, Currencies.VND), PaymentMethod.STRIPE, UUID.randomUUID().toString());

        // Giả lập Stripe ném ngoại lệ
        when(stripePaymentGateway.charge(paymentRequest)).thenThrow(new RuntimeException("Stripe payment failed"));

        // Giả lập lưu trạng thái failed
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.pay(paymentRequest);
        });

        // Xác nhận rằng payment đã được đánh dấu là failed và lưu vào repository
        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
        assertEquals("Stripe payment failed", exception.getCause().getMessage());
    }

    @Test
    void testPay_WithMethodUnsupported_shouldMarkPaymentFailed() throws StripeException {
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), Money.of(100L, Currencies.VND),
                PaymentMethod.PAYPAL, UUID.randomUUID().toString());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.pay(paymentRequest));

        verify(paymentRepository, times(1)).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
        verify(stripePaymentGateway, never()).charge(any(PaymentRequest.class));
        assertEquals("Unsupported payment method: " + PaymentMethod.PAYPAL.name(), exception.getMessage());
    }

}
