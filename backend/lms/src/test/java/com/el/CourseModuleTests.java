package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.*;
import com.el.course.application.dto.QuestionDTO.AnswerOptionDTO;
import com.el.course.domain.*;
import com.el.course.web.dto.AssignTeacherDTO;
import com.el.course.web.dto.CourseRequestApproveDTO;
import com.el.course.web.dto.UpdatePriceDTO;
import com.el.course.web.dto.UpdateSectionDTO;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CourseModuleTests extends AbstractLmsApplicationTests {



    @Test
    void testCreateCourse_Successful() {
        // Arrange
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
        // Arrange
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
        // Arrange
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
        // Arrange
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
        // Arrange
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
        // Arrange
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

    @Test
    void testUpdateInfoCourse_Unauthorized() {
        // Arrange
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
        // Arrange
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
        // Arrange
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
        // Arrange
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
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteCourse_Unauthorized() {
        // Act
        webTestClient.delete().uri("/courses/{courseId}", 1000L) // dont need to be a valid course id
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testDeleteForceCourse_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, null);

        // Act
        webTestClient.delete().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Assert
        Course deletedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();


        // act
        webTestClient.delete().uri("/courses/{courseId}?force=true", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(courseRepository.findById(course.getId())).isEmpty();
    }

    @Test
    void testUpdatePrice_Successfully() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        var courseId = course.getId();

        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Act
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
        var newPrice = new UpdatePriceDTO(Money.of(1000, Currencies.VND));

        // Act
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
        webTestClient.put().uri("/courses/{courseId}/requests/{requestId}/approve", 999L, 999L)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .body(BodyInserters.fromValue(TestFactory.createDefaultCourseRequestApproveDTOPublish()))
                .exchange()
                .expectStatus().isForbidden();
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

        // Act
        Long sectionId = webTestClient.post().uri("/courses/{courseId}/sections", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sectionDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(sectionId).isNotNull();
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
                .expectStatus().isOk();
    }

    @Test
    void testUpdateSectionInfo_UserIsAdmin_Successful() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, false, Set.of(sectionDTO)); // admin has the same permission as teacher

        UpdateSectionDTO updateSectionDTO = new UpdateSectionDTO("New title");

        // Act
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateSectionDTO))
                .exchange()
                .expectStatus().isOk();
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
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

        // Act
        var sectionId = course.getSections().iterator().next().getId();
        Long lessonId = webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(lessonId).isNotNull();
    }

    @Test
    void testAddLesson_LessonDuplicate_BadRequest() {
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 1", Lesson.Type.VIDEO, "http://example.com/lesson1.mp4");

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk();

        LessonDTO lessonDTODup = new LessonDTO("Lesson 1", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

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

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

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
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

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
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

        var sectionId = course.getSections().iterator().next().getId();
        var lessonId = webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(lessonId).isNotNull();
        ;

        LessonDTO updateLessonDTO = new LessonDTO("New title", Lesson.Type.TEXT, "http://example.com/lesson1.txt");

        // Act
        var section = course.getSections().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateLessonDTO))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateLesson_LessonDuplicate_OKBecauseIsItself() {
        // first, add a lesson
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

        // Gửi request POST để thêm lesson cho section của khóa học
        var sectionId = course.getSections().iterator().next().getId();

        var lessonId = webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(lessonId).isNotNull();

        //act
        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

        // Gửi request PUT để cập nhật lesson của khóa học
        var section = course.getSections().iterator().next();
        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId)
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

        LessonDTO updateLessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

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
        LessonDTO updateLessonDTO = new LessonDTO("New title", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

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
        LessonDTO lessonDTO = new LessonDTO("Lesson 2", Lesson.Type.TEXT, "http://example.com/lesson2.txt");

        var sectionId = course.getSections().iterator().next().getId();
        var lessonId = webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/lessons", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(lessonDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).returnResult().getResponseBody();

        // Assert
        assertThat(lessonId).isNotNull();
        ;

        // Act: Delete lesson
        var section = course.getSections().iterator().next();
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}", course.getId(), section.getId(), lessonId)
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

    // Post

    @Test
    void testAddPostToCourse_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        approvePublishByCourseId(course.getId());

        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act
        var postId = performCreatePost(postDTO, course.getId());

        // Act: Get post
        webTestClient.get().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(postId)
                .jsonPath("$.info.firstName").isEqualTo(extractClaimFromToken(teacherToken.accessToken, "given_name"))
                .jsonPath("$.info.lastName").isEqualTo(extractClaimFromToken(teacherToken.accessToken, "family_name"))
                .jsonPath("$.content").isEqualTo(postDTO.content())
                .jsonPath("$.attachmentUrls").isArray()
                .jsonPath("$.delete").doesNotExist();

        webTestClient.get().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdatePost_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        approvePublishByCourseId(course.getId());

        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act: Add post
        var postId = performCreatePost(postDTO, course.getId());

        // Assert
        assertThat(postId).isNotNull();

        // Act: Update post
        CoursePostDTO updatePostDTO = new CoursePostDTO("New content (Update)", null);
        webTestClient.put().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatePostDTO))
                .exchange()
                .expectStatus().isOk();

        // Act: Get post
        webTestClient.get().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(postId)
                .jsonPath("$.info.firstName").isEqualTo(extractClaimFromToken(teacherToken.accessToken, "given_name"))
                .jsonPath("$.info.lastName").isEqualTo(extractClaimFromToken(teacherToken.accessToken, "family_name"))
                .jsonPath("$.content").isEqualTo(updatePostDTO.content())
                .jsonPath("$.attachmentUrls").isEmpty()
                .jsonPath("$.delete").doesNotExist();
    }

    @Test
    void testCreateAndUpdatePost_UserIsNotTeacherOrAdmin_Forbidden() {
        // Arrange
        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act: Create post
        webTestClient.post().uri("/courses/{courseId}/posts", 100L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(postDTO))
                .exchange()
                .expectStatus().isForbidden();

        // Act: Update post
        webTestClient.put().uri("/courses/{courseId}/posts/{postId}", 100L, 200L)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(postDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testDeletePost_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        approvePublishByCourseId(course.getId());

        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act: Add post
        var postId = performCreatePost(postDTO, course.getId());

        // Assert
        assertThat(postId).isNotNull();

        // Act: Delete post
        webTestClient.delete().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Get post in trash
        webTestClient.get().uri("/courses/{courseId}/posts/trash", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1);

        // Act: Get post
        webTestClient.get().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRestorePost_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        approvePublishByCourseId(course.getId());

        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act: Add post
        var postId = performCreatePost(postDTO, course.getId());

        // Assert
        assertThat(postId).isNotNull();

        // Act: Delete post
        webTestClient.delete().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Restore post
        webTestClient.post().uri("/courses/{courseId}/posts/{postId}/restore", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        // Act: Get post
        webTestClient.get().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteForcePost_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher
        approvePublishByCourseId(course.getId());

        CoursePostDTO postDTO = TestFactory.createDefaultCoursePostDTO();

        // Act: Add post
        var postId = performCreatePost(postDTO, course.getId());

        // Assert
        assertThat(postId).isNotNull();

        // Act: Delete post
        webTestClient.delete().uri("/courses/{courseId}/posts/{postId}", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Delete force
        webTestClient.delete().uri("/courses/{courseId}/posts/{postId}?force=true", course.getId(), postId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/courses/{courseId}", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.posts.length()").isEqualTo(0);

        // Act: Get post in trash
        webTestClient.get().uri("/courses/{courseId}/posts/trash", course.getId())
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void testAddQuizToCourse_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Act: Get quiz
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(quizId)
                .jsonPath("$.title").isEqualTo(quizDTO.title())
                .jsonPath("$.description").isEqualTo(quizDTO.description())
                .jsonPath("$.passScorePercentage").isEqualTo(quizDTO.passScorePercentage())
                .jsonPath("$.afterLessonId").isEqualTo(lessonId)
                .jsonPath("$.questions").isArray()
                .jsonPath("$.delete").doesNotExist();

        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateQuiz_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Update quiz
        QuizUpdateDTO updateQuizDTO = TestFactory.createDefaultQuizUpdateDTO();

        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateQuizDTO))
                .exchange()
                .expectStatus().isOk();

        // Act: Get quiz
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(quizId)
                .jsonPath("$.title").isEqualTo(updateQuizDTO.title())
                .jsonPath("$.description").isEqualTo(updateQuizDTO.description())
                .jsonPath("$.passScorePercentage").isEqualTo(updateQuizDTO.passScorePercentage())
                .jsonPath("$.afterLessonId").isEqualTo(lessonId)
                .jsonPath("$.delete").doesNotExist();
    }

    @Test
    void testDeleteQuiz_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Delete quiz
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Get quiz in trash
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/trash", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo(quizId);

        // Act: Get quiz
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRestoreQuiz_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Delete quiz
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Restore quiz
        webTestClient.post().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}/restore", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        // Act: Get quiz in trash
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/trash", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);

        // Act: Get quiz
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteForceQuiz_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Delete quiz
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Delete force
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}?force=true", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Get quiz in trash
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/trash", course.getId(), sectionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void testAddQuestionToQuiz_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Add question
        QuestionDTO questionDTO = TestFactory.createDefaultQuestionDTO();

        var questionId = performCreateQuestion(questionDTO, course.getId(), sectionId, quizId);

        // Act: Get question
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo(quizDTO.title())
                .jsonPath("$.questions.length()").isEqualTo(1)
                .jsonPath("$.totalScore").isEqualTo(questionDTO.score())
                .jsonPath("$.questions[0].id").isEqualTo(questionId)
                .jsonPath("$.questions[0].content").isEqualTo(questionDTO.content())
                .jsonPath("$.questions[0].type").isEqualTo(questionDTO.type().name())
                .jsonPath("$.questions[0].options.length()").isEqualTo(4)
                .jsonPath("$.questions[0].score").isEqualTo(questionDTO.score());

        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}/questions/{questionId}", course.getId(), sectionId, quizId, questionId)
                .headers(header -> header.setBearerAuth(userToken.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void testUpdateQuestion_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Add question
        QuestionDTO questionDTO = TestFactory.createDefaultQuestionDTO();

        var questionId = performCreateQuestion(questionDTO, course.getId(), sectionId, quizId);

        // Assert
        assertThat(questionId).isNotNull();

        // Act: Update question
        QuestionDTO updateQuestionDTO = new QuestionDTO("New content (Update)", QuestionType.TRUE_FALSE,
                Set.of(
                        new AnswerOptionDTO("True answer", true),
                        new AnswerOptionDTO("False answer", false)
                ), 3);

        webTestClient.put().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}/questions/{questionId}", course.getId(), sectionId, quizId, questionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateQuestionDTO))
                .exchange()
                .expectStatus().isOk();

        // Act: Get question
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo(quizDTO.title())
                .jsonPath("$.questions.length()").isEqualTo(1)
                .jsonPath("$.totalScore").isEqualTo(updateQuestionDTO.score())
                .jsonPath("$.questions[0].id").isEqualTo(questionId)
                .jsonPath("$.questions[0].content").isEqualTo(updateQuestionDTO.content())
                .jsonPath("$.questions[0].type").isEqualTo(updateQuestionDTO.type().name())
                .jsonPath("$.questions[0].options.length()").isEqualTo(2)
                .jsonPath("$.questions[0].score").isEqualTo(updateQuestionDTO.score());
    }

    @Test
    void testDeleteQuestion_Successful() {
        // Arrange
        var courseDTO = TestFactory.createDefaultCourseDTO();
        CourseSectionDTO sectionDTO = new CourseSectionDTO("Billie Jean [4K] 30th Anniversary, 2001");
        Course course = createCourseWithParameters(teacherToken, courseDTO, true, Set.of(sectionDTO)); // admin has the same permission as teacher

        CourseSection courseSection = course.getSections().iterator().next();
        Long sectionId = courseSection.getId();
        Long lessonId = courseSection.getLessons().iterator().next().getId();

        QuizDTO quizDTO = TestFactory.createDefaultQuizDTO(lessonId);

        // Act: Add quiz
        var quizId = performCreateQuiz(quizDTO, course.getId(), sectionId);

        // Assert
        assertThat(quizId).isNotNull();

        // Act: Add question
        QuestionDTO questionDTO = TestFactory.createDefaultQuestionDTO();

        var questionId = performCreateQuestion(questionDTO, course.getId(), sectionId, quizId);

        // Assert
        assertThat(questionId).isNotNull();

        // Act: Delete question
        webTestClient.delete().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}/questions/{questionId}", course.getId(), sectionId, quizId, questionId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        // Act: Get question
        webTestClient.get().uri("/courses/{courseId}/sections/{sectionId}/quizzes/{quizId}", course.getId(), sectionId, quizId)
                .headers(header -> header.setBearerAuth(teacherToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo(quizDTO.title())
                .jsonPath("$.questions.length()").isEqualTo(0);
    }

}
