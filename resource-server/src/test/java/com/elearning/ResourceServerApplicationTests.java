package com.elearning;

import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.domain.Language;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
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

    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                    .withRealmImportFile("thainguyencoffee-realm.json");

    @Autowired
    private WebTestClient webTestClient;

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
    }

    @Test
    void contextLoads() {
        assertThat(userToken.accessToken).isNotNull();
        assertThat(user2Token.accessToken).isNotNull();
        assertThat(teacherToken.accessToken).isNotNull();
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
