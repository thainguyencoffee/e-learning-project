package com.el.course.domain;

import com.el.common.MoneyUtils;
import com.el.common.exception.ResourceNotFoundException;
import com.el.common.exception.InputInvalidException;
import com.el.course.application.dto.QuizCalculationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Table("course")
@ToString
public class Course extends AbstractAggregateRoot<Course> {
    @Id
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String description;
    private final Language language;
    private Set<Language> subtitles;
    private Set<String> benefits;
    private Set<String> prerequisites;

    @MappedCollection(idColumn = "course")
    private Set<CourseSection> sections = new HashSet<>();
    private MonetaryAmount price;
    private Boolean published;
    private LocalDateTime publishedDate;
    private Boolean unpublished;
    private String teacher;
    private String approvedBy;
    private Set<CourseRequest> courseRequests = new HashSet<>();

    @MappedCollection(idColumn = "course")
    private Set<Post> posts = new HashSet<>();

    @MappedCollection(idColumn = "course")
    private Set<Review> reviews = new HashSet<>();

    @JsonIgnore
    private boolean deleted;
    @Version
    private int version;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public Course(
            String title,
            String description,
            String thumbnailUrl,
            Set<String> benefits,
            Language language,
            Set<String> prerequisites,
            Set<Language> subtitles,
            String teacher
    ) {

        if (title == null || title.isEmpty()) {
            throw new InputInvalidException("Title must not be empty.");
        }

        if (language == null) {
            throw new InputInvalidException("Language must not be null.");
        }

        if (subtitles != null && subtitles.contains(language)) {
            throw new InputInvalidException("Subtitles must not contain the same language as the course.");
        }

        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.language = language;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
        this.subtitles = subtitles;
        this.teacher = teacher;
        deleted = false;
        published = false;
        unpublished = false;
    }

