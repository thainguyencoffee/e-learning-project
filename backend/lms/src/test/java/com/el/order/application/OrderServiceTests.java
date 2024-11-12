package com.el.order.application;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.discount.application.DiscountService;
import com.el.order.application.dto.OrderItemDTO;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.application.impl.OrderServiceImpl;
import com.el.order.domain.Order;
import com.el.order.domain.OrderRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.MonetaryAmount;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Mock
    private RolesBaseUtil rolesBaseUtil;

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
        when(course.getPrice()).thenReturn(Money.of(100, Currencies.VND));
        when(discountService.calculateDiscount(anyString(), any(MonetaryAmount.class)))
                .thenReturn(Money.of(10, Currencies.VND));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(TestFactory.user, orderRequestDTO);

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
        // Mock role
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenReturn(course);
        // Just mock the behavior of the course object called in the createOrder method
        when(course.getPrice()).thenReturn(Money.of(100, Currencies.VND));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(TestFactory.user, orderRequestDTO);

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
    void createOrder_RoleAdminOrTeacher_ThrowsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "DISCOUNT10");
        // Mock role
        when(rolesBaseUtil.isAdmin()).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> orderService.createOrder(TestFactory.user, orderRequestDTO));
    }

    @Test
    void createOrder_CourseNotFound_ThrowsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "DISCOUNT10");
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenThrow(new ResourceNotFoundException());
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TestFactory.user, orderRequestDTO));
    }

    @Test
    void createOrder_DiscountCodeInvalid_ThrowsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                Set.of(new OrderItemDTO(1L)), "INVALID_DISCOUNT");
        Course course = Mockito.mock(Course.class);
        when(course.getPrice()).thenReturn(Money.of(100, Currencies.VND));
        // Mock role
        when(courseQueryService.findPublishedCourseById(any(Long.class))).thenReturn(course);
        when(discountService.calculateDiscount(anyString(), any(MonetaryAmount.class)))
                .thenThrow(new InputInvalidException("Invalid discount code"));

        assertThrows(InputInvalidException.class, () -> orderService.createOrder(TestFactory.user, orderRequestDTO));
    }

    @Test
    void paymentSucceeded_ShouldMarkOrderAsPaid_WhenOrderExists() {
        UUID orderId = UUID.randomUUID();
        Order order = Mockito.mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.paymentSucceeded(orderId);

        verify(order, times(1)).makePaid();
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void paymentSucceeded_ShouldIncreaseDiscountUsage_WhenOrderHasDiscountCode() {
        UUID orderId = UUID.randomUUID();
        Order order = Mockito.mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getDiscountCode()).thenReturn("DISCOUNT10");

        orderService.paymentSucceeded(orderId);

        verify(order, times(1)).makePaid();
        verify(discountService, times(1)).increaseUsage("DISCOUNT10");
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void paymentSucceeded_ShouldNotIncreaseDiscountUsage_WhenOrderHasNoDiscountCode() {
        UUID orderId = UUID.randomUUID();
        Order order = Mockito.mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getDiscountCode()).thenReturn("");

        orderService.paymentSucceeded(orderId);

        verify(order, times(1)).makePaid();
        verify(discountService, never()).increaseUsage(anyString());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void paymentSucceeded_ShouldThrowException_WhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.paymentSucceeded(orderId));

        verify(orderRepository, never()).save(any(Order.class));
    }

}
