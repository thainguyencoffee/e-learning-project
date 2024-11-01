package com.el.order.web;

import com.el.common.config.CustomAuthenticationEntryPoint;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.config.SecurityConfig;
import com.el.common.exception.InputInvalidException;
import com.el.order.application.dto.OrderItemDTO;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.application.impl.OrderServiceImpl;
import com.el.order.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class, CustomAuthenticationEntryPoint.class})
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderServiceImpl orderService;

    @Test
    void createOrder_ValidOrderRequest_CreatesOrderSuccessful() throws Exception {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), null);
        Order orderCreated = Mockito.mock(Order.class);
        var orderId = UUID.randomUUID();
        when(orderCreated.getId()).thenReturn(orderId);

        when(orderService.createOrder(any(), any(OrderRequestDTO.class))).thenReturn(orderCreated);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + orderCreated.getId()))
                .andExpect(jsonPath("$.id").value(orderCreated.getId().toString()));
    }

    @Test
    void createOrder_InvalidOrderRequest_ReturnsBadRequest() throws Exception {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(null)), null);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_OrderServiceThrowsException_ShouldThrows() throws Exception {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), null);

        when(orderService.createOrder(any(), any(OrderRequestDTO.class))).thenThrow(new InputInvalidException("Something went wrong"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), null);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isUnauthorized());
    }

}
