package com.elearning.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.*;
import com.elearning.discount.application.DiscountService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.money.MonetaryAmount;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class CourseServiceTest {

    @Mock
    private CourseRepository repository;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourseWithoutDiscountShouldSuccess() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(1L, "Section 1", "Description", Set.of(
                        new LessonDTO(1L, "Lesson 1", "https://lesson-link.com", "VIDEO")))),
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert & Verify
        verify(repository, times(1)).save(any(Course.class));
        verify(discountService, times(0)).calculateDiscountForCourse(anyLong(), any());
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        assertThat(courseCreated.getSections()).hasSize(1);
        assertThat(courseCreated.getSections().iterator().next().getLessons()).hasSize(1);
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testCreateCourseWithoutCourseSectionsShouldSuccess() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                null, // Course sections are null
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        verify(discountService, times(0)).calculateDiscountForCourse(anyLong(), any()); // Discount should not be applied
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        // Course sections should be null
        assertThat(courseCreated.getSections()).isEmpty();
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testCreateCourseWithAudienceIsPublicWithNonEmptyAuthorities() {
        // Why isPublic = true and emailAuthorities is valid?
        // Because don't want to clear the emailAuthorities when isPublic = true

        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(true, Set.of("email1@example.com")), // isPublic = true, emailAuthorities not empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        verify(discountService, times(0)).calculateDiscountForCourse(anyLong(), any()); // Discount should not be applied
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        // Course sections should be null
        assertThat(courseCreated.getSections()).isEmpty();
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testCreateCourseWithAudienceIsNotPublicWithNullAuthoritiesShouldThrowException() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, null), // isPublic = false, but emailAuthorities is null
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Act & Assert
        assertThrows(AudienceInvalidException.class, () -> courseService.createCourse(courseRequestDTO));
    }

    @Test
    void testCreateCourseWithAudienceIsNotPublicWithEmptyAuthoritiesShouldThrowException() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of()), // isPublic = false, but emailAuthorities is empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Act & Assert
        assertThrows(AudienceInvalidException.class, () -> courseService.createCourse(courseRequestDTO));
    }

    @Test
    void testCreateCourseWithAudienceIsNotPublicWithNonEmptyAuthorities() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")), // isPublic = false, emailAuthorities not empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        courseService.createCourse(courseRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        verify(discountService, times(0)).calculateDiscountForCourse(anyLong(), any()); // Discount should not be applied
    }

    @Test
    void testCreateCourseWithAudienceIsPublicWithNullAuthorities() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                // Null emailAuthorities, but valid because isPublic = true
                // isPublic = true, emailAuthorities is null
                new AudienceDTO(true, null),
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Course object to be saved
        Course course = courseRequestDTO.toCourse();
        course.setId(1L);

        // Mock repository behavior
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        courseService.createCourse(courseRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        verify(discountService, times(0)).calculateDiscountForCourse(anyLong(), any()); // Discount should not be applied
    }

    @Test
    void testCreateCourseWithDiscountShouldSuccess() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(1L, "Section 1", "Description", Set.of(
                        new LessonDTO(1L, "Lesson 1", "https://lesson-link.com", "VIDEO")))),
                1L // Discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        Course course = courseRequestDTO.toCourse();
        course.setId(1L);
        MonetaryAmount discountedPrice = Money.of(20, "USD");

        when(discountService.calculateDiscountForCourse(1L, Money.of(100, "USD"))).thenReturn(discountedPrice);
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert
        verify(discountService, times(1)).calculateDiscountForCourse(1L, Money.of(100, "USD"));
        verify(repository, times(1)).save(any(Course.class));
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        // Discounted price should be applied and value = 20 USD
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price().subtract(discountedPrice));
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        assertThat(courseCreated.getSections()).hasSize(1);
        assertThat(courseCreated.getSections().iterator().next().getLessons()).hasSize(1);
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testCreateCourseWithDiscountNotFoundShouldSuccess() {
        // Arrange
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(1L, "Section 1", "Description", Set.of(
                        new LessonDTO(1L, "Lesson 1", "https://lesson-link.com", "VIDEO")))),
                1L // Discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        Course course = courseRequestDTO.toCourse();
        course.setId(1L);

        when(discountService.calculateDiscountForCourse(1L, Money.of(100, "USD")))
                .thenThrow(new ResourceNotFoundException("Discount not found"));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert
        verify(discountService, times(1)).calculateDiscountForCourse(1L, Money.of(100, "USD"));
        verify(repository, times(1)).save(any(Course.class));
        // verifyNoMoreInteractions should ok because has exception thrown
        verifyNoMoreInteractions(discountService);
        verify(discountService, times(1)).calculateDiscountForCourse(1L, Money.of(100, "USD"));
        verify(repository, times(1)).save(any(Course.class));
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        // Discounted price should be applied and value = 20 USD
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        assertThat(courseCreated.getSections()).hasSize(1);
        assertThat(courseCreated.getSections().iterator().next().getLessons()).hasSize(1);
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testCreateCourseWithMultipleSectionsAndLessonsShouldSuccess() {
        // Arrange
        Set<LessonDTO> lessons1 = Set.of(new LessonDTO(1L, "Lesson 1", "http://lesson1.com", "VIDEO"));
        Set<LessonDTO> lessons2 = Set.of(new LessonDTO(2L, "Lesson 2", "http://lesson2.com", "TEXT"));

        Set<CourseSectionDTO> sections = Set.of(
                new CourseSectionDTO(1L, "Section 1", "Description", lessons1),
                new CourseSectionDTO(2L, "Section 2", "Description", lessons2)
        );

        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                sections,
                null // No discount
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course courseCreated = courseService.createCourse(courseRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class));

        // verifyNoMoreInteractions should ok because no more interactions with discountService
        verifyNoMoreInteractions(discountService);
        verify(repository, times(1)).save(any(Course.class));
        assertThat(courseCreated).isNotNull();
        assertThat(courseCreated.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(courseCreated.getPrice()).isEqualTo(courseRequestDTO.price());
        // Discounted price should be applied and value = 20 USD
        assertThat(courseCreated.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(courseCreated.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(courseCreated.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(courseCreated.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        assertThat(courseCreated.getSections()).hasSize(2);
        assertThat(courseCreated.getSections().iterator().next().getLessons()).hasSize(1);
        assertThat(courseCreated.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }

    @Test
    void testUpdateCourseThenUpdateSectionsAndLessons() {
        // Arrange
        var existingSectionId = 1L;
        var existingLessonId = 1L;
        var oldDiscountId = 1L;
        var newDiscountId = 2L;
        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com"))
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setDiscountId(oldDiscountId);
        existingCourse.setDiscountedPrice(Money.of(80, "USD"));

        CourseSection existingSection = new CourseSection("Section 1");
        existingSection.setId(existingSectionId);

        Lesson existingLesson = new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://lesson1.com");
        existingLesson.setId(existingLessonId);
        existingSection.addLesson(existingLesson);
        existingCourse.addSection(existingSection);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Updated Spring Boot Course",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(existingSectionId, "Updated Section 1", "Updated section description", Set.of(
                        new LessonDTO(existingSectionId, "Updated Lesson 1", "http://updatedlesson1.com", "TEXT")))),
                newDiscountId // Update discount then should apply new discount
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingCourse));
        // mock discount for id = 2
        MonetaryAmount discountedPrice = Money.of(20, "USD");

        when(discountService.calculateDiscountForCourse(newDiscountId, updateRequestDTO.price())).thenReturn(discountedPrice);
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(1L, updateRequestDTO);

        // Assert
        assertNotNull(updatedCourse);
        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        // original price after update: 150 - 20 = 130 USD
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price().subtract(discountedPrice));
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).hasSize(1);
        assertThat(updatedCourse.getSections().iterator().next().getLessons()).hasSize(1);
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());

        CourseSection updatedSection = updatedCourse.findSectionById(1L);
        assertNotNull(updatedSection);
        assertEquals("Updated Section 1", updatedSection.getTitle());

        Lesson updatedLesson = updatedSection.findLessonById(1L);
        assertNotNull(updatedLesson);
        assertEquals("Updated Lesson 1", updatedLesson.getTitle());
        assertEquals("http://updatedlesson1.com", updatedLesson.getLink());
    }

    @Test
    void testUpdateCourseWithoutCourseSections() {
        // Arrange
        var existingSectionId = 1L;
        var existingLessonId = 1L;
        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setDiscountId(1L);
        existingCourse.setDiscountedPrice(Money.of(80, "USD"));

        CourseSection existingSection = new CourseSection("Section 1");
        existingSection.setId(existingSectionId);

        Lesson existingLesson = new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://lesson1.com");
        existingLesson.setId(existingLessonId);
        existingSection.addLesson(existingLesson);
        existingCourse.addSection(existingSection);
        
        
        CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description of course",
                new AudienceDTO(false, Set.of("email1@example.com")),
                null, // Course sections are null, meaning sections should remain unchanged
                null  // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior: find the existing course by ID
        when(repository.findById(1L)).thenReturn(Optional.of(existingCourse));
        // Mock repository behavior: save the updated course
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(1L, courseRequestDTO);

        // Assert
        assertNotNull(updatedCourse); // Đảm bảo rằng khóa học đã được cập nhật
        assertNotNull(updatedCourse.getSections()); // Sections không bị xóa khi null
        assertThat(updatedCourse.getSections().size()).isEqualTo(1); // Số lượng sections không thay đổi
        // Kiểm tra rằng course đã được lưu đúng cách với repository
        verify(repository, times(1)).save(any(Course.class));

        // Không có giảm giá thì service calculateDiscountForCourse không được gọi
        verifyNoMoreInteractions(discountService);

        assertEquals(updatedCourse.getPrice(), courseRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(courseRequestDTO.price());
        assertThat(updatedCourse.getTitle()).isEqualTo(courseRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(courseRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(courseRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(courseRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).hasSize(1);
        assertThat(updatedCourse.getSections().iterator().next().getLessons()).hasSize(1);
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(courseRequestDTO.teacherId());
    }


    @Test
    void testUpdateCourseThenAddNewSectionAndLesson() {
        // Arrange
        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Updated Spring Boot Course",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(null, "New Section", "New section description", Set.of(
                        new LessonDTO(null, "New Lesson", "http://newlesson.com", "VIDEO")))),
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(1L, updateRequestDTO);

        // Assert
        assertNotNull(updatedCourse);
        assertEquals(1, updatedCourse.getSections().size());

        CourseSection newSection = updatedCourse.getSections().stream()
                .filter(section -> "New Section".equals(section.getTitle()))
                .findFirst()
                .orElse(null);

        assertNotNull(newSection);
        assertEquals(1, newSection.getLessons().size());

        Lesson newLesson = newSection.getLessons().stream()
                .filter(lesson -> "New Lesson".equals(lesson.getTitle()))
                .findFirst()
                .orElse(null);

        assertNotNull(newLesson);
        assertEquals("http://newlesson.com", newLesson.getLink());

        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price());
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).hasSize(1);
        assertThat(updatedCourse.getSections().iterator().next().getLessons()).hasSize(1);
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }

    @Test
    void testUpdateCourseRemoveSectionAndLessonNotInDTO() {
        // Arrange
        var sectionIdToKeep = 1L;
        var lessonIdToKeep = 1L;
        // Create existing course with two sections and a section will be removed
        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        CourseSection sectionToKeep = new CourseSection("Section 1");
        sectionToKeep.setId(sectionIdToKeep);
        Lesson lessonToKeep = new Lesson("Lesson 1", Lesson.Type.VIDEO, "http://lesson1.com");
        lessonToKeep.setId(lessonIdToKeep);
        sectionToKeep.addLesson(lessonToKeep);

        CourseSection sectionToRemove = new CourseSection("Section 2");
        sectionToRemove.setId(2L);
        Lesson lessonToRemove = new Lesson("Lesson 2", Lesson.Type.VIDEO, "http://lesson2.com");
        lessonToRemove.setId(2L);
        sectionToRemove.addLesson(lessonToRemove);

        existingCourse.addSection(sectionToKeep);
        existingCourse.addSection(sectionToRemove);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Updated Spring Boot Course",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(sectionIdToKeep, "Section 1 Updated", "Updated section description", Set.of(
                        new LessonDTO(lessonIdToKeep, "Lesson 1 Updated", "http://updatedlesson1.com", "TEXT")))),
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(1L, updateRequestDTO);

        // Assert
        assertNotNull(updatedCourse);
        assertEquals(1, updatedCourse.getSections().size());

        CourseSection updatedSection = updatedCourse.findSectionById(sectionIdToKeep);
        assertNotNull(updatedSection);
        assertEquals("Section 1 Updated", updatedSection.getTitle());

        Lesson updatedLesson = updatedSection.findLessonById(lessonIdToKeep);
        assertNotNull(updatedLesson);
        assertEquals("Lesson 1 Updated", updatedLesson.getTitle());

        // Ensure sectionToRemove and its lesson were removed
        assertNull(updatedCourse.findSectionById(2L));

        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price()); // No discount applied
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).hasSize(1);
        assertThat(updatedCourse.getSections().iterator().next().getLessons()).hasSize(1);
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }

    @Test
    void testUpdateCourseWithInvalidDiscount() {
        // Arrange
        var discountId = 1L;
        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"), "Description of course",
                new Audience(false, Set.of("email1@example.com"))
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Updated Spring Boot Course",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of("email1@example.com")),
                Set.of(new CourseSectionDTO(null, "Section 1", "Updated section description", Set.of())),
                discountId // Invalid Discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new ResourceNotFoundException("Discount not found"))
                .when(discountService).calculateDiscountForCourse(discountId, updateRequestDTO.price());

        // Act
        Course updatedCourse = courseService.updateCourse(1L, updateRequestDTO);

        // Verify & Assert
        verify(discountService, times(1)).calculateDiscountForCourse(discountId, Money.of(150, "USD"));
        assertNotNull(updatedCourse);
        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price()); // No discount applied
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).hasSize(1);
        assertThat(updatedCourse.getSections().iterator().next().getLessons()).hasSize(0);
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }

    @Test
    void deleteCourseSuccessfully() {
        Course course = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(true, null),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        course.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(repository, times(1)).delete(course);
    }

    @Test
    void deleteCourseWithCourseHasSectionsShouldThrows() {
        CourseSection section = new CourseSection("Section 1");
        Course course = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(true, null),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1"));
        course.addSection(section);
        course.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(course));

        assertThrows(CourseHasSectionsException.class, () -> courseService.deleteCourse(1L));

        verify(repository, times(0)).delete(course);
    }

    @Test
    void deleteCourseWhenCourseNotFoundShouldThrows() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));

        verify(repository, times(0)).delete(any(Course.class));
    }

    @Test
    void testUpdateCourseWithAudienceIsPublicAndNonEmptyAuthorities() {
        // Arrange
        Long courseId = 1L;

        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setId(courseId);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(true, Set.of("email1@example.com")), // isPublic = true, emailAuthorities not empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(courseId, updateRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        assertNotNull(updatedCourse);
        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price()); // No discount applied
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).isEmpty();
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }

    @Test
    void testUpdateCourseWithAudienceIsNotPublicAndNullAuthoritiesShouldThrows() {
        // Arrange
        Long courseId = 1L;

        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setId(courseId);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, null), // isPublic = false, but emailAuthorities is null
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.findById(courseId)).thenReturn(Optional.of(existingCourse));

        // Act & Assert
        assertThrows(AudienceInvalidException.class, () -> courseService.updateCourse(courseId, updateRequestDTO));
    }

    @Test
    void testUpdateCourseWithAudienceIsNotPublicWithEmptyAuthoritiesShouldThrows() {
        // Arrange
        Long courseId = 1L;

        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setId(courseId);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of()), // isPublic = false, but emailAuthorities is empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.findById(courseId)).thenReturn(Optional.of(existingCourse));

        // Act & Assert
        assertThrows(AudienceInvalidException.class, () -> courseService.updateCourse(courseId, updateRequestDTO));
    }

    @Test
    void testUpdateCourseWithAudienceIsNotPublicAndNonEmptyAuthoritiesShouldOK() {
        // Arrange
        Long courseId = 1L;

        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setId(courseId);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(false, Set.of("email1@example.com", "email2@example.com")), // isPublic = false, emailAuthorities not empty
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(courseId, updateRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once
        assertNotNull(updatedCourse);
        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price()); // No discount applied
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).isEmpty();
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }

    @Test
    void testUpdateCourseWithAudienceIsPublicAndNullAuthoritiesShouldOK() {
        // Arrange
        Long courseId = 1L;

        Course existingCourse = new Course(
                "Spring Boot Course",
                Money.of(100, "USD"),
                "Description of course",
                new Audience(false, Set.of("email1@example.com")),
                "foo.jpg",
                UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );
        existingCourse.setId(courseId);

        CourseRequestDTO updateRequestDTO = new CourseRequestDTO(
                "Spring Boot Course Updated",
                Money.of(150, "USD"),
                "Updated description",
                new AudienceDTO(true, null), // isPublic = true, emailAuthorities is null
                null, // Course sections
                null // No discount ID
                , "foo.jpg",
                existingCourse.getTeacherId(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1")
        );

        // Mock repository behavior
        when(repository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(courseId, updateRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(Course.class)); // Ensure save is called once

        assertNotNull(updatedCourse);
        assertEquals(updatedCourse.getPrice(), updateRequestDTO.price());
        assertThat(updatedCourse.getDiscountedPrice()).isEqualTo(updateRequestDTO.price()); // No discount applied
        assertThat(updatedCourse.getTitle()).isEqualTo(updateRequestDTO.title());
        assertThat(updatedCourse.getDescription()).isEqualTo(updateRequestDTO.description());
        assertThat(updatedCourse.getAudience().isPublic()).isEqualTo(updateRequestDTO.audience().isPublic());
        assertThat(updatedCourse.getAudience().emailAuthorities()).isEqualTo(updateRequestDTO.audience().emailAuthorities());
        assertThat(updatedCourse.getSections()).isEmpty();
        // teacher id should be updated
        assertThat(updatedCourse.getTeacherId()).isEqualTo(updateRequestDTO.teacherId());
    }


}
