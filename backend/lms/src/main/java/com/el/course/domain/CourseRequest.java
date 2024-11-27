package com.el.course.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("course_request")
@Getter
public class CourseRequest  {

    @Id
    private Long id;
    private RequestType type;
    private RequestStatus status;
    private Boolean resolved;
    private String resolvedBy;
    private String requestedBy;
    private String message;
    private String rejectReason;
    private String approveMessage;
    private LocalDateTime requestedDate;
    private LocalDateTime resolvedDate;

    public CourseRequest(RequestType type, String message, String requestedBy) {
        if (type == null) {
            throw new InputInvalidException("Request type must not be null.");
        }
        if (message == null || message.isEmpty()) {
            throw new InputInvalidException("Request message must not be null or empty.");
        }
        if (requestedBy == null || requestedBy.isEmpty()) {
            throw new InputInvalidException("Requester must not be null or empty.");
        }

        this.type = type;
        this.message = message;
        this.requestedBy = requestedBy;
        this.requestedDate = LocalDateTime.now();

        this.status = RequestStatus.PENDING;
        this.resolved = false;
    }

    public void approve(String approvedBy, String approveMessage) {
        if (status != RequestStatus.PENDING) {
            throw new InputInvalidException("Request is not pending.");
        }
        if (approvedBy == null || approvedBy.isEmpty()) {
            throw new InputInvalidException("Approver must not be null or empty.");
        }

        this.status = RequestStatus.APPROVED;
        this.approveMessage = approveMessage;
        this.resolvedBy = approvedBy;
        this.resolvedDate = LocalDateTime.now();
        this.resolved = true;
    }

    public void reject(String rejectedBy, String rejectReason) {
        if (!canResolve()) {
            throw new InputInvalidException("This request cannot be resolved because it is not pending and is not unresolved.");
        }
        if (rejectedBy == null || rejectedBy.isEmpty()) {
            throw new InputInvalidException("Rejecter must not be null or empty.");
        }

        this.status = RequestStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.resolvedBy = rejectedBy;
        this.resolvedDate = LocalDateTime.now();
        this.resolved = true;
    }

    public boolean isUnresolved() {
        return !resolved;
    }

    private boolean canResolve() {
        return !resolved && status == RequestStatus.PENDING;
    }

}
