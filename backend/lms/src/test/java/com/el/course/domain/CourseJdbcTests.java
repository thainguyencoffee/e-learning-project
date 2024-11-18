package com.el.course.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.config.DataAuditConfig;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class CourseJdbcTests {

    @Autowired
    private CourseRepository courseRepository;

    private Course courseWithSections;

    @BeforeEach
    void setUp() {
        courseWithSections = TestFactory.createCourseWithSections();
        courseWithSections.changePrice(Money.of(23000, Currencies.VND));
    }

    @Test
    public void testSaveCourse() {
        Course savedCourse = courseRepository.save(courseWithSections);

        assertNotNull(savedCourse.getId());
        assertEquals(courseWithSections.getTitle(), savedCourse.getTitle());
        assertEquals(courseWithSections.getTeacher(), savedCourse.getTeacher());
    }

    @Test
    public void testFindById() {
        Course savedCourse = courseRepository.save(courseWithSections);

        Optional<Course> retrievedCourse = courseRepository.findById(savedCourse.getId());

        assertTrue(retrievedCourse.isPresent());
        assertEquals(savedCourse.getTitle(), retrievedCourse.get().getTitle());
    }

    @Test
    public void testFindAllCourses() {
        courseRepository.save(courseWithSections);

        Page<Course> coursesPage = courseRepository.findAll(PageRequest.of(0, 10));

        assertEquals(1, coursesPage.getTotalElements());
        assertEquals(courseWithSections.getTitle(), coursesPage.getContent().get(0).getTitle());
    }

    @Test
    public void testDeleteCourse() {
        Course savedCourse = courseRepository.save(courseWithSections);

        // Xóa course
        savedCourse.delete();
        courseRepository.save(savedCourse);

        // Tìm course theo ID đã xóa
        Optional<Course> deletedCourse = courseRepository.findByIdAndDeleted(savedCourse.getId(), true);
        assertTrue(deletedCourse.isPresent());
        assertTrue(deletedCourse.get().isDeleted());
    }

    @Test
    void testAddPost() {
        Course savedCourse = spy(courseRepository.save(courseWithSections));
        Post post = new Post("Quisque vitae rutrum turpis. Mauris non mauris purus. Mauris consequat nunc bibendum aliquam pharetra.",
                new UserInfo("thai", "nguyen", "user"),
                Set.of("http://placekitten.com/1", "http://placekitten.com/2"));

        when(savedCourse.isNotPublishedAndDeleted()).thenReturn(false);

        savedCourse.addPost(post);
        courseRepository.save(savedCourse);

        Optional<Post> postOptional = courseRepository.findPostByCourseIdAndPostIdAndDeleted(
                courseWithSections.getId(), post.getId(), false);
        List<Post> postsWithCorrectTeacher = courseRepository.findAllPostsByCourseIdAndTeacherAndDeleted(
                courseWithSections.getId(), TestFactory.teacher, false, 0, 10);
        List<Post> postsWithIncorrectTeacher = courseRepository.findAllPostsByCourseIdAndTeacherAndDeleted(
                courseWithSections.getId(), "somebody", false, 0, 10);

        assertEquals(1, postsWithCorrectTeacher.size());
        assertEquals(0, postsWithIncorrectTeacher.size());
        assertTrue(postOptional.isPresent());
        assertEquals(post.getContent(), postOptional.get().getContent());
    }

    @Test
    void testAddComment() {
        Course savedCourse = spy(courseRepository.save(courseWithSections));
        Post post = new Post("Quisque vitae rutrum turpis. Mauris non mauris purus. Mauris consequat nunc bibendum aliquam pharetra.",
                new UserInfo("thai", "nguyen", "user"),
                Set.of("http://placekitten.com/1", "http://placekitten.com/2"));
        Comment comment = new Comment("Quisque vitae rutrum turpis. Mauris non mauris purus. Mauris consequat nunc bibendum aliquam pharetra.",
                new UserInfo("thai", "nguyen", "user"),
                Set.of("http://placekitten.com/1", "http://placekitten.com/2"));

        when(savedCourse.isNotPublishedAndDeleted()).thenReturn(false);

        savedCourse.addPost(post);
        courseRepository.save(savedCourse);

        post.addComment(comment);
        courseRepository.save(savedCourse);

        List<Comment> comments = courseRepository.findAllCommentsByCourseIdAndPostId(
                courseWithSections.getId(), post.getId(), 0, 10);
        List<Comment> commentsByTeacher = courseRepository.findAllCommentsByCourseIdAndPostIdAndTeacher(
                courseWithSections.getId(), post.getId(), TestFactory.teacher, 0, 10);
        List<Comment> commentsByAnotherTeacher = courseRepository.findAllCommentsByCourseIdAndPostIdAndTeacher(
                courseWithSections.getId(), post.getId(), "somebody", 0, 10);

        assertEquals(1, comments.size());
        assertEquals(1, commentsByTeacher.size());
        assertEquals(0, commentsByAnotherTeacher.size());
        assertEquals(comment.getContent(), comments.get(0).getContent());
        assertEquals(comment.getAttachmentUrls(), comments.get(0).getAttachmentUrls());
    }

    @Test
    void testAddEmotion() {
        Course savedCourse = spy(courseRepository.save(courseWithSections));
        Post post = new Post("Quisque vitae rutrum turpis. Mauris non mauris purus. Mauris consequat nunc bibendum aliquam pharetra.",
                new UserInfo("thai", "nguyen", "user"),
                Set.of("http://placekitten.com/1", "http://placekitten.com/2"));

        when(savedCourse.isNotPublishedAndDeleted()).thenReturn(false);

        savedCourse.addPost(post);
        courseRepository.save(savedCourse);

        post.addEmotion(new Emotion("nguyen"));
        post.addEmotion(new Emotion("dung"));
        post.addEmotion(new Emotion("nam"));
        courseRepository.save(savedCourse);

        Optional<Post> postOptional = courseRepository.findPostByCourseIdAndPostIdAndDeleted(
                courseWithSections.getId(), post.getId(), false);

        assertTrue(postOptional.isPresent());
        assertEquals(3, postOptional.get().getEmotions().size());
    }

    @Test
    void testAddQuizToSection() {
        Course savedCourse = spy(courseRepository.save(courseWithSections));
        CourseSection section = savedCourse.getSections().iterator().next();
        Long lessonId = section.getLessons().iterator().next().getId();

        Quiz quiz = new Quiz("Quiz 1", "Quiz description", lessonId, 100);

        when(savedCourse.isNotPublishedAndDeleted()).thenReturn(true);

        savedCourse.addQuizToSection(section.getId(), quiz);
        courseRepository.save(savedCourse);

        Optional<Course> retrievedCourse = courseRepository.findById(savedCourse.getId());

        assertTrue(retrievedCourse.isPresent());
        assertNotNull(quiz.getId());

        // Test add question to quiz
        Question question = new Question("Question 1", QuestionType.SINGLE_CHOICE, 2,
                Set.of(new AnswerOption("Answer 1", true), new AnswerOption("Answer 2", false)));
        quiz.addQuestion(question);
        courseRepository.save(savedCourse);

        assertNotNull(question.getId());
    }


}
