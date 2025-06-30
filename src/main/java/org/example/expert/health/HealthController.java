package org.example.expert.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // 기본 health check
    // 구체적인 health check : GET "/admin/health"(admin 권한 포함 토큰 필요)
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}