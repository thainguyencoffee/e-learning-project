package com.elearning.course.application;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.*;
import com.elearning.discount.application.DiscountInvalidDateException;
import com.elearning.discount.application.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository repository;
    private final DiscountService discountService;

    public List<Course> findAll(Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }

    public Course findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Course.class, id));
    }

    public boolean isItMyCourse(Long courseId, String createdBy) {
        return repository.existsCourseByIdAndCreatedBy(courseId, createdBy);
    }

    @Transactional
    public Course createCourse(CourseRequestDTO courseRequestDTO) {
        Course course = courseRequestDTO.toCourse();
        if (courseRequestDTO.discountId() != null) {
            handleDiscountCalculate(course, courseRequestDTO.discountId());
        }
        if (courseRequestDTO.sections() != null ) {
            for (CourseSectionDTO sectionDTO : courseRequestDTO.sections()) {
                CourseSection newSection = createSectionWithLessons(sectionDTO);
                course.addSection(newSection);
            }
        }
        return repository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, CourseRequestDTO courseRequestDTO) {
        Course course = findById(id);
        course.updateInfo(courseRequestDTO.toCourse());

        // Chỉ xử lý sections nếu không phải là null
        if (courseRequestDTO.sections() != null) {
            // Xử lý cập nhật các course sections
            for (CourseSectionDTO sectionDTO : courseRequestDTO.sections()) {
                CourseSection section = course.findSectionById(sectionDTO.id());
                if (section != null) {
                    // Update existing section
                    updateSectionWithLessons(section, sectionDTO);
                } else {
                    // new section
                    CourseSection newSection = createSectionWithLessons(sectionDTO);
                    course.addSection(newSection);
                }
            }

            // Loại bỏ những sections không còn tồn tại
            course.removeSectionsOrphan(sectionDTOsToSectionIds(courseRequestDTO.sections()));
        }

        // Update discount
        if (courseRequestDTO.discountId() != null) {
            handleDiscountCalculate(course, courseRequestDTO.discountId());
        } else {
            course.setDiscountId(null);
            course.setDiscountedPrice(course.getFinalPrice());
        }

        // Lưu lại course sau khi cập nhật
        return repository.save(course);
    }


    private void updateSectionWithLessons(CourseSection section, CourseSectionDTO sectionDTO) {
        section.updateInfo(sectionDTO.title());

        for (LessonDTO lessonDTO : sectionDTO.lessons()) {
            Lesson lesson = section.findLessonById(lessonDTO.id());
            if (lesson != null) {
                lesson.updateInfo(lessonDTO.title(), Lesson.Type.valueOf(lessonDTO.type()), lessonDTO.link());
            } else {
                // Create new lesson
                Lesson newLesson = new Lesson(lessonDTO.title(), Lesson.Type.valueOf(lessonDTO.type()), lessonDTO.link());
                section.addLesson(newLesson);
            }
        }

        section.removeLessonsOrphan(lessonDTOsToLessonIds(sectionDTO.lessons()));
    }

    private CourseSection createSectionWithLessons(CourseSectionDTO sectionDTO) {
        CourseSection newSection = new CourseSection(sectionDTO.title());

        for (LessonDTO lessonDTO : sectionDTO.lessons()) {
            Lesson newLesson = new Lesson(lessonDTO.title(), Lesson.Type.valueOf(lessonDTO.type()), lessonDTO.link());
            newSection.addLesson(newLesson);
        }

        return newSection;
    }

    private void handleDiscountCalculate(Course course, Long discountId) {
        try {
            MonetaryAmount discountPrice = discountService.calculateDiscountForCourse(discountId, course.getPrice());
            course.setDiscountId(discountId);
            course.applyDiscount(discountPrice);
        } catch (ResourceNotFoundException | DiscountInvalidDateException e) {
            log.warn("Discount {} is invalid or not found, course will be saved without discount", discountId);
        }
    }

    private Set<Long> sectionDTOsToSectionIds(Set<CourseSectionDTO> sectionDTOs) {
        return sectionDTOs.stream()
                .map(CourseSectionDTO::id)
                .filter(Objects::nonNull) // Lọc ra các section có ID (không phải section mới)
                .collect(Collectors.toSet());
    }

    private Set<Long> lessonDTOsToLessonIds(Set<LessonDTO> lessonDTOs) {
        return lessonDTOs.stream()
                .map(LessonDTO::id)
                .filter(Objects::nonNull) // Lọc ra các lesson có ID (không phải lesson mới)
                .collect(Collectors.toSet());
    }

    @Transactional
    public boolean deleteCourse(Long id) {
        Course course = findById(id);
        if (!course.getSections().isEmpty()) {
            throw new CourseHasSectionsException("Cannot delete course with id " + id + " because it has course sections");
        }

        repository.delete(course);
        return true;
    }

}
