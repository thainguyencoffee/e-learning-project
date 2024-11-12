package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Table("comment")
@Getter
public class Comment {
    @Id
    private Long id;
    private String content;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private UserInfo info;
    private Set<String> attachmentUrls;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public Comment(String content, UserInfo info, Set<String> attachmentUrls) {
        this.content = content;
        this.info = info;
        this.attachmentUrls = attachmentUrls;

        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Comment() {}

    public void updateInfo(String newContent, Set<String> newAttachmentUrls) {

        if (content == null || content.isBlank()) {
            throw new InputInvalidException("Content of the comment is required");
        }

        this.content = newContent;
        this.attachmentUrls = newAttachmentUrls;
        this.lastModifiedDate = LocalDateTime.now();
    }

}
