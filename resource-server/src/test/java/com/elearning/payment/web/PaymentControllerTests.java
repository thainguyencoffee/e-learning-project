package com.elearning.payment.web;

import com.elearning.common.Currencies;
import com.elearning.common.config.jackson.JacksonCustomizations;
import com.elearning.common.config.SecurityConfig;
import com.elearning.payment.application.PaymentRequest;
import com.elearning.payment.application.PaymentService;
import com.elearning.payment.domain.Payment;
import com.elearning.payment.domain.PaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
class PaymentControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void payment_ValidPaymentRequest_ReturnsCreatedStatus() throws Exception {
        var token = "token";
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), Money.of(100L, Currencies.VND), PaymentMethod.STRIPE, token);
        Payment payment = Mockito.mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());

        when(paymentService.pay(any(PaymentRequest.class))).thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/payments/" + payment.getId()))
                .andExpect(jsonPath("$.id").value(payment.getId().toString()));
    }

    @Test
    void payment_InvalidPaymentRequest_ReturnsBadRequest() throws Exception {
        var token = "token";
        PaymentRequest paymentRequest = new PaymentRequest(null, Money.of(100L, Currencies.VND), PaymentMethod.STRIPE, token);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payment_PaymentServiceThrowsException_ReturnsInternalServerError() throws Exception {
        var token = "token";
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), Money.of(100L, Currencies.VND), PaymentMethod.STRIPE, token);

        when(paymentService.pay(any(PaymentRequest.class))).thenThrow(new RuntimeException("Internal error"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void payment_Unauthorized_ReturnsUnauthorized() throws Exception {
        var token = "token";
        PaymentRequest paymentRequest = new PaymentRequest(null, Money.of(100L, Currencies.VND), PaymentMethod.STRIPE, token);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isUnauthorized());
    }
    
}
