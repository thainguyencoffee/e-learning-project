package com.elearning.course.domain;

import com.elearning.common.config.DataAuditConfig;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class CourseJdbcTests {

    private static final CurrencyUnit USD = Monetary.getCurrency("USD");

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        // Save some test data
        Set<CourseSection> sections1 = Set.of(new CourseSection("Section 1.1"), new CourseSection("Section 1.2"));
        Set<CourseSection> sections2 = Set.of(new CourseSection("Section 2.1"), new CourseSection("Section 2.2"));
        Set<CourseSection> sections3 = Set.of(new CourseSection("Section 3.1"), new CourseSection("Section 3.2"));
        Set<CourseSection> sections4= Set.of(new CourseSection("Section 4.1"), new CourseSection("Section 4.2"));
        Course course1 = new Course("Course 1", Money.of(100, USD), "Description 1", new Audience(true, null), "foo.jpg", UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1"));
        Course course2 = new Course("Course 2", Money.of(200, USD), "Description 2", new Audience(false, Set.of("admin@elearning.com")), "foo.jpg", UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1"));
        Course course3 = new Course("Course 3", Money.of(300, USD), "Description 3", new Audience(true, null), "foo.jpg", UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1"));
        Course course4 = new Course("Course 4", Money.of(400, USD), "Description 4", new Audience(false, Set.of("student@elearning.com", "teacher@elearning.com")), "foo.jpg", UUID.randomUUID().toString(),
                Term.LIFETIME,
                Language.ENGLISH,
                Set.of(Language.VIETNAMESE),
                Set.of("Benefit 1"),
                Set.of("Prerequisite 1"));
        course1.setSections(sections1);
        course2.setSections(sections2);
        course3.setSections(sections3);
        course4.setSections(sections4);
        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);
        courseRepository.save(course4);
    }

    @Test
    void testFindAll_withPagination() {
        // Arrange: Create a pageable request (page 0, size 2)
        Pageable pageable = PageRequest.of(0, 2);

        // Act: Retrieve a page of courses
        Page<Course> page = courseRepository.findAll(pageable);

        // Assert: Verify that the page contains the expected number of courses
        assertThat(page.getContent()).hasSize(2); // Expect 2 results per page
        assertThat(page.getTotalElements()).isEqualTo(4); // Total number of courses is 4
        assertThat(page.getTotalPages()).isEqualTo(2); // Total pages should be 2
        assertThat(page.getContent()).extracting(Course::getTitle)
                .containsExactly("Course 1", "Course 2"); // Verify contents of the page
    }

    @Test
    void testFindAll_withPaginationAndSorting() {
        // Arrange: Create a pageable request with sorting by title in descending order
        Pageable pageable = PageRequest.of(0, 2, Sort.by("title").descending());

        // Act: Retrieve a page of courses with sorting
        Page<Course> page = courseRepository.findAll(pageable);

        // Assert: Verify that the courses are returned in sorted order
        assertThat(page.getContent()).hasSize(2); // Expect 2 results per page
        assertThat(page.getContent()).extracting(Course::getTitle)
                .containsExactly("Course 4", "Course 3"); // Verify the sorting order
    }


    @Test
    void testFindAll_onSecondPage() {
        // Arrange: Create a pageable request for the second page (page 1, size 2)
        Pageable pageable = PageRequest.of(1, 2);

        // Act: Retrieve the second page of courses
        Page<Course> page = courseRepository.findAll(pageable);

        // Assert: Verify that the second page contains the remaining courses
        assertThat(page.getContent()).hasSize(2); // Expect 2 results on the second page
        assertThat(page.getContent()).extracting(Course::getTitle)
                .containsExactly("Course 3", "Course 4"); // Verify the second page's contents
    }

}