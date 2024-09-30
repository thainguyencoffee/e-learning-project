package com.elearning;

import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.application.dto.LessonDTO;
import com.elearning.course.domain.*;
import com.elearning.course.web.ApplyDiscountDTO;
import com.elearning.course.web.AssignTeacherDTO;
import com.elearning.course.web.UpdatePriceDTO;
import com.elearning.course.web.UpdateSectionDTO;
import com.elearning.discount.domain.Discount;
import com.elearning.discount.domain.DiscountRepository;
import com.elearning.discount.domain.Type;
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
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ResourceServerApplicationTests {

    protected static KeycloakToken userToken;
    protected static KeycloakToken user2Token;
    protected static KeycloakToken teacherToken;
    protected static KeycloakToken bossToken;

    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("thainguyencoffee-realm.json");

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private DiscountRepository discountRepository;

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/thainguyencoffee");
    }

    @BeforeAll
    static void generateAccessToken() {
        keycloak.start(); // Fix: Mapped port can only be obtained after the container is started

        WebClient webClient = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl() + "/realms/thainguyencoffee/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        userToken   = authenticateWith("user", "1", webClient);
        user2Token = authenticateWith("user2", "1", webClient);
        teacherToken = authenticateWith("teacher", "1", webClient);
        bossToken = authenticateWith("boss", "1", webClient);
    }

    @BeforeEach
    void setupData() {
        courseRepository.deleteAll();  // Xóa dữ liệu test trước đó

        // Tạo một khóa học giả lập trong database
        Course course = new Course(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
                Language.ENGLISH,
                Set.of("Basic Programming Knowledge"),
                Set.of(Language.ENGLISH, Language.SPANISH),
                "teacher-id"  // Giáo viên giả lập
        );
        courseRepository.save(course);  // Lưu khóa học vào CSDL

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
                "DISCOUNT_30_DOLLARS",
                Type.FIXED,
                null,
                Money.of(30, "USD"),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600)
        );
        discountRepository.save(discount2);
    }


    @Test
    void contextLoads() {
        assertThat(userToken.accessToken).isNotNull();
        assertThat(user2Token.accessToken).isNotNull();
        assertThat(teacherToken.accessToken).isNotNull();
        assertThat(bossToken.accessToken).isNotNull();
    }

    @Test
    void testCreateCourse_Successful() {
        // Thiết lập dữ liệu CourseDTO để tạo một khóa học mới
        var courseDTO = new CourseDTO(
                "Java Programming",
                "Learn Java from scratch",
                "http://example.com/thumbnail.jpg",
                Set.of("OOP", "Concurrency"),
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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = new CourseUpdateDTO(
                "Advanced Java Programming",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("OOP", "Concurrency", "Multithreading"),  // Cập nhật benefits
                Set.of("Basic Programming", "Java SE"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

        // Gửi request PUT với token của "teacher" để cập nhật khóa học
        webTestClient.put().uri("/courses/{courseId}", courseId)  // Sử dụng Location từ header để update đúng khóa học
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
                Set.of("OOP", "Concurrency", "Multithreading"),  // Cập nhật benefits
                Set.of("Basic Programming", "Java SE"),  // Cập nhật prerequisites
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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập dữ liệu CourseUpdateDTO để cập nhật thông tin khóa học
        var courseUpdateDTO = new CourseUpdateDTO(
                "",  // Cập nhật title mới
                "Learn advanced Java programming",  // Cập nhật description mới
                "http://example.com/new-thumbnail.jpg",  // Cập nhật thumbnail mới
                Set.of("OOP", "Concurrency", "Multithreading"),  // Cập nhật benefits
                Set.of("Basic Programming", "Java SE"),  // Cập nhật prerequisites
                Set.of(Language.ENGLISH, Language.GERMAN)  // Cập nhật subtitles
        );

        // Gửi request PUT với token của "teacher" để cập nhật khóa học
        webTestClient.put().uri("/courses/{courseId}", courseId)  // Sử dụng Location từ header để update đúng khóa học
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(courseUpdateDTO))  // Body của request là JSON CourseUpdateDTO
                .exchange()
                .expectStatus().isBadRequest();  // Kiểm tra phản hồi 400 Bad Request
    }

    @Test
    void testDeleteCourse_Successful() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", courseId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isNoContent();  // Phản hồi trả về 204 No Content

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(courseId).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted
    }

    @Test
    void testDeleteCourse_WithAdmin_Successful() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Xóa khóa học với token của giáo viên
        webTestClient.delete().uri("/courses/{courseId}", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của admin
                .exchange()
                .expectStatus().isNoContent();  // Phản hồi trả về 204 No Content

        // Kiểm tra rằng khóa học đã bị đánh dấu là deleted
        Course deletedCourse = courseRepository.findById(courseId).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();  // Kiểm tra khóa học đã bị đánh dấu deleted
    }

    @Test
    void testDeleteCourse_AlreadyDeleted() {
        // Lấy khóa học và đánh dấu là đã xóa
        Course course = courseRepository.findAll().iterator().next();
        course.delete();  // Đánh dấu khóa học là đã xóa
        courseRepository.save(course);

        // Gửi request DELETE lần nữa
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteCourse_Unauthorized() {
        // Lấy khóa học từ database
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Gửi request DELETE mà không đính kèm token
        webTestClient.delete().uri("/courses/{courseId}", courseId)
                .exchange()
                .expectStatus().isUnauthorized();  // Phản hồi 401 Unauthorized
    }

    @Test
    void testUpdatePrice_Successfully() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, "USD"));

        // Gửi request PUT để cập nhật giá khóa học
        webTestClient.put().uri("/courses/{courseId}/update-price", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newPrice))  // Body của request là JSON CourseUpdateDTO.PriceDTO
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.price").isEqualTo("USD1,000.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testUpdatePrice_Forbidden() {
        // Thiết lập giá mới cho khóa học
        var newPrice = new UpdatePriceDTO(Money.of(1000, "USD"));

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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập giáo viên mới cho khóa học
        var assignTeacherDTO = new AssignTeacherDTO("new-teacher-id");

        // Gửi request PUT để cập nhật giáo viên cho khóa học
        webTestClient.put().uri("/courses/{courseId}/assign-teacher", courseId)
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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập giá cho khóa học
        course.changePrice(Money.of(1000, "USD"));
        // Thiết lập các sections cho khóa học
        CourseSection courseSection = new CourseSection("Section 1");
        courseSection.addLesson(new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null));
        course.addSection(courseSection);
        // Lưu khóa học đã cập nhật
        courseRepository.save(course);

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản
    }

    @Test
    void testPublishCourse_WithNoSetPriceOrNoSections_BadRequest() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", courseId)
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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        course.changePrice(Money.of(1000, "USD"));  // Đặt giá cho khóa học
        courseRepository.save(course);  // Lưu khóa học đã cập nhật

        Long courseId = course.getId();
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
                .jsonPath("$.discountedPrice").isEqualTo("USD500.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testApplyDiscountFixed_Successful() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        course.changePrice(Money.of(1000, "USD"));  // Đặt giá cho khóa học
        courseRepository.save(course);  // Lưu khóa học đã cập nhật

        Long courseId = course.getId();
        Discount discount = discountRepository.findByCode("DISCOUNT_30_DOLLARS").get();
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
                .jsonPath("$.discountedPrice").isEqualTo("USD970.00");  // Kiểm tra giá đã được cập nhật
    }

    @Test
    void testApplyDiscount_NotFound() {
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        course.changePrice(Money.of(1000, "USD"));  // Đặt giá cho khóa học
        courseRepository.save(course);  // Lưu khóa học đã cập nhật
        Long courseId = course.getId();

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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        course.changePrice(Money.of(1000, "USD"));  // Đặt giá cho khóa học
        courseRepository.save(course);  // Lưu khóa học đã cập nhật
        Long courseId = course.getId();

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
        // Lấy khóa học từ CSDL thật (được tạo ở setupData)
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

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
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", courseId)
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
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testAddSectionToCourse_Forbidden() {
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        CourseSectionDTO sectionDTO = new CourseSectionDTO(
                "Section 1",
                Set.of(new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null),
                        new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null),
                        new LessonDTO("Lesson 3", Lesson.Type.QUIZ, null, 1L))
        );

        // Gửi request POST để thêm section cho khóa học
        webTestClient.post().uri("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void testUpdateSectionInfo_UserIsTeacher_Successful() {
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        CourseSection section = new CourseSection("Section 1");
        section.addLesson(new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null));
        section.addLesson(new Lesson("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null));
        section.addLesson(new Lesson("Lesson 3", Lesson.Type.QUIZ, null, 1L));
        course.addSection(section);
        courseRepository.save(course);

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", courseId, section.getId())
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
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        CourseSection section = new CourseSection("Section 1");
        section.addLesson(new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null));
        section.addLesson(new Lesson("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt", null));
        section.addLesson(new Lesson("Lesson 3", Lesson.Type.QUIZ, null, 1L));
        course.addSection(section);
        courseRepository.save(course);

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", courseId, section.getId())
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
        Course course = courseRepository.findAll().iterator().next();
        Long courseId = course.getId();

        // Thiết lập giá cho khóa học
        course.changePrice(Money.of(1000, "USD"));
        // Thiết lập các sections cho khóa học
        CourseSection courseSection = new CourseSection("Section 1");
        courseSection.addLesson(new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4", null));
        course.addSection(courseSection);
        // Lưu khóa học đã cập nhật
        courseRepository.save(course);

        // Gửi request PUT để xuất bản khóa học
        webTestClient.put().uri("/courses/{courseId}/publish", courseId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .exchange()
                .expectStatus().isOk()  // Phản hồi 200 OK
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);  // Kiểm tra khóa học đã được xuất bản

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Gửi request PUT để cập nhật section info cho khóa học
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", courseId, courseSection.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))  // Đính kèm JWT của giáo viên
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isBadRequest();
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
                        .with("client_id", "thainguyencoffee-confidentials")
                        .with("client_secret", "qk2lxjuIPAUY0e9I1AMzQZLQf3YINJ80")
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
}