    public void updateInfo(
            String title,
            String description,
            String thumbnailUrl,
            Set<String> benefits,
            Set<String> prerequisites,
            Set<Language> subtitles
    ) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a published course.");
        }

        if (title == null || title.isEmpty()) {
            throw new InputInvalidException("Title must not be empty.");
        }

        if (subtitles != null && subtitles.contains(this.language)) {
            throw new InputInvalidException("Subtitles must not contain the same language as the course.");
        }

        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
        this.subtitles = subtitles;
    }

    public void changePrice(MonetaryAmount newPrice) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot change price of a published course.");
        }
        if (validSections()) {
            throw new InputInvalidException("Cannot change price of a course without sections or section without lessons");
        }
        MoneyUtils.checkValidPrice(newPrice);
        this.price = newPrice;
    }

    private boolean validSections() {
        return this.sections.isEmpty() || !this.sections.stream().allMatch(CourseSection::hasLessons);
    }


    public void assignTeacher(String teacher) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot assign a teacher to a published course.");
        }

        if (teacher == null || teacher.isEmpty()) {
            throw new InputInvalidException("Teacher must not be empty.");
        }

        this.teacher = teacher;
    }

    public void requestPublish(CourseRequest courseRequest) {
        // if this course is already published, throw exception
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot request publish for a published course.");
        }
        if (validSections() || this.getTeacher() == null) {
            throw new InputInvalidException("Cannot publish a course without sections or teacher.");
        }
        if (courseRequest.getType() != RequestType.PUBLISH) {
            throw new InputInvalidException("Request type invalid.");
        }
        if (isAnyRequestsUnresolved()) {
            throw new InputInvalidException("Cannot request publish while there are unresolved requests.");
        }
        if (!courseRequest.getRequestedBy().equals(teacher)) {
            throw new InputInvalidException("Only the teacher can request publish.");
        }

        courseRequests.add(courseRequest);
    }

    public void approvePublish(Long courseRequestId, String approvedBy, String approveMessage) {
        if (approveMessage.isBlank()) {
            throw new InputInvalidException("Approve message must not be blank.");
        }
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot approve publish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(approvedBy)) {
            throw new InputInvalidException("Teacher cannot approve their own course.");
        }
        courseRequests.stream().filter(courseRequest -> courseRequest.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.approve(approvedBy, approveMessage);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
        this.approvedBy = approvedBy;
        this.published = true;
        this.publishedDate = LocalDateTime.now();
        this.unpublished = false;

        registerEvent(new CoursePublishedEvent(this.id, this.teacher));
    }

    public record CoursePublishedEvent(Long courseId, String teacher) {
    }

    public void rejectPublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot reject publish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(rejectedBy)) {
            throw new InputInvalidException("Teacher cannot reject their own course.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.reject(rejectedBy, rejectReason);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
    }

    public void requestUnpublish(CourseRequest courseRequest) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot request unpublish for an unpublished course.");
        }
        if (courseRequest.getType() != RequestType.UNPUBLISH) {
            throw new InputInvalidException("Request type invalid.");
        }
        if (isAnyRequestsUnresolved()) {
            throw new InputInvalidException("Cannot request unpublish while there are unresolved requests.");
        }
        if (!courseRequest.getRequestedBy().equals(teacher)) {
            throw new InputInvalidException("Only the teacher can request unpublish.");
        }

        courseRequests.add(courseRequest);
    }

    public void approveUnpublish(Long courseRequestId, String approvedBy, String approveMessage) {
        if (approveMessage.isBlank()) {
            throw new InputInvalidException("Approve message must not be blank.");
        }
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot approve unpublish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(approvedBy)) {
            throw new InputInvalidException("Teacher cannot approve unpublish their own course.");
        }
        if (!approvedBy.equals(this.approvedBy)) {
            throw new InputInvalidException("Only the approver can approve unpublish.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.approve(approvedBy, approveMessage);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
        this.approvedBy = approvedBy;
        this.published = false;
        this.unpublished = true;
    }

    public void rejectUnpublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot reject unpublish for an unpublished course.");
        }
        if (isTeacherRequestingSelfApproval(rejectedBy)) {
            throw new InputInvalidException("Teacher cannot reject unpublish their own course.");
        }
        if (!rejectedBy.equals(this.approvedBy)) {
            throw new InputInvalidException("Only the approver can reject unpublish.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.reject(rejectedBy, rejectReason);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
    }

    private boolean isAnyRequestsUnresolved() {
        return courseRequests.stream().anyMatch(CourseRequest::isUnresolved);
    }

    private boolean isTeacherRequestingSelfApproval(String approvedBy) {
        return approvedBy.equals(this.getTeacher());
    }


    public void addSection(CourseSection section) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a section to a published course.");
        }

        if (section == null) {
            throw new InputInvalidException("Section must not be null.");
        }

        // Check valid section
        // Now when addSection is called, that section only has title
        // Don't support add section that has whole attributes
        if (section.hasLessons())
            throw new InputInvalidException("Cannot add a section that has lessons.");

        if (this.sections.stream().anyMatch(existingSection -> existingSection.getTitle().equals(section.getTitle()))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

        int orderIndexLast = 1;
        if (!this.sections.isEmpty()) {
            orderIndexLast = this.sections.stream().mapToInt(CourseSection::getOrderIndex).max().getAsInt() + 1;
        }
        section.setOrderIndex(orderIndexLast);
        this.sections.add(section);
    }

    public void updateSection(Long sectionId, String title) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a section in a published course.");
        }

        CourseSection existingSection = findSectionById(sectionId);

        if (this.sections.stream().anyMatch(section -> section.getTitle().equals(title))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

        existingSection.updateInfo(title);
    }

    public void removeSection(Long sectionId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot remove a section from a published course.");
        }
        CourseSection courseSection = findSectionById(sectionId);
        this.sections.remove(courseSection);
    }

    public void addLessonToSection(Long sectionId, Lesson lesson) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.addLesson(lesson);
    }

    public void updateLessonInSection(Long sectionId, Long lessonId, Lesson updatedLesson) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.updateLesson(lessonId, updatedLesson);
    }

    public void removeLessonFromSection(Long sectionId, Long lessonId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.removeLesson(lessonId);
    }

    public void delete() {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot delete a published course.");
        }
        if (this.deleted) {
            throw new InputInvalidException("Course is already deleted.");
        }
        this.deleted = true;
    }

    public void deleteForce() {
        if (!this.deleted) {
            throw new InputInvalidException("Course is not deleted.");
        }
    }

    public void restore() {
        if (!this.deleted) {
            throw new InputInvalidException("Course is not deleted.");
        }
        this.deleted = false;
    }

    public void addPost(Post post) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a post to an unpublished course.");
        }

        this.posts.add(post);
    }

    public void updatePost(Long postId, String newContent, Set<String> newAttachmentUrls) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a post in an unpublished course.");
        }

        Post post = findPostById(postId);
        post.updateInfo(newContent, newAttachmentUrls);
    }

    public void deletePost(Long postId) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot remove a post from an unpublished course.");
        }
        Post post = findPostById(postId);
        post.delete();
    }

    public void restorePost(Long postId) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot restore a post in an unpublished course.");
        }
        Post post = findPostById(postId);
        post.restore();
    }

    public void forceDeletePost(Long postId) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot force delete a post in an unpublished course.");
        }
        Post post = findPostById(postId);
        if (!post.isDeleted())
            throw new InputInvalidException("Post is not deleted.");
        this.posts.remove(post);
    }

    public void addCommentToPost(Long postId, Comment comment) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a comment to an unpublished course.");
        }
        Post post = findPostById(postId);
        post.addComment(comment);
    }

    public void deleteCommentFromPost(Long postId, Long commentId, String username) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot delete a comment from an unpublished course.");
        }
        Post post = findPostById(postId);
        post.deleteComment(commentId, username);
    }

    public void addEmotionToPost(Long postId, Emotion emotion) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add an emotion to an unpublished course.");
        }
        Post post = findPostById(postId);
        post.addEmotion(emotion);
    }

    public void addQuizToSection(Long sectionId, Quiz quiz) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a quiz to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.findLessonById(quiz.getAfterLessonId());
        section.addQuiz(quiz);
    }

    public void addQuestionToQuizInSection(Long sectionId, Long quizId, Question question) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a question to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.addQuestionToQuiz(quizId, question);
    }

    public void updateQuestionInQuizInSection(Long sectionId, Long quizId, Long questionId, Question updatedQuestion) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a question in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.updateQuestionInQuiz(quizId, questionId, updatedQuestion);
    }

    public void deleteQuestionFromQuizInSection(Long sectionId, Long quizId, Long questionId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot delete a question from a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.deleteQuestionFromQuiz(quizId, questionId);
    }

    public void updateQuizInSection(Long sectionId, Long quizId, String newTitle, String newDescription, Integer newPassScorePercent) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a quiz in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.updateQuiz(quizId, newTitle, newDescription, newPassScorePercent);
    }

    public void deleteQuizFromSection(Long sectionId, Long quizId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot delete a quiz from a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.deleteQuiz(quizId);
    }

    public void restoreQuizInSection(Long sectionId, Long quizId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot restore a quiz in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.restoreQuiz(quizId);
    }

    public void forceDeleteQuizFromSection(Long sectionId, Long quizId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot force delete a quiz in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.forceDeleteQuiz(quizId);
    }

    public QuizCalculationResult calculateQuiz(long quizId, Map<Long, Object> userAnswers) {
        Quiz quiz = findQuizById(quizId);
        Integer score = quiz.calculateScore(userAnswers);
        Boolean passed = quiz.isPassed(score);
        return new QuizCalculationResult(score, passed);
    }

    private Quiz findQuizById(Long quizId) {
        return this.sections.stream()
                .flatMap(section -> section.getQuizzes().stream())
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Post findPostById(Long postId) {
        return this.posts.stream()
                .filter(post -> post.getId().equals(postId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }


    public boolean isNotPublishedAndDeleted() {
        return !published && !deleted;
    }

    public boolean isPublishedAndNotDeleted() {
        return published && !deleted;
    }

    private CourseSection findSectionById(Long sectionId) {
        return this.sections.stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Map<Long, String> getLessonIds() {
        return this.sections.stream()
                .flatMap(section -> section.getLessons().stream())
                .collect(HashMap::new, (map, lesson) -> map.put(lesson.getId(), lesson.getTitle()), Map::putAll);
    }


    public void updateComment(Long postId, Long commentId, String content, Set<String> strings) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a comment in an unpublished course.");
        }
        Post post = findPostById(postId);
        post.updateComment(commentId, content, strings);
    }

    public void addReview(Review review) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a review to an unpublished course.");
        }
        this.reviews.stream()
                .filter(r -> r.getUsername().equals(review.getUsername()))
                .findFirst()
                .ifPresent(r -> {
                    throw new InputInvalidException("User already reviewed this course.");
                });
        this.reviews.add(review);
        registerEvent(new CourseReviewedEvent(this.id, review.getUsername()));
    }

    public record CourseReviewedEvent(Long courseId, String username) {
    }

    public double getAverageRating() {
        if (this.reviews.isEmpty()) {
            return 0;
        }
        return this.reviews.stream().mapToDouble(Review::getRating).sum() / this.reviews.size();
    }

}
