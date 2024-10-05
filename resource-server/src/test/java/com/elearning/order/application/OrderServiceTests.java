package com.elearning.order.application;

import com.elearning.common.Currencies;
import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.CourseQueryService;
import com.elearning.course.domain.Course;
import com.elearning.discount.application.DiscountService;
import com.elearning.order.application.dto.OrderItemDTO;
import com.elearning.order.application.dto.OrderRequestDTO;
import com.elearning.order.application.impl.OrderServiceImpl;
import com.elearning.order.domain.Order;
import com.elearning.order.domain.OrderRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.MonetaryAmount;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourseQueryService courseQueryService;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_ValidOrderRequest_CreatesOrderSuccessfully() {
        // Arrange
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "DISCOUNT10");

        Course course = Mockito.mock(Course.class);
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenReturn(course);
        // Just mock the behavior of the course object called in the createOrder method
        when(course.getFinalPrice()).thenReturn(Money.of(100, Currencies.VND));
        when(discountService.calculateDiscount(anyString(), any(MonetaryAmount.class)))
                .thenReturn(Money.of(10, Currencies.VND));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(orderRequestDTO);

        // Assert and Verify
        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals(Money.of(90, Currencies.VND), order.getDiscountedPrice());
        assertEquals("DISCOUNT10", order.getDiscountCode());

        verify(courseQueryService, times(1)).findPublishedCourseById(1L);
        verify(discountService, times(1)).calculateDiscount("DISCOUNT10", Money.of(100, Currencies.VND));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_ValidOrderRequestWithoutDiscount_CreatesOrderSuccessfully() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), null);
        Course course = Mockito.mock(Course.class);
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenReturn(course);
        // Just mock the behavior of the course object called in the createOrder method
        when(course.getFinalPrice()).thenReturn(Money.of(100, Currencies.VND));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(orderRequestDTO);

        // Assert and Verify
        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals(Money.of(100, Currencies.VND), order.getTotalPrice());
        assertNull(order.getDiscountCode());

        verify(courseQueryService, times(1)).findPublishedCourseById(1L);
        verify(discountService, never()).calculateDiscount(anyString(), any(MonetaryAmount.class));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_CourseNotFound_ThrowsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "DISCOUNT10");
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequestDTO));
    }

    @Test
    void createOrder_DiscountCodeInvalid_ThrowsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "INVALID_DISCOUNT");
        Course course = Mockito.mock(Course.class);
        when(course.getFinalPrice()).thenReturn(Money.of(100, Currencies.VND));
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenReturn(course);
        when(discountService.calculateDiscount(anyString(), any(MonetaryAmount.class)))
                .thenThrow(new InputInvalidException("Invalid discount code"));

        assertThrows(InputInvalidException.class, () -> orderService.createOrder(orderRequestDTO));
    }

}
