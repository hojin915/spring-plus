package org.example.expert.domain.log.dto;

import lombok.Getter;
import org.example.expert.domain.log.entity.DomainLog;

import java.time.LocalDateTime;

@Getter
public class LogResponseDto {
    private Long id;
    private LocalDateTime requestedAt;
    private Long requestedUserId;
    private Long todoId;
    private Long registeredUserId;
    private String logMessage;

    public LogResponseDto(DomainLog log) {
        this.id = log.getId();
        this.requestedAt = log.getRequestedAt();
        this.requestedUserId = log.getRequestedUserId();
        this.todoId = log.getTodoId();
        this.registeredUserId = log.getRegisteredUserId();
        this.logMessage = log.getLogMessage();
    }
}
