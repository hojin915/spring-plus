package org.example.expert.domain.log.repository;

import org.example.expert.domain.log.entity.DomainLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LogRepository extends JpaRepository<DomainLog, Long> {
    Page<DomainLog> findAllByRequestedAtBetween(Pageable pageable, LocalDateTime start, LocalDateTime end);
}
