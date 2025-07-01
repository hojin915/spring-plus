package org.example.expert.domain.auth.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.BatchService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BatchService batchService;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getEmail(),
                signupRequest.getNickname(),
                encodedPassword,
                userRole
        );
        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getNickname(), savedUser.getEmail(), userRole);

        return new SignupResponse(bearerToken);
    }

    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getNickname(), user.getEmail(), user.getUserRole());

        return new SigninResponse(bearerToken);
    }

    // userCount 만큼 테스트 유저 생성
    public Integer setUser() {
        int userCount = 1000000;
        int batchSize = 5000;
        List<User> toInsert = new ArrayList<>();

        for(int i = 1; i <= userCount; i++) {
            String email = i + "@example.com";
            String nickname = generateNickname();
            String password = passwordEncoder.encode("1234");
            User user = new User(email, nickname, password, UserRole.USER);
            toInsert.add(user);

            // batchSize 단위로 DB 에 반영
            if(toInsert.size() >= batchSize) {
                batchService.batchInsert(toInsert);
                toInsert.clear();
                System.out.println("Processing " + i + " of " + userCount);
            }
        }

        if(!toInsert.isEmpty()) {
            userRepository.saveAll(toInsert);
        }
        System.out.println("Complete setting users");
        return userCount;
    }

    // 1~5자 사이의 랜덤 소문자 알파벳으로 구성된 닉네임 생성
    private String generateNickname() {
        Random random = new Random();

        int randomLength = random.nextInt(5) + 1;
        StringBuilder nickname = new StringBuilder();
        for(int i = 0; i < randomLength; i++) {
            char randomChar = (char) ('a' + random.nextInt(26));
            nickname.append(randomChar);
        }

        return nickname.toString();
    }
}