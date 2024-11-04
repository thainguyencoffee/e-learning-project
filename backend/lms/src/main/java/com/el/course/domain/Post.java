package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table("post")
@Getter
public class Post {
    @Id
    private Long id;
    private String content;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private UserInfo info;
    private Set<String> attachmentUrls;
    @MappedCollection(idColumn = "post")
    private Set<Comment> comments = new HashSet<>();
    @MappedCollection(idColumn = "post")
    private Set<Emotion> emotions = new HashSet<>();
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    @JsonIgnore
    private boolean deleted = false;

    public Post(String content, UserInfo info, Set<String> attachmentUrls) {
        final UrlValidator URL_VALIDATOR = new UrlValidator();

        if (content == null || content.isBlank()) {
            throw new InputInvalidException("Content of the post is required");
        }
        if (info == null) {
            throw new InputInvalidException("User info is required");
        }

        if (!attachmentUrls.stream().allMatch(URL_VALIDATOR::isValid)) {
            throw new InputInvalidException("Invalid photo URL");
        }

        this.content = content;
        this.info = info;
        this.attachmentUrls = attachmentUrls;

        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Post() {
    }

    protected void updateInfo(String newContent, Set<String> newAttachmentUrls) {
        final UrlValidator URL_VALIDATOR = new UrlValidator();

        if (newContent == null || newContent.isBlank()) {
            throw new InputInvalidException("Content of the post is required");
        }

        if (newAttachmentUrls != null && !newAttachmentUrls.stream().allMatch(URL_VALIDATOR::isValid)) {
            throw new InputInvalidException("Invalid attachment urls");
        }

        this.content = newContent;
        this.attachmentUrls = newAttachmentUrls;
        this.lastModifiedDate = LocalDateTime.now();
    }

    protected void delete() {
        if (this.deleted) {
            throw new InputInvalidException("Post is already deleted");
        }
        this.deleted = true;
    }

    public void restore() {
        if (!this.deleted) {
            throw new InputInvalidException("Post is not deleted");
        }
        this.deleted = false;
    }

    public void addComment(Comment comment) {
        if (isDeleted()) {
            throw new InputInvalidException("Post is deleted");
        }
        comments.add(comment);
    }

    public void addEmotion(Emotion emotion) {
        if (isDeleted()) {
            throw new InputInvalidException("Post is deleted");
        }
        emotions.add(emotion);
    }

    public void deleteComment(Long commentId) {
        if (isDeleted()) {
            throw new InputInvalidException("Post is deleted");
        }
        comments.removeIf(comment -> comment.getId().equals(commentId));
    }

    public void deleteEmotion(Long emotionId) {
        if (isDeleted()) {
            throw new InputInvalidException("Post is deleted");
        }
        emotions.removeIf(emotion -> emotion.getId().equals(emotionId));
    }

    public void updateComment(Long commentId, String newContent, Set<String> newAttachmentUrls) {
        if (isDeleted()) {
            throw new InputInvalidException("Post is deleted");
        }
        Comment comment = findCommentById(commentId);
        comment.updateInfo(newContent, newAttachmentUrls);
    }

    private Comment findCommentById(Long commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

}
