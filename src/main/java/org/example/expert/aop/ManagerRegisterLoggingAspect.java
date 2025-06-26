package org.example.expert.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.dto.CustomUser;
import org.example.expert.domain.log.entity.DomainLog;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManagerRegisterLoggingAspect {

    private final LogService logService;

    // 로그에 넣을 값은 request
    // 실행 결과(로그 메세지)는 response 에서 가져오기 때문에
    // Around 를 선택
    @Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
    public Object aroundSaveManager(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        LocalDateTime requestedTime = LocalDateTime.now();

        // 요청 arguments 에서 로그에 필요한 값 추출
        CustomUser user = (CustomUser) args[0];
        Long todoId = (Long) args[1];
        ManagerSaveRequest managerSaveRequest = (ManagerSaveRequest) args[2];

        Long requestedUserId = user.getId();
        Long registeredUserId = managerSaveRequest.getManagerUserId();

        // 로그 생성
        DomainLog log = DomainLog.builder()
                .requestedAt(requestedTime)
                .requestedUserId(requestedUserId)
                .todoId(todoId)
                .registeredUserId(registeredUserId).build();

        Object result;
        try {
            result = joinPoint.proceed(args);
            // 성공시 로그 메세지 Success
            log.setLogMessage("Success");
        } catch (Throwable throwable) {
            // 예외 발생시 예외 메세지
            log.setLogMessage(throwable.getMessage());
            throw throwable;
        } finally {
            // DB 로그 저장
            logService.saveLog(log);
        }

        return result;
    }
}
