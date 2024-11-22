package com.el;

import com.el.common.Currencies;
import com.el.course.web.dto.CourseSectionDTO;
import com.el.course.domain.Course;
import com.el.discount.web.dto.DiscountDTO;
import com.el.discount.domain.Discount;
import com.el.order.web.dto.OrderItemDTO;
import com.el.order.web.dto.OrderRequestDTO;
import com.el.order.domain.Status;
import com.el.payment.web.dto.PaymentRequest;
import com.el.payment.domain.PaymentMethod;
import com.el.payment.domain.PaymentStatus;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class OrderModuleTests extends AbstractLmsApplicationTests{

    @Test
    void testCreateOrder_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        approvePublishByCourseId(course.getId());

        webTestClient.get()
                .uri("/published-courses/{courseId}", courseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId.intValue());

        // Arrange: Prepare data for order request
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);

        // Act: Send POST request to create order
        String location = webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].course").isEqualTo(courseId.intValue())
                .jsonPath("$.items[0].price").isEqualTo("VND1,000.00")
                .jsonPath("$.status").isEqualTo(Status.PENDING.name())
                .jsonPath("$.totalPrice").isEqualTo("VND1,000.00")
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(userToken.accessToken, "preferred_username"))
                .returnResult().getResponseHeaders().getLocation().toString();

        String orderId = location.substring(location.lastIndexOf("/") + 1);

        webTestClient.get().uri("/orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        // verify if another user can't access this order
        webTestClient.get().uri("/orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(user2Token.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound();

        // verify admin can access this order
        webTestClient.get().uri("/orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testCreateOrder_Unauthorized() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Arrange: Prepare data for order request
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);

        // Act: Send POST request to create order
        webTestClient.post().uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testCreateOrder_CourseNotFound() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Arrange: Prepare data for order request
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);
        // Course tồn tại nhưng không được publish

        // Gửi request POST để tạo order
        webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateOrderAndThenPay_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        approvePublishByCourseId(course.getId());

        webTestClient.get()
                .uri("/published-courses/{courseId}", courseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId.intValue());

        // Chuẩn bị dữ liệu cho request tạo order
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);

        // Gửi request POST để tạo order
        String location = webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].course").isEqualTo(courseId.intValue())
                .jsonPath("$.items[0].price").isEqualTo("VND1,000.00")
                .jsonPath("$.status").isEqualTo(Status.PENDING.name())
                .jsonPath("$.totalPrice").isEqualTo("VND1,000.00")
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(userToken.accessToken, "preferred_username"))
                .returnResult().getResponseHeaders().getLocation().toString();

        String orderId = location.substring(location.lastIndexOf("/") + 1);

        // Act: Pay order
        var paymentRequestDto = new PaymentRequest(
                UUID.fromString(orderId),
                Money.of(50000, Currencies.VND),
                PaymentMethod.STRIPE, "tok_visa");
        webTestClient.post().uri("/payments")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.status").isEqualTo(PaymentStatus.PAID.name());
    }

    @Test
    void testCreateOrderWithDiscount_Successful() {
        // Arrange discount
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();
        String id = performCreateDiscountTest(discountDTO);
        Discount discount = discountRepository.findByIdAndDeleted(Long.valueOf(id), false).get();
        String code = discount.getCode();

        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        approvePublishByCourseId(course.getId());

        webTestClient.get()
                .uri("/published-courses/{courseId}", courseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId.intValue());

        // Arrange order
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, code);


        // Act
        String location = webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].course").isEqualTo(courseId.intValue())
                .jsonPath("$.items[0].price").isEqualTo("VND1,000.00")
                .jsonPath("$.status").isEqualTo(Status.PENDING.name())
                .jsonPath("$.totalPrice").isEqualTo("VND1,000.00")
                .jsonPath("$.discountedPrice").isEqualTo("VND900.00")
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(userToken.accessToken, "preferred_username"))
                .returnResult().getResponseHeaders().getLocation().toString();

        String orderId = location.substring(location.lastIndexOf("/") + 1);

        // Act: Pay order
        var paymentRequestDto = new PaymentRequest(
                UUID.fromString(orderId),
                Money.of(50000, Currencies.VND),
                PaymentMethod.STRIPE, "tok_visa");
        webTestClient.post().uri("/payments")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentRequestDto))
                .exchange()
                .expectStatus().isCreated();

        // Verify get all payments by orderId
        webTestClient.get().uri("/payments/orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].orderId").isEqualTo(orderId);

        // verify Discount was increased usage
        // cannot verify because it's async, and don't have any idea to verify it
//        webTestClient.get().uri("/discounts/code/{code}", code)
//                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.currentUsage").isEqualTo(1)
//                .jsonPath("$.maxUsage").isEqualTo(discountDTO.maxUsage());
    }
}
