package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.CourseSectionDTO;
import com.el.course.domain.Course;
import com.el.order.application.dto.OrderItemDTO;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.payment.application.PaymentRequest;
import com.el.payment.domain.PaymentMethod;
import org.javamoney.moneta.Money;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class EnrolmentModuleTests extends AbstractLmsApplicationTests {

    private Long performCreateEnrollment(KeycloakToken actor) {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO));
        var courseId = course.getId();

        approvePublishByCourseId(course.getId());

        webTestClient.get()
                .uri("/published-courses/{courseId}", courseId)
                .exchange()
                .expectStatus().isOk();

        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);

        // Gửi request POST để tạo order
        String location = webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(actor.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult().getResponseHeaders().getLocation().toString();

        String orderId = location.substring(location.lastIndexOf("/") + 1);

        // Act: Pay order
        var paymentRequestDto = new PaymentRequest(
                UUID.fromString(orderId),
                Money.of(50000, Currencies.VND),
                PaymentMethod.STRIPE, "tok_visa");
        webTestClient.post().uri("/payments")
                .headers(header -> header.setBearerAuth(actor.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentRequestDto))
                .exchange()
                .expectStatus().isCreated();
        return courseId;
    }

}
