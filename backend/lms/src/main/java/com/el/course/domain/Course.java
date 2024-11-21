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
import java.util.stream.Collectors;

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
    private LocalDateTime unpublishedDate;
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
        if (isPublishedAndNotUnpublishedOrDelete()) {
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
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot change price of a published course.");
        }
        if (validSections()) {
            throw new InputInvalidException("Cannot change price of a course without sections or section without lessons");
        }
        MoneyUtils.checkValidPrice(newPrice);
        this.price = newPrice;
    }

    private boolean validSections() {
        return this.sections.isEmpty() || !this.sections.stream().allMatch(CourseSection::hasLessons); // update later
    }

    public void assignTeacher(String teacher) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot assign a teacher to a published course.");
        }

        if (teacher == null || teacher.isEmpty()) {
            throw new InputInvalidException("Teacher must not be empty.");
        }

        this.teacher = teacher;
    }

    public void requestPublish(CourseRequest courseRequest) {
        // if this course is already published, throw exception
        if (isPublishedAndNotUnpublishedOrDelete()) {
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
        if (isPublishedAndNotUnpublishedOrDelete()) {
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

        // All old students paid for this course can see the new sections, but it's optional
        this.sections.stream().filter(section -> !section.getPublished()).forEach(CourseSection::markAsSectionPublished);

        registerEvent(new CoursePublishedEvent(this.id, this.teacher));
    }

    public void rejectPublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (isPublishedAndNotUnpublishedOrDelete()) {
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
        if (!isPublishedAndNotUnpublishedOrDelete()) {
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
        if (!isPublishedAndNotUnpublishedOrDelete()) {
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
//        this.published = false;
        this.unpublished = true;
        this.unpublishedDate = LocalDateTime.now();
    }

    public void rejectUnpublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (!isPublishedAndNotUnpublishedOrDelete()) {
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
        if (isPublishedAndNotUnpublishedOrDelete()) {
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

        if (unpublished) {
            // add Section in unpublished mode
            section.markAsSectionUnpublished();
            this.sections.add(section);
        } else {
            this.sections.add(section);
        }
    }

    public void updateSection(Long sectionId, String title) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot update a section in a published course.");
        }

        CourseSection existingSection = findSectionById(sectionId);

        if (this.sections.stream().anyMatch(section -> section.getTitle().equals(title))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }
        checkConflictBetweenUnpublishedAndPublishedForSection(existingSection);

        existingSection.updateInfo(title);
    }

    public void removeSection(Long sectionId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot remove a section from a published course.");
        }
        CourseSection courseSection = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(courseSection);
        this.sections.remove(courseSection);
    }

    public void addLessonToSection(Long sectionId, Lesson lesson) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.addLesson(lesson);
    }

    public void updateLessonInSection(Long sectionId, Long lessonId, Lesson updatedLesson) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.updateLesson(lessonId, updatedLesson);
    }

    public void removeLessonFromSection(Long sectionId, Long lessonId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.removeLesson(lessonId);
    }

    public void delete() {
        if (isPublishedAndNotUnpublishedOrDelete()) {
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
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot add a post to an unpublished course.");
        }

        this.posts.add(post);
    }

    public void updatePost(Long postId, String newContent, Set<String> newAttachmentUrls) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot update a post in an unpublished course.");
        }

        Post post = findPostById(postId);
        post.updateInfo(newContent, newAttachmentUrls);
    }

    public void deletePost(Long postId) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot remove a post from an unpublished course.");
        }
        Post post = findPostById(postId);
        post.delete();
    }

    public void restorePost(Long postId) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot restore a post in an unpublished course.");
        }
        Post post = findPostById(postId);
        post.restore();
    }

    public void forceDeletePost(Long postId) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot force delete a post in an unpublished course.");
        }
        Post post = findPostById(postId);
        if (!post.isDeleted())
            throw new InputInvalidException("Post is not deleted.");
        this.posts.remove(post);
    }

    public void addCommentToPost(Long postId, Comment comment) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot add a comment to an unpublished course.");
        }
        Post post = findPostById(postId);
        post.addComment(comment);
    }

    public void updateComment(Long postId, Long commentId, String content, Set<String> strings) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot update a comment in an unpublished course.");
        }
        Post post = findPostById(postId);
        post.updateComment(commentId, content, strings);
    }

    public void deleteCommentFromPost(Long postId, Long commentId, String username) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot delete a comment from an unpublished course.");
        }
        Post post = findPostById(postId);
        post.deleteComment(commentId, username);
    }

    public void addEmotionToPost(Long postId, Emotion emotion) {
        if (isNotPublishedOrDeleted()) {
            throw new InputInvalidException("Cannot add an emotion to an unpublished course.");
        }
        Post post = findPostById(postId);
        post.addEmotion(emotion);
    }

    public void addQuizToSection(Long sectionId, Quiz quiz) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot add a quiz to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.findLessonById(quiz.getAfterLessonId());
        section.addQuiz(quiz);
    }

    public void addQuestionToQuizInSection(Long sectionId, Long quizId, Question question) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot add a question to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.addQuestionToQuiz(quizId, question);
    }

    public void updateQuestionInQuizInSection(Long sectionId, Long quizId, Long questionId, Question updatedQuestion) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot update a question in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.updateQuestionInQuiz(quizId, questionId, updatedQuestion);
    }

    public void deleteQuestionFromQuizInSection(Long sectionId, Long quizId, Long questionId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot delete a question from a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.deleteQuestionFromQuiz(quizId, questionId);
    }

    public void updateQuizInSection(Long sectionId, Long quizId, String newTitle, String newDescription, Integer newPassScorePercent) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot update a quiz in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.updateQuiz(quizId, newTitle, newDescription, newPassScorePercent);
    }

    public void deleteQuizFromSection(Long sectionId, Long quizId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot delete a quiz from a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.deleteQuiz(quizId);
    }

    public void restoreQuizInSection(Long sectionId, Long quizId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
            throw new InputInvalidException("Cannot restore a quiz in a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        checkConflictBetweenUnpublishedAndPublishedForSection(section);
        section.restoreQuiz(quizId);
    }

    public void forceDeleteQuizFromSection(Long sectionId, Long quizId) {
        if (isPublishedAndNotUnpublishedOrDelete()) {
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

    public void addReview(Review review) {
        if (isNotPublishedOrDeleted()) {
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

    public double getAverageRating() {
        if (this.reviews.isEmpty()) {
            return 0;
        }
        return this.reviews.stream().mapToDouble(Review::getRating).sum() / this.reviews.size();
    }

    public Set<CourseSection> getSectionForPublished() {
        return this.sections.stream().filter(CourseSection::getPublished).collect(Collectors.toSet());
    }

    public Integer getNumberOfQuizzes() {
        return this.sections.stream()
                .mapToInt(section -> section.getQuizzes().size())
                .sum();
    }

    /*Condition methods*/

    private void checkConflictBetweenUnpublishedAndPublishedForSection(CourseSection courseSection) {
        if (unpublished && courseSection.getPublished())
            throw new InputInvalidException("You cannot update old section in unpublished mode.");
    }

    public boolean isNotPublishedOrDeleted() {
        return !published || deleted;
    }

    public boolean isPublishedAndNotUnpublishedOrDelete() {
        return published && !unpublished || deleted;
    }

    /*Read methods*/
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

    private CourseSection findSectionById(Long sectionId) {
        return this.sections.stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Map<Long, String> getLessonIdAndTitleMap() {
        return this.sections.stream()
                .flatMap(section -> section.getLessons().stream())
                .collect(HashMap::new, (map, lesson) -> map.put(lesson.getId(), lesson.getTitle()), Map::putAll);
    }

    /*Events*/
    public record CoursePublishedEvent(Long courseId, String teacher) {}

    public record CourseReviewedEvent(Long courseId, String username) {}

}
