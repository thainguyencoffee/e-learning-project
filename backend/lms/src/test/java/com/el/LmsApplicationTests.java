package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseSectionDTO;
import com.el.course.application.dto.CourseUpdateDTO;
import com.el.course.application.dto.LessonDTO;
import com.el.course.domain.*;
import com.el.course.web.dto.ApplyDiscountDTO;
import com.el.course.web.dto.AssignTeacherDTO;
import com.el.course.web.dto.UpdatePriceDTO;
import com.el.course.web.dto.UpdateSectionDTO;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LmsApplicationTests {

    protected static KeycloakToken userToken;
    protected static KeycloakToken teacherToken;
    protected static KeycloakToken bossToken;

    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("keycloak101-realm.json");

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
        teacherToken = authenticateWith("teacher", "1", webClient);
        bossToken = authenticateWith("boss", "1", webClient);
    }

    @BeforeEach
    void setupData() {
        courseRepository.deleteAll();  // Xóa dữ liệu test trước đó

        // prepare data for discount record
        discountRepository.deleteAll();
        Discount discount = new Discount(
                "DISCOUNT_50",
                Type.PERCENTAGE,
                50.0,
                null,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600)
        );
        discountRepository.save(discount);

        Discount discount2 = new Discount(
                "DISCOUNT_30_VN",
                Type.FIXED,
                null,
                Money.of(30, Currencies.VND),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600)
        );
        discountRepository.save(discount2);
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
                .isEqualTo(extractClaimFromToken(userToken.getAccessToken(), "sub"))
                .jsonPath("$.email")
                .isEqualTo(extractClaimFromToken(userToken.getAccessToken(), "email"))
                .jsonPath("$.roles")
                .isArray();
    }

    @Test
    void testCreateCourse_Successful() {
        // Thiết lập dữ liệu CourseDTO để tạo một khóa học mới
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Extract the "sub" claim from the teacherToken (which represents the teacher's user ID)
        String teacherId = extractClaimFromToken(teacherToken.getAccessToken(), "sub");

        // Gửi request POST với token của "teacher"
        webTestClient.post().uri("/courses")
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))  // Body của request là JSON CourseDTO
                .exchange()
                .expectStatus().isCreated()  // Kiểm tra xem phản hồi có trả về 201 Created không
                .expectHeader().value("Location", location -> assertThat(location).contains("/courses/"))  // Kiểm tra Location header
                .expectBody()
                .jsonPath("$.title").isEqualTo("Java Programming")  // Kiểm tra thuộc tính title của Course trả về
                .jsonPath("$.teacher").isEqualTo(teacherId);  // Kiểm tra teacher là sub (user ID từ token)
    }

    @Test
    void testCreateCourse_Unauthorized() {
        // Thiết lập dữ liệu CourseDTO
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Gửi request POST mà không có token
        webTestClient.post().uri("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseDTO))  // Body của request là JSON CourseDTO
                .exchange()
                .expectStatus().isUnauthorized();  // Kiểm tra phản hồi 401 Unauthorized
    }

    @Test
    void testCreateCourse_Forbidden() {
        // Thiết lập dữ liệu CourseDTO
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

        // Gửi request POST với token của người dùng không có quyền "teacher"
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
        var invalidCourseDTO = new CourseDTO(
                "",  // Tiêu đề trống
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

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
        // Thiết lập dữ liệu CourseDTO với dữ liệu quá lớn
        String largeDescription = "A".repeat(2001);  // Mô tả quá lớn

        var largeCourseDTO = new CourseDTO(
                "Java Programming",
                largeDescription,  // Mô tả quá lớn
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );

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
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = new CourseUpdateDTO(
                "Advanced Java Programming",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("Be a master OOP Java Programming"),
                Set.of("Be a master OOP Java Programming"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

        // Gửi request PUT với token của "teacher" để cập nhật khóa học
        webTestClient.put().uri("/courses/{courseId}", course.getId())  // Sử dụng Location từ header để update đúng khóa học
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isOk()  // Kiểm tra xem phản hồi có trả về 200 OK không
                .expectBody()
                .jsonPath("$.title").isEqualTo("Advanced Java Programming")  // Kiểm tra thuộc tính title đã được cập nhật
                .jsonPath("$.description").isEqualTo("Learn advanced Java programming")  // Kiểm tra thuộc tính description đã được cập nhật
                .jsonPath("$.thumbnailUrl").isEqualTo("http://example.com/new-thumbnail.jpg");  // Kiểm tra thuộc tính thumbnail đã được cập nhật
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
                    .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new UpdatePriceDTO(Money.of(1000, Currencies.VND))))  // Body của request là JSON CourseUpdateDTO.PriceDTO
                    .exchange()
                    .expectStatus().isOk();  // Phản hồi 200 OK
        }

        if (sectionDTOs != null && !sectionDTOs.isEmpty()) {
            for (CourseSectionDTO sectionDTO : sectionDTOs) {
                webTestClient.post().uri("/courses/{courseId}/sections", courseId)
                        .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
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
        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = new CourseUpdateDTO(
                "Advanced Java Programming",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("OOP", "Concurrency", "Multithreading"),  // Cập nhật benefits
                Set.of("Basic Programming", "Java SE"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

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
        var courseUpdateDTO = new CourseUpdateDTO(
                "Advanced Java Programming",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("OOP", "Concurrency", "Multithreading"),  // Cập nhật benefits
                Set.of("Basic Programming", "Java SE"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

        // Gửi request PUT với token của người dùng không có quyền "teacher"
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
        var courseUpdateDTO = new CourseUpdateDTO(
                "Advanced Java Programming",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("Be a master OOP Java Programming"),
                Set.of("Be a master OOP Java Programming"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

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
        var courseUpdateDTO = new CourseUpdateDTO(
                "",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("Be a master OOP Java Programming"),
                Set.of("Be a master OOP Java Programming prerequisite"),
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

        // Gửi request PUT với token của "teacher" để cập nhật khóa học
        webTestClient.put().uri("/courses/{courseId}", 111)  // Sử dụng Location từ header để update đúng khóa học
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isBadRequest();  // Kiểm tra phản hồi 400 Bad Request
    }

    @Test
    void testDeleteCourse_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isNoContent();  // Phản hồi trả về 204 No Content

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted
    }

    @Test
    void testDeleteCourse_WithAdmin_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của admin
                .exchange()
                .expectStatus().isNoContent();  // Phản hồi trả về 204 No Content

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted
    }

    @Test
    void testDeleteCourse_AlreadyDeleted() {
        // Lấy khóa học và đánh dấu là đã xóa
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isNoContent();  // Phản hồi trả về 204 No Content

        // Gửi request DELETE lần nữa
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteCourse_Unauthorized() {
        // Gửi request DELETE mà không đính kèm token
        webTestClient.delete().uri("/courses/{courseId}", 1000L) // dont need to be a valid course id
                .exchange()
                .expectStatus().isUnauthorized();  // Phản hồi 401 Unauthorized
    }

    @Test
    void testUpdatePrice_Successfully() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Gửi request PUT để cập nhật giá khóa học
        webTestClient.put().uri("/courses/{courseId}/update-price", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newPrice))  // Body của request là JSON CourseUpdateDTO.PriceDTO
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.price").isEqualTo("VND1,000.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testUpdatePrice_Forbidden() {
        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Gửi request PUT để cập nhật giá khóa học
        webTestClient.put().uri("/courses/{courseId}/update-price", 1)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newPrice))  // Body của request là JSON CourseUpdateDTO.PriceDTO
                .exchange()
                .expectStatus().isForbidden();  // Phản hồi 403 Forbidden
    }

    @Test
    void testAssignTeacher_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.teacher").isEqualTo("new-teacher-id");  // Kiểm tra giáo viên đã được cập nhật
    }

    @Test
    void testAssignTeacher_Forbidden() {
        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", 1)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isForbidden();  // Phản hồi 403 Forbidden
    }

    @Test
    void testAssignTeacher_NotFound() {
        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học không tồn tại
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", 999)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assignTeacherDTO))  // Body của request là JSON AssignTeacherDTO
                .exchange()
                .expectStatus().isNotFound();  // Phản hồi 404 Not Found
    }

    @Test
    void testPublishCourse_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        // Check các khóa học published không cần login
        webTestClient.get().uri("/published-courses/{courseId}", course.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Java Programming")
                .jsonPath("$.published").isEqualTo(true);
    }

    @Test
    void testPublishCourse_WithNoSetPriceOrNoSections_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testPublishCourse_WithUserNotAdmin_Forbidden() {
        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", 111L)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testApplyDiscount_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        Discount discount = discountRepository.findByCode("DISCOUNT_50").get();
        String discountCode = discount.getCode();

        // Thiết lập giảm giá cho khóa học
        var applyDiscountDTO = new ApplyDiscountDTO(discountCode);

        // Gửi request POST để áp dụng giảm giá cho khóa học
        webTestClient.post().uri("/courses/{courseId}/apply-discount", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(applyDiscountDTO))  // Body của request là JSON ApplyDiscountDTO
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.discountCode").isEqualTo(discountCode)  // Kiểm tra giảm giá đã được cập nhật
                .jsonPath("$.discountedPrice").isEqualTo("VND500.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testApplyDiscountFixed_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        Discount discount = discountRepository.findByCode("DISCOUNT_30_VN").get();
        String discountCode = discount.getCode();

        // Thiết lập giảm giá cho khóa học
        var applyDiscountDTO = new ApplyDiscountDTO(discountCode);

        // Gửi request POST để áp dụng giảm giá cho khóa học
        webTestClient.post().uri("/courses/{courseId}/apply-discount", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(applyDiscountDTO))  // Body của request là JSON ApplyDiscountDTO
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.discountCode").isEqualTo(discountCode)  // Kiểm tra giảm giá đã được cập nhật
                .jsonPath("$.discountedPrice").isEqualTo("VND970.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testApplyDiscount_NotFound() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Thiết lập giảm giá cho khóa học
        var applyDiscountDTO = new ApplyDiscountDTO("INVALID_DISCOUNT_CODE");

        // Gửi request POST để áp dụng giảm giá cho khóa học không tồn tại
        webTestClient.post().uri("/courses/{courseId}/apply-discount", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(applyDiscountDTO))  // Body của request là JSON ApplyDiscountDTO
                .exchange()
                .expectStatus().isNotFound();  // Phản hồi 404 Not Found
    }

    @Test
    void testApplyDiscount_Forbidden() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Thiết lập giảm giá cho khóa học
        var applyDiscountDTO = new ApplyDiscountDTO("DISCOUNT_50");

        // Gửi request POST để áp dụng giảm giá cho khóa học không có quyền
        webTestClient.post().uri("/courses/{courseId}/apply-discount", courseId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(applyDiscountDTO))  // Body của request là JSON ApplyDiscountDTO
                .exchange()
                .expectStatus().isForbidden();  // Phản hồi 403 Forbidden
    }

    @Test
    void testApplyDiscount_PriceNotSet() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Thiết lập giảm giá cho khóa học
        var applyDiscountDTO = new ApplyDiscountDTO("DISCOUNT_50"); // Correct discount code

        // Gửi request POST để áp dụng giảm giá cho khóa học không có giá
        webTestClient.post().uri("/courses/{courseId}/apply-discount", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(applyDiscountDTO))  // Body của request là JSON ApplyDiscountDTO
                .exchange()
                .expectStatus().isBadRequest();  // Phản hồi 400 Bad Request
    }

    @Test
    void testAddSectionToCourse_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null); // admin has the same permission as teacher

        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sections.length()").isEqualTo(1)
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(3);
    }

    @Test
    void testAddSectionToCourse_Unauthorized() {
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testAddSectionToCourse_Forbidden() {
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

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
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].title").isEqualTo(updateSectionDTO.title());
    }

    @Test
    void testUpdateSectionInfo_UserIsAdmin_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
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
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateSectionInfo_CoursePublished_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRemoveSection_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request DELETE để xóa section khỏi khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections.length()").isEqualTo(0);
    }

    @Test
    void testRemoveSection_CoursePublished_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher


        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        // Gửi request DELETE để xóa section khỏi khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddLesson_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(2);
    }

    @Test
    void testAddLesson_LessonDuplicate_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddLesson_CoursePublished_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request POST để thêm lesson cho section của khóa học
        var courseSection = course.getSections().iterator().next();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), courseSection.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
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
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateLesson_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO updateLessonDTO = new LessonDTO("New title", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var section = course.getSections().iterator().next();
        var lesson = section.getLessons().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lesson.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons[0].title").isEqualTo(updateLessonDTO.title());
    }

    @Test
    void testUpdateLesson_LessonDuplicate_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.VIDEO, "http://example.com/lesson2.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var section = course.getSections().iterator().next();
        var lesson1 = section.getLessons().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lesson1.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateLesson_CoursePublished_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null);

        // Gửi request PUT để cập nhật lesson của khóa học
        var courseSection = course.getSections().iterator().next();
        var lesson = courseSection.getLessons().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), courseSection.getId(), lesson.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
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
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testRemoveLesson_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request DELETE để xóa lesson khỏi khóa học
        var section = course.getSections().iterator().next();
        var lesson = section.getLessons().iterator().next();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lesson.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].lessons.length()").isEqualTo(0);
    }

    @Test
    void testRemoveLesson_CoursePublished_BadRequest() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        // Gửi request DELETE để xóa lesson khỏi khóa học
        var courseSection = course.getSections().iterator().next();
        var lesson = courseSection.getLessons().iterator().next();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), courseSection.getId(), lesson.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRemoveLesson_UserIsNotTeacherOrAdmin_Forbidden() {
        // Gửi request DELETE để xóa lesson khỏi khóa học
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", 100L, 200L, 300L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testCreateOrder_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

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
        webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của người dùng
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
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(userToken.accessToken, "sub"));
    }

    @Test
    void testCreateOrder_Unauthorized() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
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
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Chuẩn bị dữ liệu cho request tạo order
        Set<OrderItemDTO> orderItemsDto = new HashSet<>();
        orderItemsDto.add(new OrderItemDTO(courseId));
        var orderRequestDto = new OrderRequestDTO(orderItemsDto, null);
        // Course tồn tại nhưng không được publish

        // Gửi request POST để tạo order
        webTestClient.post().uri("/orders")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của người dùng
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderRequestDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateOrderAndThenPay_Successful() {
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("Be a master OOP Java Programming"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

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
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của người dùng
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
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(userToken.accessToken, "sub"))
                .returnResult().getResponseHeaders().getLocation().toString();

        String orderId = location.substring(location.lastIndexOf("/") + 1);

        // Gửi request POST để thanh toán order
        var paymentRequestDto = new PaymentRequest(
                UUID.fromString(orderId),
                Money.of(50000, Currencies.VND),
                PaymentMethod.STRIPE, "tok_visa");
        webTestClient.post().uri("/payments")
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))  // Đính kèm JWT của người dùng
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentRequestDto))
                .exchange()
                .expectStatus().isCreated();
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
            return claims.get(claimName).toString();  // Trả về giá trị của claim "sub"
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
            return (List<String>) claims.get(claimName);  // Trả về giá trị của claim "sub"
        } catch (IOException e) {
            throw new RuntimeException("Error decoding JWT", e);
        }
    }
}
