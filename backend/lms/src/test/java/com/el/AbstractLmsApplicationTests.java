package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.*;
import com.el.course.domain.Course;
import com.el.course.domain.CourseRepository;
import com.el.course.domain.Lesson;
import com.el.course.web.dto.CourseRequestApproveDTO;
import com.el.course.web.dto.UpdatePriceDTO;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.domain.DiscountRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient(timeout = "36000")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractLmsApplicationTests {

    protected static KeycloakToken userToken;
    protected static KeycloakToken user2Token;
    protected static KeycloakToken teacherToken;
    protected static KeycloakToken bossToken;

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected CourseRepository courseRepository;

    @Autowired
    protected DiscountRepository discountRepository;

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        KeycloakTestContainer.keycloakProperties(registry);
    }

    @BeforeAll
    static void generateAccessToken() {
        WebClient webClient = WebClient.builder()
                .baseUrl(KeycloakTestContainer.getInstance().getAuthServerUrl() + "/realms/keycloak101/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        userToken = authenticateWith("user", "1", webClient);
        user2Token = authenticateWith("user2", "1", webClient);
        teacherToken = authenticateWith("teacher", "1", webClient);
        bossToken = authenticateWith("boss", "1", webClient);
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

    // extracted method for reusing
    protected void approvePublishByCourseId(Long courseId) {
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

    protected Course createCourseWithParameters(KeycloakToken token, CourseDTO courseDTO, boolean hasPrice, Set<CourseSectionDTO> sectionDTOs) {
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

        if (sectionDTOs != null && !sectionDTOs.isEmpty()) {
            for (CourseSectionDTO sectionDTO : sectionDTOs) {
                Long sectionId = webTestClient.post().uri("/courses/{courseId}/sections", courseId)
                        .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(sectionDTO))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Long.class)
                        .returnResult()
                        .getResponseBody();

                webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", courseId, sectionId)
                        .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new LessonDTO(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vestibulum mi quis libero luctus sollicitudin. Suspendisse laoreet vulputate est",
                                Lesson.Type.VIDEO,
                                "https://www.youtube.com/watch?v=1")
                        ))
                        .exchange()
                        .expectStatus().isOk();

                webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", courseId, sectionId)
                        .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new LessonDTO(
                                "Donec facilisis vel tortor eget efficitur. Sed congue ante mi, sed tristique purus feugiat a",
                                Lesson.Type.VIDEO,
                                "https://www.youtube.com/watch?v=2")
                        ))
                        .exchange()
                        .expectStatus().isOk();
            }
        }

        if (hasPrice) {
            webTestClient.put().uri("/courses/{courseId}/update-price", courseId)
                    .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new UpdatePriceDTO(Money.of(1000, Currencies.VND))))
                    .exchange()
                    .expectStatus().isOk();
        }

        return courseRepository.findById(Long.valueOf(courseId))
                .orElseThrow(() -> new IllegalStateException("createCourseWithParameters something went wrong!"));
    }

    protected String performCreateDiscountTest(DiscountDTO discountDTO) {
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

    protected Long performCreatePost(CoursePostDTO postDTO, Long courseId) {
        // Act: Add post
        var postId = webTestClient.post().uri("/courses/{courseId}/posts", courseId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(postDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(postId).isNotNull();
        return postId;
    }

    protected Long performCreateQuiz(QuizDTO quizDTO, Long courseId, Long sectionId) {
        // Act: Add quiz
        var quizId = webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/quizzes", courseId, sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(quizDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(quizId).isNotNull();
        return quizId;
    }

    protected Long performCreateQuestion(QuestionDTO questionDTO, Long id, Long sectionId, Long quizId) {
        return webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}/questions", id, sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(questionDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();
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

    protected String extractClaimFromToken(String token, String claimName) {
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

    protected static class KeycloakToken {
        protected final String accessToken;

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
