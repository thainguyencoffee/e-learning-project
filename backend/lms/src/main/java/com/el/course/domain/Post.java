package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Table("post")
@Getter
public class Post {
    @Id
    private Long id;
    private String content;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private UserInfo info;
    private Set<String> photoUrls;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    @JsonIgnore
    private boolean deleted = false;

    public Post(String content, UserInfo info, Set<String> photoUrls) {
        final UrlValidator URL_VALIDATOR = new UrlValidator();

        if (content == null || content.isBlank()) {
            throw new InputInvalidException("Content of the post is required");
        }
        if (info == null) {
            throw new InputInvalidException("User info is required");
        }

        if (!photoUrls.stream().allMatch(URL_VALIDATOR::isValid)) {
            throw new InputInvalidException("Invalid photo URL");
        }

        this.content = content;
        this.info = info;
        this.photoUrls = photoUrls;

        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Post() {
    }

    protected void updateInfo(String newContent, Set<String> newPhotoUrls) {
        final UrlValidator URL_VALIDATOR = new UrlValidator();

        if (newContent == null || newContent.isBlank()) {
            throw new InputInvalidException("Content of the post is required");
        }

        if (newPhotoUrls != null && !newPhotoUrls.stream().allMatch(URL_VALIDATOR::isValid)) {
            throw new InputInvalidException("Invalid photo URL");
        }

        this.content = newContent;
        this.photoUrls = newPhotoUrls;
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

}
