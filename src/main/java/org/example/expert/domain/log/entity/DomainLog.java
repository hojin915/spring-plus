package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "log")
public class DomainLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime requestedAt;
    private Long requestedUserId;
    private Long todoId;
    private Long registeredUserId;

    @Setter
    private String logMessage;

    @Builder
    public DomainLog(LocalDateTime requestedAt, Long requestedUserId, Long todoId, Long registeredUserId) {
        this.requestedAt = requestedAt;
        this.requestedUserId = requestedUserId;
        this.todoId = todoId;
        this.registeredUserId = registeredUserId;
    }

    protected DomainLog() {
    }
}