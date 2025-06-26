package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.log.dto.LogResponseDto;
import org.example.expert.domain.log.entity.DomainLog;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(DomainLog log) {
        logRepository.save(log);
    }

    public Page<LogResponseDto> getLogs(int page, int size, LocalDateTime start, LocalDateTime end) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestedAt").descending());

        if(start == null){
            start = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if(end == null){
            end = LocalDateTime.now();
        }

        Page<DomainLog> logs = logRepository.findAllByRequestedAtBetween(pageable, start, end);

        return logs.map(LogResponseDto::new);
    }
}
