package org.example.expert.domain.common;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final UserRepository userRepository;
    private final EntityManager em;

    // 내부에 있으면 Transactional 작동 안해서 분리
    // 전체에 Transactional 걸려있을 때 도중에 멈추면 데이터 추가 안됨
    @Transactional
    public void batchInsert(List<User> users) {
        userRepository.saveAll(users);
        em.flush();
        em.clear();
    }
}
