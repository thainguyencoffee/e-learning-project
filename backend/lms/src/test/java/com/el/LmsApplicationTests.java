package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseSectionDTO;
import com.el.course.application.dto.LessonDTO;
import com.el.course.domain.*;
import com.el.course.web.CourseRequestApproveDTO;
import com.el.course.web.dto.AssignTeacherDTO;
import com.el.course.web.dto.UpdatePriceDTO;
import com.el.course.web.dto.UpdateSectionDTO;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.domain.Discount;
import com.el.discount.domain.DiscountRepository;
import com.el.discount.domain.Type;
import com.el.order.application.dto.OrderItemDTO;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.domain.Status;
import com.el.payment.application.PaymentRequest;
import com.el.payment.domain.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LmsApplicationTests {

    protected static KeycloakToken userToken;
    protected static KeycloakToken user2Token;
    protected static KeycloakToken teacherToken;
    protected static KeycloakToken bossToken;

    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("keycloak101-realm.json")
                    .withEnv("KEYCLOAK_ADMIN", "admin")
                    .withEnv("KEYCLOAK_ADMIN_PASSWORD", "secret");

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private DiscountRepository discountRepository;

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/keycloak101");
        registry.add("reverse-proxy-uri", keycloak::getAuthServerUrl);
        registry.add("authorization-server-prefix", () -> "");
    }

    @BeforeAll
    static void generateAccessToken() {
        keycloak.start(); // Fix: Mapped port can only be obtained after the container is started

        WebClient webClient = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl() + "/realms/keycloak101/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        userToken = authenticateWith("user", "1", webClient);
        user2Token = authenticateWith("user2", "1", webClient);
        teacherToken = authenticateWith("teacher", "1", webClient);
        bossToken = authenticateWith("boss", "1", webClient);
    }

    @BeforeEach
    void setupData() {
        courseRepository.deleteAll();

        // prepare data for discount record
        discountRepository.deleteAll();
    }


    @Test
    void contextLoads() {
        assertThat(userToken.accessToken).isNotNull();
        assertThat(teacherToken.accessToken).isNotNull();
        assertThat(bossToken.accessToken).isNotNull();
    }

    @Test
    void givenRequestIsAnonymous_whenGetLiveness_thenOk() throws Exception {
        webTestClient.get().uri("/actuator/health/liveness")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void givenRequestIsAnonymous_whenGetReadiness_thenOk() throws Exception {
        webTestClient.get().uri("/actuator/health/readiness")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void testMeWhenUnauthenticated() {
        webTestClient.get().uri("/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username")
                .isEqualTo("")
                .jsonPath("$.email")
                .isEqualTo("")
                .jsonPath("$.roles")
                .isEmpty()
                .jsonPath("$.exp")
                .doesNotExist();
    }

    @Test
    void testMeWhenAuthenticated() {
        webTestClient.get().uri("/me")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username")
                .isEqualTo(extractClaimFromToken(userToken.getAccessToken(), "preferred_username"))
                .jsonPath("$.email")
                .isEqualTo(extractClaimFromToken(userToken.getAccessToken(), "email"))
                .jsonPath("$.roles")
                .isArray();
    }

    @Test
    void testCreateCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();

        // Extract the "preferred_username" claim from the teacherToken (which represents the teacher's user ID)
        String teacher = extractClaimFromToken(teacherToken.getAccessToken(), "preferred_username");

        // Gửi request POST với token của "teacher"
        webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> assertThat(location).contains("/courses/"))
                .expectBody()
                .jsonPath("$.title").isEqualTo(courseDTO.title())
                .jsonPath("$.teacher").isEqualTo(teacher);
    }

    @Test
    void testCreateCourse_Unauthorized() {
        var courseDTO = TestFactory.createDefaultCourseDTO();

        // Gửi request POST mà không có token
        webTestClient.post().uri("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))  // Body của request là JSON CourseDTO
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testCreateCourse_Forbidden() {
        // Thiết lập dữ liệu CourseDTO
        var courseDTO = TestFactory.createDefaultCourseDTO();

        // Gửi request POST với token  không có quyền "teacher"
        webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Token của "user" không có quyền "teacher"
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))  // Body của request là JSON CourseDTO
                .exchange()
                .expectStatus().isForbidden();  // Kiểm tra phản hồi 403 Forbidden
    }

    @Test
    void testCreateCourse_BadRequest_EmptyTitle() {
        // Thiết lập dữ liệu CourseDTO với tiêu đề trống
        var invalidCourseDTO = TestFactory.createCourseDTOBlankTitle();

        // Gửi request POST với dữ liệu không hợp lệ
        webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Token hợp lệ của "teacher"
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidCourseDTO))  // Body của request là JSON CourseDTO
                .exchange()
                .expectStatus().isBadRequest()  // Kiểm tra phản hồi 400 Bad Request
                .expectBody();  // Kiểm tra thông báo lỗi từ API
    }

    @Test
    void testCreateCourse_PayloadTooLarge() {
        var largeCourseDTO = TestFactory.createCourseDTOTooLargeString();

        // Gửi request POST với payload quá lớn
        webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(largeCourseDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateInfoCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        var courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();

        webTestClient.put().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(courseUpdateDTO.title())
                .jsonPath("$.description").isEqualTo(courseUpdateDTO.description())
                .jsonPath("$.thumbnailUrl").isEqualTo(courseUpdateDTO.thumbnailUrl());
    }

    private Course createCourseWithParameters(KeycloakToken token, CourseDTO courseDTO, boolean hasPrice, Set<CourseSectionDTO> sectionDTOs) {
        String courseId = webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(token.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> {
                    assertThat(location).contains("/courses/");
                })
                .returnResult(String.class)
                .getResponseHeaders()
                .getLocation()
                .getPath()
                .split("/")[2];

        if (hasPrice) {
            webTestClient.put().uri("/courses/{courseId}/update-price", courseId)
                    .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new UpdatePriceDTO(Money.of(1000, Currencies.VND))))
                    .exchange()
                    .expectStatus().isOk();
        }

        if (sectionDTOs != null && !sectionDTOs.isEmpty()) {
            for (CourseSectionDTO sectionDTO : sectionDTOs) {
                webTestClient.post().uri("/courses/{courseId}/sections", courseId)
                        .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(sectionDTO))
                        .exchange()
                        .expectStatus().isCreated();
            }
        }
        return courseRepository.findById(Long.valueOf(courseId)).orElseThrow();
    }

    @Test
    void testUpdateInfoCourse_Unauthorized() {
        var courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();

        // Gửi request PUT mà không có token
        webTestClient.put().uri("/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isUnauthorized();  // Kiểm tra phản hồi 401 Unauthorized
    }

    @Test
    void testUpdateInfoCourse_Forbidden() {
        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();

        // Gửi request PUT với token  không có quyền "teacher"
        webTestClient.put().uri("/courses/1")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Token của "user" không có quyền "teacher"
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isForbidden();  // Kiểm tra phản hồi 403 Forbidden
    }

    @Test
    void testUpdateInfoCourse_NotFound() {
        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = TestFactory.createDefaultCourseUpdateDTO();

        // Gửi request PUT với token của "teacher" để cập nhật khóa học không tồn tại
        webTestClient.put().uri("/courses/999")
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Token của "teacher"
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isNotFound();  // Kiểm tra phản hồi 404 Not Found
    }

    @Test
    void testUpdateInfoCourse_BadRequest_EmptyTitle() {
        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = TestFactory.createCourseUpdateDTOBlankTitle();

        // Gửi request PUT với token của "teacher" để cập nhật khóa học
        webTestClient.put().uri("/courses/{courseId}", 111)  // Sử dụng Location từ header để update đúng khóa học
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isBadRequest();  // Kiểm tra phản hồi 400 Bad Request
    }

    @Test
    void testDeleteCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();
    }

    @Test
    void testDeleteCourse_WithAdmin_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted
    }

    @Test
    void testDeleteCourse_AlreadyDeleted() {
        // Lấy khóa học và đánh dấu là đã xóa
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Gửi request DELETE lần nữa
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteCourse_Unauthorized() {
        // Gửi request DELETE mà không đính kèm token
        webTestClient.delete().uri("/courses/{courseId}", 1000L) // dont need to be a valid course id
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testDeleteForceCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        // Bắt buộc xóa mềm trước
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken())) 
                .exchange()
                .expectStatus().isNoContent();

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted


        // act
        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}?force=true", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Kiểm tra rằng khóa học đã bị xóa hoàn toàn
        assertThat(courseRepository.findById(course.getId())).isEmpty();  // Kiểm tra khóa học đã bị xóa hoàn toàn
    }

    @Test
    void testUpdatePrice_Successfully() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Gửi request PUT để cập nhật giá khóa học
        webTestClient.put().uri("/courses/{courseId}/update-price", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newPrice))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").isEqualTo("VND1,000.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testUpdatePrice_Forbidden() {
        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Gửi request PUT để cập nhật giá khóa học
        webTestClient.put().uri("/courses/{courseId}/update-price", 1)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newPrice))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testAssignTeacher_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teacher").isEqualTo("new-teacher-id");  // Kiểm tra giáo viên đã được cập nhật
    }

    @Test
    void testAssignTeacher_Forbidden() {
        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", 1)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testAssignTeacher_NotFound() {
        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học không tồn tại
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", 999)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    void testRequestPublishCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request POST để yêu cầu xuất bản khóa học
        webTestClient.post().uri("/courses/{courseId}/requests", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOPublish()))
                .exchange()
                .expectStatus().isOk();

        // assert
        course = courseRepository.findById(course.getId()).get();
        assertThat(course.getCourseRequests().size()).isEqualTo(1);
        assertThat(course.getCourseRequests().iterator().next().getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void testApproveRequest_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request POST để yêu cầu xuất bản khóa học
        webTestClient.post().uri("/courses/{courseId}/requests", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOPublish()))
                .exchange()
                .expectStatus().isOk();

        var requestId = courseRepository.findById(course.getId()).get().getCourseRequests().iterator().next().getId();

        CourseRequestApproveDTO approveDTO = TestFactory.createDefaultCourseRequestApproveDTOPublish();
        // Gửi request PUT để duyệt yêu cầu xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/approve", course.getId(), requestId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .body(BodyInserters.fromValue(approveDTO))
                .exchange()
                .expectStatus().isOk();

        // assert
        course = courseRepository.findById(course.getId()).get();
        assertThat(course.getCourseRequests().size()).isEqualTo(1);
        assertThat(course.getCourseRequests().iterator().next().getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(course.isPublishedAndNotDeleted()).isTrue();
    }

    @Test
    void testApproveRequest_Forbidden() {
        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/approve", 999L,  999L)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestApproveDTOPublish()))
                .exchange()
                .expectStatus().isForbidden();
    }


    private void approvePublishByCourseId(Long courseId) {
        webTestClient.post().uri("/courses/{courseId}/requests", courseId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOPublish()))
                .exchange()
                .expectStatus().isOk();

        var requestId = courseRepository.findById(courseId).get().getCourseRequests().iterator().next().getId();

        CourseRequestApproveDTO approveDTO = TestFactory.createDefaultCourseRequestApproveDTOPublish();

        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/approve", courseId, requestId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .body(BodyInserters.fromValue(approveDTO))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testRequestUnpublishCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        // Gửi request POST để yêu cầu hủy xuất bản khóa học
        webTestClient.post().uri("/courses/{courseId}/requests", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOUnPublish()))
                .exchange()
                .expectStatus().isOk();

        // assert
        course = courseRepository.findById(course.getId()).get();
        assertThat(course.getCourseRequests().size()).isEqualTo(2);
        assertThat(course.getCourseRequests().stream().anyMatch(request -> request.getStatus() == RequestStatus.PENDING)).isTrue();
    }

    @Test
    void testApproveUnpublishCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        // Gửi request POST để yêu cầu hủy xuất bản khóa học
        webTestClient.post().uri("/courses/{courseId}/requests", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOUnPublish()))
                .exchange()
                .expectStatus().isOk();

        var requestId = courseRepository.findById(course.getId()).get().getCourseRequests().stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING).findFirst().get().getId(); // damn this is ugly


        CourseRequestApproveDTO approveDTOUnPublish = TestFactory.createDefaultCourseRequestApproveDTOUnPublish();

        // Gửi request PUT để duyệt yêu cầu hủy xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/approve", course.getId(), requestId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .body(BodyInserters.fromValue(approveDTOUnPublish))
                .exchange()
                .expectStatus().isOk();

        // assert
        course = courseRepository.findById(course.getId()).get();
        assertThat(course.getCourseRequests().size()).isEqualTo(2);
        assertThat(course.getCourseRequests().stream().anyMatch(request -> request.getStatus() == RequestStatus.APPROVED)).isTrue();
        assertThat(course.isNotPublishedAndDeleted()).isTrue();
        assertThat(course.getUnpublished()).isTrue();
        assertThat(course.isPublishedAndNotDeleted()).isFalse();
    }

    @Test
    void testRejectPublishCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request POST để yêu cầu xuất bản khóa học
        webTestClient.post().uri("/courses/{courseId}/requests", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestDTOPublish()))
                .exchange()
                .expectStatus().isOk();

        var requestId = courseRepository.findById(course.getId()).get().getCourseRequests().iterator().next().getId();

        // Gửi request PUT để từ chối yêu cầu xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/reject", course.getId(), requestId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestRejectDTOPublish()))
                .exchange()
                .expectStatus().isOk();

        // assert
        course = courseRepository.findById(course.getId()).get();
        assertThat(course.getCourseRequests().size()).isEqualTo(1);
        assertThat(course.getCourseRequests().stream().anyMatch(request -> request.getStatus() == RequestStatus.REJECTED)).isTrue();
        assertThat(course.isPublishedAndNotDeleted()).isFalse();
    }


    @Test
    void testAddSectionToCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sections.length()").isEqualTo(1);
    }

    @Test
    void testAddSectionToCourse_Unauthorized() {
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testAddSectionToCourse_Forbidden() {
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void testUpdateSectionInfo_UserIsTeacher_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].title").isEqualTo(updateSectionDTO.title());
    }

    @Test
    void testUpdateSectionInfo_UserIsAdmin_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].title").isEqualTo(updateSectionDTO.title());
    }

    @Test
    void testUpdateSectionInfo_UserIsNotTeacherOrAdmin_Forbidden() {
        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", 1L, 1L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateSectionInfo_CoursePublished_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRemoveSection_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request DELETE để xóa section khỏi khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testRemoveSection_CoursePublished_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        // Gửi request DELETE để xóa section khỏi khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddLesson_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(1);
    }

    @Test
    void testAddLesson_LessonDuplicate_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk();

        LessonDTO lessonDTODup = new LessonDTO("Lesson 1", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTODup))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddLesson_CoursePublished_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var courseSection = course.getSections().iterator().next();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), courseSection.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddLesson_UserIsNotTeacherOrAdmin_Forbidden() {
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", 999L, 111L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateLesson_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher


        // Gửi request POST để thêm lesson cho section của khóa học
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        var sectionId = course.getSections().iterator().next().getId();
        AtomicReference<Integer> lessonId = new AtomicReference<>();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(1)
                .jsonPath("$.sections[0].lessons[0].id").value(lessonId::set);

        LessonDTO updateLessonDTO = new LessonDTO("New title", Lesson.Type.TEXT, "http://example.com/lesson1.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var section = course.getSections().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId.get())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons[0].title").isEqualTo(updateLessonDTO.title());
    }

    @Test
    void testUpdateLesson_LessonDuplicate_OKBecauseIsItself() {
        // first, add a lesson
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        AtomicReference<Integer> lessonId = new AtomicReference<>();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(1)
                .jsonPath("$.sections[0].lessons[0].id").value(lessonId::set);

        //act
        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var section = course.getSections().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId.get())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateLesson_CoursePublished_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var courseSection = course.getSections().iterator().next();
        var lessonId = 1234567L; // not need lessonId because course is published then throws exception

        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), courseSection.getId(), lessonId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateLesson_UserIsNotTeacherOrAdmin_Forbidden() {
        LessonDTO updateLessonDTO = new LessonDTO("New title", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", 1000L, 2000L, 3000L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testRemoveLesson_Successful() {
        // Arrange: Create course and add lesson
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Arrange: Add lesson
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        var sectionId = course.getSections().iterator().next().getId();
        AtomicReference<Integer> lessonId = new AtomicReference<>();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(1)
                .jsonPath("$.sections[0].lessons[0].id").value(lessonId::set);

        // Act: Delete lesson
        var section = course.getSections().iterator().next();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId.get())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testRemoveLesson_CoursePublished_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        approvePublishByCourseId(course.getId());

        // Act: Delete lesson
        var courseSection = course.getSections().iterator().next();
        var lessonId = 1234567L; // not need lessonId because course is published then throws exception
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), courseSection.getId(), lessonId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRemoveLesson_UserIsNotTeacherOrAdmin_Forbidden() {
        // Act: Delete lesson
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", 100L, 200L, 300L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

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

        webTestClient.get().uri("/orders/my-orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        // verify if another user can't access this order
        webTestClient.get().uri("/orders/my-orders/{orderId}", orderId)
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

        // Chuẩn bị dữ liệu cho request tạo order
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);

        // Gửi request POST để tạo order
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

        // Chuẩn bị dữ liệu cho request tạo order
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
                .expectStatus().isCreated();

        // verify order as paid
        webTestClient.get().uri("/orders/my-orders/{orderId}", orderId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(Status.PAID.name());
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
        webTestClient.get().uri("/discounts/code/{code}", code)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentUsage").isEqualTo(1)
                .jsonPath("$.maxUsage").isEqualTo(discountDTO.maxUsage());
    }


    @Test
    void testGetCountUsers_IsAdmin_Successful() {
        webTestClient.get().uri("/users/count")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class).isEqualTo(4);
    }

    @Test
    void testGetCountUsersWithCriteria_IsAdmin_Successful() {
        webTestClient.get().uri("/users/count?lastName=nguyen")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class).isEqualTo(3);
    }

    @Test
    void testGetCountUsersWithSearch_IsAdmin_Successful() {
        // I want to search by username
        // 1. Exact search then use "foo"
        // 2. Prefix-based then use foo*
        // 3. Infix search then just use foo
        webTestClient.get().uri("/users/count/search?search=\"use\"") // search exact "use"
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class).isEqualTo(0);

        webTestClient.get().uri("/users/count/search?search=use*") // search prefix "use"
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class).isEqualTo(2);
    }

    @Test
    void testSearchUsersWithUsername_IsAdmin_Successful() {
        webTestClient.get().uri("/users/search?username=teach")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo("9db2e8a4-ca81-43ca-bfb4-bef6fa9e0844")
                .jsonPath("$[0].firstName").isEqualTo("teacher")
                .jsonPath("$[0].lastName").isEqualTo("nguyen")
                .jsonPath("$[0].username").isEqualTo("teacher")
                .jsonPath("$[0].email").isEqualTo("nguyennt110320042@gmail.com");

        webTestClient.get().uri("/users/search?username=each*")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);

        webTestClient.get().uri("/users/search?username=teacher")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo("9db2e8a4-ca81-43ca-bfb4-bef6fa9e0844")
                .jsonPath("$[0].firstName").isEqualTo("teacher")
                .jsonPath("$[0].lastName").isEqualTo("nguyen")
                .jsonPath("$[0].username").isEqualTo("teacher")
                .jsonPath("$[0].email").isEqualTo("nguyennt110320042@gmail.com");
    }

    // Discount bounded context

    private String performCreateDiscountTest(DiscountDTO discountDTO) {
        return webTestClient.post().uri("/discounts")
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(discountDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.code").isEqualTo(discountDTO.code())
                .jsonPath("$.percentage").isEqualTo(discountDTO.percentage())
                .jsonPath("$.maxUsage").isEqualTo(discountDTO.maxUsage())
                .jsonPath("$.startDate").isNotEmpty()
                .jsonPath("$.endDate").isNotEmpty()
                .jsonPath("$.type").isEqualTo(discountDTO.type().name())
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.lastModifiedBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.lastModifiedDate").isNotEmpty()
                .returnResult().getResponseHeaders().getLocation().getPath().split("/")[2];
    }

    @Test
    void testCreateDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();
        performCreateDiscountTest(discountDTO);
    }

    @Test
    void testUpdateDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        DiscountDTO updateDiscountDTO = new DiscountDTO("DISCOUNT_30",
                Type.PERCENTAGE,
                30.0,
                null,
                LocalDateTime.now().minusSeconds(360),
                LocalDateTime.now().plusSeconds(360),
                100);

        webTestClient.put().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateDiscountDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(discountId)
                .jsonPath("$.code").isEqualTo(updateDiscountDTO.code())
                .jsonPath("$.percentage").isEqualTo(updateDiscountDTO.percentage())
                .jsonPath("$.maxUsage").isEqualTo(updateDiscountDTO.maxUsage())
                .jsonPath("$.startDate").isNotEmpty()
                .jsonPath("$.endDate").isNotEmpty()
                .jsonPath("$.type").isEqualTo(updateDiscountDTO.type().name())
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.lastModifiedBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.lastModifiedDate").isNotEmpty();
    }

    @Test
    void testDeleteDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();
        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();
    }

    @Test
    void testRestoreDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();

        webTestClient.post().uri("/discounts/{id}/restore", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isNotEmpty();
    }

    @Test
    void testDeleteForceDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();

        webTestClient.delete().uri("/discounts/{id}?force=true", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findById(Long.valueOf(discountId))).isEmpty();
    }


    protected static class KeycloakToken {
        private final String accessToken;

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    private static KeycloakToken authenticateWith(
            String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "bff-client")
                        .with("client_secret", "secret")
                        .with("username", username)
                        .with("password", password)
                )
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    private String extractClaimFromToken(String token, String claimName) {
        // Sử dụng thư viện JWT hoặc Base64 để giải nén thông tin từ token
        // Bạn có thể dùng các thư viện như Nimbus JWT hoặc Java JWT (jjwt)
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> claims = mapper.readValue(payload, Map.class);
            return claims.get(claimName).toString();  // Trả về giá trị của claim "preferred_username"
        } catch (IOException e) {
            throw new RuntimeException("Error decoding JWT", e);
        }
    }

    private List<String> extractClaimToListFromToken(String token, String claimName) {
        // Sử dụng thư viện JWT hoặc Base64 để giải nén thông tin từ token
        // Bạn có thể dùng các thư viện như Nimbus JWT hoặc Java JWT (jjwt)
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> claims = mapper.readValue(payload, Map.class);
            return (List<String>) claims.get(claimName);  // Trả về giá trị của claim "preferred_username"
        } catch (IOException e) {
            throw new RuntimeException("Error decoding JWT", e);
        }
    }
}
